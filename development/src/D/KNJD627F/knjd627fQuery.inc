<?php

require_once('for_php7.php');

class knjd627fQuery extends Query
{
    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    public function getDataDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    public function getProName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T2.PROFICIENCYNAME1 AS LABEL , ";
        $query .= "     T1.GRADE AS GRADE ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT GRADES";
        $query .= " ON ";
        $query .= "     GRADES.YEAR     = T1.YEAR ";
        $query .= " AND GRADES.GRADE    = T1.GRADE ";
        $query .= " AND GRADES.GRADE_CD = '03' "; //卒業年次
        if ($model->isIndicateSchoolKind()) {
            $query .= " AND GRADES.SCHOOL_KIND = '{$model->selectSchoolKind}'";
        }
        $query .= " LEFT JOIN PROFICIENCY_MST T2 ON T1.PROFICIENCYDIV = T2.PROFICIENCYDIV";
        $query .= "                             AND T1.PROFICIENCYCD  = T2.PROFICIENCYCD";
        $query .= " WHERE ";
        $query .= "     T1.YEAR           = '{$model->year}' AND ";
        $query .= "     T1.SEMESTER       = '{$model->semester}' AND ";
        $query .= "     T1.PROFICIENCYDIV = '{$model->proficiencyDiv}' ";

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象年度における科目コードと略称名を取得
    public function getClassCodes($model)
    {
        $query   = " SELECT ";
        $query  .= "     CLASSCD, ";
        $query  .= "     CLASSABBV ";
        $query  .= " FROM ";
        $query  .= "     V_CLASS_MST";
        $query  .= " WHERE";
        $query  .= "     YEAR = '{$model->year}' ";
        if ($model->isIndicateSchoolKind()) {
            $query .=  " AND SCHOOL_KIND = '{$model->selectSchoolKind}'";
        }
        return $query;
    }

    //対象年度における卒業年次学生の情報を取得
    public function getLastGradeStudentsInYear($model)
    {
        $query   = " SELECT ";
        $query  .= "     T1.GRADE, ";
        $query  .= "     T1.HR_CLASS, ";
        $query  .= "     T3.HR_CLASS_NAME1, ";
        $query  .= "     T1.COURSECODE, ";
        $query  .= "     TC.COURSECODEABBV1, ";
        $query  .= "     T1.ATTENDNO, ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     T2.NAME ";
        $query  .= " FROM ";
        $query  .= "     SCHREG_REGD_DAT T1 ";
        $query  .= "     LEFT JOIN SCHREG_BASE_MST T2 ";
        $query  .= "         ON T2.SCHREGNO = T1.SCHREGNO ";
        $query  .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query  .= "         ON  T3.YEAR       = T1.YEAR ";
        $query  .= "         AND T3.SEMESTER   = T1.SEMESTER ";
        $query  .= "         AND T3.GRADE      = T1.GRADE ";
        $query  .= "         AND T3.HR_CLASS   = T1.HR_CLASS ";
        $query  .= "     LEFT JOIN V_COURSECODE_MST  TC ";
        $query  .= "         ON  TC.YEAR       = T1.YEAR ";
        $query  .= "         AND TC.COURSECODE = T1.COURSECODE ";
        $query  .= " WHERE ";
        $query  .= "         T1.YEAR     = '{$model->year}' ";
        $query  .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query  .= "     AND T1.GRADE    = '{$model->grade}' ";

        return $query;
    }

    //３ヵ年分の科目別修得単位を取得
    public function getCreditSouten($model)
    {
        $firstYear  = $model->year -2;   //入学年度
        $middleYear = $model->year -1;   //２年次年度

        $query  .= " WITH CALC_PASSFLG AS ( ";
        $query  .= " SELECT ";
        $query  .= "     T1.YEAR, ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     T3.CLASSCD, ";
        $query  .= "     T3.SCHOOL_KIND, ";
        $query  .= "     T3.CURRICULUM_CD, ";
        $query  .= "     T3.SUBCLASSCD, ";
        $query  .= "     T3.SCORE, ";
        $query  .= "     CASE ";
        $query  .= "         WHEN T4.PASS_SCORE <= T3.SCORE THEN '1' ";
        $query  .= "         ELSE '0' ";
        $query  .= "     END AS PASSFLG, ";
        $query  .= "     VALUE(T5.CREDITS, 0) AS CREDITS ";
        $query  .= " FROM ";
        $query  .= "     SCHREG_REGD_DAT T1 ";
        $query  .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query  .= "       ON T2.YEAR  = T1.YEAR ";
        $query  .= "      AND T2.GRADE = T1.GRADE ";
        $query  .= "     LEFT JOIN RECORD_RANK_SDIV_DAT T3 ";
        $query  .= "       ON T3.YEAR     = T1.YEAR ";
        $query  .= "      AND T3.SCHREGNO = T1.SCHREGNO ";
        $query  .= "      AND T3.SEMESTER || '-' || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->GET_TESTCD}' ";
        $query  .= "     LEFT JOIN PERFECT_RECORD_DAT T4 ";
        $query  .= "       ON T4.YEAR          = T3.YEAR ";
        $query  .= "      AND T4.SEMESTER      = T3.SEMESTER ";
        $query  .= "      AND T4.TESTKINDCD    = T3.TESTKINDCD ";
        $query  .= "      AND T4.TESTITEMCD    = T3.TESTITEMCD ";
        $query  .= "      AND T4.CLASSCD       = T3.CLASSCD ";
        $query  .= "      AND T4.SCHOOL_KIND   = T3.SCHOOL_KIND ";
        $query  .= "      AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query  .= "      AND T4.SUBCLASSCD    = T3.SUBCLASSCD ";
        $query  .= "      AND DIV              = '01' ";
        $query  .= "     LEFT JOIN CREDIT_MST T5 ";
        $query  .= "       ON T5.YEAR          = T1.YEAR ";
        $query  .= "      AND T5.COURSECD      = T1.COURSECD ";
        $query  .= "      AND T5.MAJORCD       = T1.MAJORCD ";
        $query  .= "      AND T5.COURSECODE    = T1.COURSECODE ";
        $query  .= "      AND T5.GRADE         = T1.GRADE ";
        $query  .= "      AND T5.CLASSCD       = T3.CLASSCD ";
        $query  .= "      AND T5.SCHOOL_KIND   = T3.SCHOOL_KIND ";
        $query  .= "      AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query  .= "      AND T5.SUBCLASSCD    = T3.SUBCLASSCD ";
        $query  .= " WHERE ";
        $query  .= "     T1.YEAR BETWEEN '{$firstYear}' AND '{$model->year}' ";
        $query  .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query  .= "     AND T2.GRADE_CD BETWEEN '01' AND '03' ";
        if ($model->isIndicateSchoolKind()) {
            $query .=  " AND T2.SCHOOL_KIND = '{$model->selectSchoolKind}'";
        }
        $query  .= "     AND T3.SUBCLASSCD IS NOT NULL  AND T4.PASS_SCORE IS NOT NULL ";
        $query  .= "     AND T3.SUBCLASSCD NOT IN ( ";
        //合計科目コードは除外
        $seperator = "";
        foreach ($model->NotIncludeSubClss as $subcls) {
            $query  .= "{$seperator}'{$subcls}'";
            $seperator = ",";
        }
        $query  .= " ) ";
        $query  .= " ), NOPASS_SUBCLS AS ( ";
        $query  .= " SELECT DISTINCT ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     T1.CLASSCD, ";
        $query  .= "     T1.SCHOOL_KIND, ";
        $query  .= "     T1.CURRICULUM_CD, ";
        $query  .= "     T1.SUBCLASSCD, ";
        $query  .= "     SM.SUBCLASSABBV ";
        $query  .= " FROM ";
        $query  .= "     CALC_PASSFLG T1 ";
        $query  .= "     LEFT JOIN V_SUBCLASS_MST SM ";
        $query  .= "       ON SM.YEAR          = T1.YEAR ";
        $query  .= "      AND SM.CLASSCD       = T1.CLASSCD ";
        $query  .= "      AND SM.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query  .= "      AND SM.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query  .= "      AND SM.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query  .= " WHERE ";
        $query  .= "     T1.PASSFLG = '0' ";  //単位取得できなかった物を抽出
        $query  .= " ), MAKE_REMARKS AS ( ";
        $query  .= " SELECT ";
        $query  .= "     T1.YEAR, ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     LISTAGG(NS.SUBCLASSABBV, ',') AS REMARKS ";
        $query  .= " FROM ";
        $query  .= "     CALC_PASSFLG T1 ";
        $query  .= "     LEFT JOIN V_CLASS_MST VC ";
        $query  .= "       ON VC.YEAR        = T1.YEAR ";
        $query  .= "      AND VC.CLASSCD     = T1.CLASSCD ";
        $query  .= "      AND VC.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query  .= "     LEFT JOIN NOPASS_SUBCLS NS ";
        $query  .= "       ON NS.SCHREGNO      = T1.SCHREGNO ";
        $query  .= "      AND NS.CLASSCD       = T1.CLASSCD ";
        $query  .= "      AND NS.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query  .= "      AND NS.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query  .= "      AND NS.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query  .= " WHERE ";
        $query  .= "     T1.PASSFLG = '0' ";
        $query  .= " GROUP BY ";
        $query  .= "     T1.YEAR, ";
        $query  .= "     T1.SCHREGNO ";
        // 教科別の判定をするために、一度利用データを確定させる。
        $query  .= " ), RETBASE_DATA AS (";
        $query  .= " SELECT ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     T1.CLASSCD, ";
        $query  .= "     T1.SCHOOL_KIND, ";
        $query  .= "     SUM(VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0)) AS SOUTEN, ";
        $query  .= "     SUM(CASE WHEN T1.PASSFLG = '1' THEN VALUE(T1.CREDITS, 0) ELSE 0 END) AS T_CREDITS, ";
        $query  .= "     '' AS NPREMARK1, ";
        $query  .= "     '' AS NPREMARK2, ";
        $query  .= "     '' AS NPREMARK3 ";
        $query  .= " FROM ";
        $query  .= "     CALC_PASSFLG T1 ";
        $query  .= " GROUP BY ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     T1.CLASSCD, ";
        $query  .= "     T1.SCHOOL_KIND ";
        $query  .= " UNION ALL ";
        $query  .= " SELECT ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     '{$model->CLSFULL}' AS CLASSCD, ";
        $query  .= "     T1.SCHOOL_KIND, ";
        $query  .= "     SUM(VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0)) AS SOUTEN, ";
        $query  .= "     SUM(CASE WHEN T1.PASSFLG = '1' THEN VALUE(T1.CREDITS, 0) ELSE 0 END) AS T_CREDITS, ";
        $query  .= "     MR1.REMARKS AS NPREMARK1, ";
        $query  .= "     MR2.REMARKS AS NPREMARK2, ";
        $query  .= "     MR3.REMARKS AS NPREMARK3 ";
        $query  .= " FROM ";
        $query  .= "     CALC_PASSFLG T1 ";
        $query  .= "     LEFT JOIN MAKE_REMARKS MR1 ";
        $query  .= "       ON MR1.SCHREGNO = T1.SCHREGNO ";
        $query  .= "      AND MR1.YEAR = '{$firstYear}' ";
        $query  .= "     LEFT JOIN MAKE_REMARKS MR2 ";
        $query  .= "       ON MR2.SCHREGNO = T1.SCHREGNO ";
        $query  .= "      AND MR2.YEAR = '{$middleYear}' ";
        $query  .= "     LEFT JOIN MAKE_REMARKS MR3 ";
        $query  .= "       ON MR3.SCHREGNO = T1.SCHREGNO ";
        $query  .= "      AND MR3.YEAR = '{$model->year}' ";
        $query  .= " GROUP BY ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     T1.SCHOOL_KIND, ";
        $query  .= "     MR1.REMARKS, ";
        $query  .= "     MR2.REMARKS, ";
        $query  .= "     MR3.REMARKS ";
        $query  .= " ) ";
        $query  .= " SELECT ";
        $query  .= "   T1.*, ";
        $query  .= "   CASE ";
        $query  .= "       WHEN (D115.NAMECD1 IS NOT NULL AND VALUE(D115.NAME2, 0) > VALUE(T1.T_CREDITS, 0)) THEN 1 ";
        $query  .= "       ELSE 0 ";
        $query  .= "   END AS CHK_STAR ";
        $query  .= " FROM ";
        $query  .= "   RETBASE_DATA T1";
        $query  .= "   LEFT JOIN V_NAME_MST D115";
        $query  .= "     ON    D115.YEAR    = '{$model->year}' ";
        $query  .= "       AND D115.NAMECD1 = 'D115' ";
        $query  .= "       AND D115.NAME1 = T1.CLASSCD || T1.SCHOOL_KIND ";
        $query  .= " ORDER BY ";
        $query  .= "     SCHREGNO, ";
        $query  .= "     CLASSCD ";

        return $query;
    }

    //３ヵ年分の年次別総合点数および未履修科目を取得
    public function getSouten($model)
    {
        $firstYear  = $model->year -2;   //入学年度
        $middleYear = $model->year -1;   //２年次年度

        $query   = " WITH CALC_PASSFLG AS ( ";
        $query  .= " SELECT ";
        $query  .= "   T1.YEAR, ";
        $query  .= "   T1.SCHREGNO, ";
        $query  .= "   T3.CLASSCD, ";
        $query  .= "   T3.SCHOOL_KIND, ";
        $query  .= "   T3.CURRICULUM_CD, ";
        $query  .= "   T3.SUBCLASSCD, ";
        $query  .= "   T3.SCORE, ";
        $query  .= "   T4.PASS_SCORE, ";
        $query  .= "   CASE ";
        $query  .= "       WHEN T4.PASS_SCORE <= T3.SCORE THEN '1' ";
        $query  .= "       ELSE '0' ";
        $query  .= "   END AS PASSFLG, ";
        $query  .= "   VALUE(T5.CREDITS, 0) AS CREDITS ";
        $query  .= " FROM ";
        $query  .= "   SCHREG_REGD_DAT T1 ";
        $query  .= "   LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query  .= "     ON T2.YEAR  = T1.YEAR ";
        $query  .= "    AND T2.GRADE = T1.GRADE ";
        $query  .= "   LEFT JOIN RECORD_RANK_SDIV_DAT T3 ";
        $query  .= "     ON T3.YEAR     = T1.YEAR ";
        $query  .= "    AND T3.SEMESTER || '-' || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->GET_TESTCD}' ";
        $query  .= "    AND T3.SCHREGNO = T1.SCHREGNO ";
        $query  .= "   LEFT JOIN PERFECT_RECORD_DAT T4 ";
        $query  .= "     ON T4.YEAR          = T3.YEAR ";
        $query  .= "    AND T4.SEMESTER      = T3.SEMESTER ";
        $query  .= "    AND T4.TESTKINDCD    = T3.TESTKINDCD ";
        $query  .= "    AND T4.TESTITEMCD    = T3.TESTITEMCD ";
        $query  .= "    AND T4.CLASSCD       = T3.CLASSCD ";
        $query  .= "    AND T4.SCHOOL_KIND   = T3.SCHOOL_KIND ";
        $query  .= "    AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query  .= "    AND T4.SUBCLASSCD    = T3.SUBCLASSCD ";
        $query  .= "    AND DIV              = '01' ";
        $query  .= "   LEFT JOIN CREDIT_MST T5 ";
        $query  .= "     ON T5.YEAR          = T1.YEAR ";
        $query  .= "    AND T5.COURSECD      = T1.COURSECD ";
        $query  .= "    AND T5.MAJORCD       = T1.MAJORCD ";
        $query  .= "    AND T5.COURSECODE    = T1.COURSECODE ";
        $query  .= "    AND T5.GRADE         = T1.GRADE ";
        $query  .= "    AND T5.CLASSCD       = T3.CLASSCD ";
        $query  .= "    AND T5.SCHOOL_KIND   = T3.SCHOOL_KIND ";
        $query  .= "    AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query  .= "    AND T5.SUBCLASSCD    = T3.SUBCLASSCD ";
        $query  .= " WHERE ";
        $query  .= "        T1.YEAR BETWEEN '{$firstYear}' AND '{$model->year}' ";
        $query  .= "    AND T1.SEMESTER    = '{$model->semester}' ";
        $query  .= "    AND T2.GRADE_CD BETWEEN '01' AND '03' ";
        if ($model->isIndicateSchoolKind()) {
            $query .= " AND T2.SCHOOL_KIND = '{$model->selectSchoolKind}'";
        }
        $query  .= "   AND T3.SUBCLASSCD IS NOT NULL ";
        $query  .= "   AND T3.SUBCLASSCD NOT IN (";
        //合計科目コードは除外
        $seperator = "";
        foreach ($model->NotIncludeSubClss as $subcls) {
            $query  .= "{$seperator}'{$subcls}'";
            $seperator = ",";
        }
        $query  .= ") ";
        //未修得科目名を取得
        $query  .= " ), NOPASS_SUBCLS AS ( ";
        $query  .= " SELECT DISTINCT ";
        $query  .= "   T1.YEAR, ";
        $query  .= "   T1.SCHREGNO, ";
        $query  .= "   T1.CLASSCD, ";
        $query  .= "   T1.SCHOOL_KIND, ";
        $query  .= "   T1.CURRICULUM_CD, ";
        $query  .= "   T1.SUBCLASSCD, ";
        $query  .= "   SM.SUBCLASSABBV ";
        $query  .= " FROM ";
        $query  .= "   CALC_PASSFLG T1 ";
        $query  .= "   LEFT JOIN V_SUBCLASS_MST SM ";
        $query  .= "     ON SM.YEAR          = T1.YEAR ";
        $query  .= "    AND SM.CLASSCD       = T1.CLASSCD ";
        $query  .= "    AND SM.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query  .= "    AND SM.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query  .= "    AND SM.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query  .= " WHERE ";
        $query  .= "   T1.PASSFLG = '0' ";
        //未修得科目を年度、生徒単位に集約
        $query  .= " ), MAKE_REMARKS AS ( ";
        $query  .= " SELECT ";
        $query  .= "   T1.YEAR, ";
        $query  .= "   T1.SCHREGNO, ";
        $query  .= "   LISTAGG(NS.SUBCLASSABBV, ',') AS REMARKS ";
        $query  .= " FROM ";
        $query  .= "   CALC_PASSFLG T1 ";
        $query  .= "   LEFT JOIN V_CLASS_MST VC ";
        $query  .= "     ON VC.YEAR        = T1.YEAR ";
        $query  .= "    AND VC.CLASSCD     = T1.CLASSCD ";
        $query  .= "    AND VC.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query  .= "   LEFT JOIN NOPASS_SUBCLS NS ";
        $query  .= "     ON NS.YEAR          = T1.YEAR ";
        $query  .= "    AND NS.SCHREGNO      = T1.SCHREGNO ";
        $query  .= "    AND NS.CLASSCD       = T1.CLASSCD ";
        $query  .= "    AND NS.SCHOOL_KIND   = T1.SCHOOL_KIND ";
        $query  .= "    AND NS.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query  .= "    AND NS.SUBCLASSCD    = T1.SUBCLASSCD ";
        $query  .= " WHERE ";
        $query  .= "   T1.PASSFLG = '0' ";
        $query  .= " GROUP BY ";
        $query  .= "   T1.YEAR, ";
        $query  .= "   T1.SCHREGNO ";
        $query  .= " ORDER BY ";
        $query  .= "   T1.YEAR, ";
        $query  .= "   T1.SCHREGNO ";
        //不足点の元となるD1～D3と総点を算出
        $query  .= " ), CALC_D AS ( ";
        $query  .= " SELECT ";
        $query  .= "   T1.SCHREGNO, ";
        $query  .= "   SUM(VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0)) AS SOUTEN, ";
        $query  .= "   SUM(";
        $query  .= "       CASE ";
        $query  .= "           WHEN T1.YEAR = '{$firstYear}' THEN VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0) ";
        $query  .= "       ELSE 0";
        $query  .= "   END) - {$model->DANGERSOUTEN_ONEYEARLINE} AS D1, ";
        $query  .= "   SUM(";
        $query  .= "       CASE ";
        $query  .= "           WHEN T1.YEAR = '{$middleYear}' THEN VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0) ";
        $query  .= "       ELSE 0";
        $query  .= "   END) - {$model->DANGERSOUTEN_ONEYEARLINE} AS D2, ";
        $query  .= "   SUM(";
        $query  .= "       CASE ";
        $query  .= "           WHEN T1.YEAR = '{$model->year}' THEN VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0) ";
        $query  .= "       ELSE 0";
        $query  .= "   END) - {$model->DANGERSOUTEN_ONEYEARLINE} AS D3 ";
        $query  .= " FROM ";
        $query  .= "   CALC_PASSFLG T1 ";
        $query  .= " GROUP BY ";
        $query  .= "   T1.SCHREGNO ";
        //不足点算出
        //不足店について：過年度が悪くて当年度が良ければ超過分で前年度の補填ができるが、過年度が良くて当年度が悪ければ、過年度分で補填できない。
        //                要は、「"過去"は補えるが、"今"は補えない仕組み。」それをCASE WHEN でパターン分けしている。
        //D1～D3について：単位*評価の合計がDANGERSOUTEN_ONEYEARLINEを超えているか超えていないかを、各学年でチェックする。
        $query  .= " ), CALC_FUSOKU AS ( ";
        $query  .= " SELECT ";
        $query  .= "   T1.SCHREGNO, ";
        $query  .= "   T1.SOUTEN, ";
        $query  .= "   T1.D1, ";
        $query  .= "   T1.D2, ";
        $query  .= "   T1.D3, ";
        $query  .= "   CASE ";
        $query  .= "       WHEN T1.D2 < 0 AND T1.D1 >= 0 THEN T1.D2 + T1.D3 ";
        $query  .= "       WHEN T1.D3 < 0 AND T1.D2 >= 0 AND T1.D1 + T1.D2 >= 0 THEN T1.D3 ";
        $query  .= "       ELSE T1.D1 + T1.D2 + T1.D3 ";
        $query  .= "   END AS FUSOKUTEN ";
        $query  .= " FROM ";
        $query  .= "   CALC_D T1 ";
        //英語の試験コードを取得(複数科目あったら、コードの小さい方を取得。そもそも英語しか登録が無い前提。)
        $query  .= " ), PROFICIENCY_ONEDAT AS ( ";
        $query  .= " SELECT ";
        $query  .= "   YEAR, ";
        $query  .= "   SEMESTER, ";
        $query  .= "   PROFICIENCYDIV, ";
        $query  .= "   PROFICIENCYCD, ";
        $query  .= "   SCHREGNO, ";
        $query  .= "   MIN(PROFICIENCY_SUBCLASS_CD) AS PROFICIENCY_SUBCLASS_CD ";
        $query  .= " FROM ";
        $query  .= "   PROFICIENCY_DAT ";
        $query  .= " GROUP BY ";
        $query  .= "   YEAR, ";
        $query  .= "   SEMESTER, ";
        $query  .= "   PROFICIENCYDIV, ";
        $query  .= "   PROFICIENCYCD, ";
        $query  .= "   SCHREGNO ";
        //英語の試験結果を取得
        $query  .= " ), PROFICIENCY_MAKEDAT AS ( ";
        $query  .= " SELECT ";
        $query  .= "   T4.* ";
        $query  .= " FROM ";
        $query  .= "   PROFICIENCY_DAT T4 ";
        $query  .= "   INNER JOIN PROFICIENCY_ONEDAT T6 ";
        $query  .= "     ON T6.YEAR                    = T4.YEAR ";
        $query  .= "    AND T6.SEMESTER                = T4.SEMESTER ";
        $query  .= "    AND T6.PROFICIENCYDIV          = T4.PROFICIENCYDIV ";
        $query  .= "    AND T6.PROFICIENCYCD           = T4.PROFICIENCYCD ";
        $query  .= "    AND T6.SCHREGNO                = T4.SCHREGNO ";
        $query  .= "    AND T6.PROFICIENCY_SUBCLASS_CD = T4.PROFICIENCY_SUBCLASS_CD ";
        //英語力判定試験基準(固定値)テーブル(範囲開始,範囲終了,ランク文字列,合否(合格は1))
        $query  .= " ), ENG_TESTTBL(RANGE_S, RANGE_E, RANK, JUDGE) AS ( ";
        $query  .= "   VALUES(350, 400, 'A', 1) ";
        $query  .= "   UNION ";
        $query  .= "   VALUES(300, 349, 'B', 1) ";
        $query  .= "   UNION ";
        $query  .= "   VALUES(250, 299, 'C', 1) ";
        $query  .= "   UNION ";
        $query  .= "   VALUES(200, 249, 'D', 1) ";
        $query  .= "   UNION ";
        $query  .= "   VALUES(150, 199, 'E', 0) ";
        $query  .= "   UNION ";
        $query  .= "   VALUES(0, 149, 'F', 0) ";
        $query  .= " ) ";
        $query  .= " SELECT ";
        $query  .= "   T1.SCHREGNO, ";
        $query  .= "   T1.SOUTEN, ";
        $query  .= "   T1.FUSOKUTEN, ";
        $query  .= "   {$model->DANGERSOUTEN_ONEYEARLINE} + T1.D1 AS D1Val, ";
        $query  .= "   {$model->DANGERSOUTEN_ONEYEARLINE} + T1.D2 AS D2Val, ";
        $query  .= "   {$model->DANGERSOUTEN_ONEYEARLINE} + T1.D3 AS D3Val, ";
        $query  .= "   T4.SCORE AS ENG_TEST_SCORE, ";
        $query  .= "   CASE WHEN T4.SCORE IS NULL THEN NULL ELSE T5.RANK END AS RANK, ";
        $query  .= "   CASE WHEN T4.SCORE IS NULL THEN NULL ELSE T5.JUDGE END AS JUDGE, ";
        $query  .= "   MR1.REMARKS AS NPREMARK1, ";
        $query  .= "   MR2.REMARKS AS NPREMARK2, ";
        $query  .= "   MR3.REMARKS AS NPREMARK3 ";
        $query  .= " FROM ";
        $query  .= "   CALC_FUSOKU T1 ";
        $query  .= "   LEFT JOIN PROFICIENCY_YMST T2 ";
        $query  .= "     ON T2.YEAR           = '{$model->year}' ";
        $query  .= "    AND T2.SEMESTER       = '{$model->semester}' ";
        $query  .= "    AND T2.PROFICIENCYDIV = '{$model->proficiencyDiv}' ";
        $query  .= "    AND T2.PROFICIENCYCD  = '{$model->proficiencyCd}' ";
        $query  .= "    AND T2.GRADE          = '{$model->grade}' ";
        $query  .= "   LEFT JOIN PROFICIENCY_MST T3 ";
        $query  .= "     ON T3.PROFICIENCYDIV = T2.PROFICIENCYDIV ";
        $query  .= "    AND T3.PROFICIENCYCD  = T2.PROFICIENCYCD ";
        $query  .= "   LEFT JOIN MAKE_REMARKS MR1 ";
        $query  .= "     ON MR1.SCHREGNO = T1.SCHREGNO ";
        $query  .= "    AND MR1.YEAR     = '{$firstYear}' ";
        $query  .= "   LEFT JOIN MAKE_REMARKS MR2 ";
        $query  .= "     ON MR2.SCHREGNO = T1.SCHREGNO ";
        $query  .= "    AND MR2.YEAR     = '{$middleYear}' ";
        $query  .= "   LEFT JOIN MAKE_REMARKS MR3 ";
        $query  .= "     ON MR3.SCHREGNO = T1.SCHREGNO ";
        $query  .= "    AND MR3.YEAR     = '{$model->year}' ";
        $query  .= "   LEFT JOIN PROFICIENCY_MAKEDAT T4 ";
        $query  .= "     ON T4.YEAR           = T2.YEAR ";
        $query  .= "    AND T4.SEMESTER       = T2.SEMESTER ";
        $query  .= "    AND T4.PROFICIENCYDIV = T2.PROFICIENCYDIV ";
        $query  .= "    AND T4.PROFICIENCYCD  = T2.PROFICIENCYCD ";
        $query  .= "    AND T4.SCHREGNO       = T1.SCHREGNO ";
        $query  .= "   LEFT JOIN ENG_TESTTBL T5 ";
        $query  .= "     ON VALUE(T4.SCORE, 0) BETWEEN T5.RANGE_S AND T5.RANGE_E ";
        $query  .= " ORDER BY ";
        $query  .= "   SCHREGNO ";

        return $query;
    }
}
