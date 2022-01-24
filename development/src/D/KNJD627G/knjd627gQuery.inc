<?php

class knjd627gQuery extends Query
{
    public function getLastSeme($model)
    {
        $query .= " SELECT ";
        $query .= "   SEMESTER ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$model->year}' AND ";
        $query .= "   SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "   SEMESTER DESC ";

        return $query;
    }


    //CSV出力データ
    public function getSchregSouten($model, $seme)
    {
        //対象学年クラス
        $query .= " WITH GRADE_HR AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "   GDAT.YEAR, ";
        $query .= "   GDAT.GRADE, ";
        $query .= "   HDAT.HR_CLASS, ";
        $query .= "   HDAT.HR_CLASS_NAME1 ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_GDAT GDAT ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREG_REGD_HDAT HDAT ";
        $query .= "    ON HDAT.YEAR = GDAT.YEAR ";
        $query .= "   AND HDAT.GRADE = GDAT.GRADE ";
        $query .= "   AND HR_CLASS_NAME1 IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "   GDAT.YEAR = '{$model->year}' ";
        $query .= "   AND GDAT.SCHOOL_KIND = 'H' ";
        $query .= "   AND GDAT.GRADE_CD = '{$model->grade}' ";
        //対象生徒
        $query .= " ), SCHREG_BASE AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.HR_CLASS_NAME1, ";
        $query .= "   REGD.SEMESTER, ";
        $query .= "   REGD.SCHREGNO, ";
        $query .= "   REGD.ATTENDNO, ";
        $query .= "   BASE.NAME ";
        $query .= " FROM ";
        $query .= "   GRADE_HR T1 ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREG_REGD_DAT REGD ";
        $query .= "    ON REGD.YEAR = T1.YEAR ";
        $query .= "   AND REGD.SEMESTER = '{$seme}' ";
        $query .= "   AND REGD.GRADE = T1.GRADE ";
        $query .= "   AND REGD.HR_CLASS = T1.HR_CLASS ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREG_BASE_MST BASE ";
        $query .= "    ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "   REGD.HR_CLASS, REGD.ATTENDNO ";
        //対象生徒の前学年度クラス
        $query .= " ), GRADE_PREV AS ( ";
        $query .= " SELECT ";
        $query .= "   MAX(REGD.YEAR) AS YEAR, ";
        $query .= "   REGD.SCHREGNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE BASE ";
        $query .= " INNER JOIN ";
        $query .= "   SCHREG_REGD_DAT REGD ";
        $query .= "    ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "   AND REGD.YEAR < '{$model->year}' ";
        $query .= "   AND REGD.GRADE < BASE.GRADE ";
        $query .= " GROUP BY ";
        $query .= "   REGD.SCHREGNO ";
        //今学年度の修得単位
        $query .= " ), CREDIT AS ( ";
        $query .= " SELECT ";
        $query .= "   BASE.YEAR, ";
        $query .= "   BASE.SCHREGNO, ";
        $query .= "   T3.CLASSCD, ";
        $query .= "   T3.SCHOOL_KIND, ";
        $query .= "   T3.CURRICULUM_CD, ";
        $query .= "   T3.SUBCLASSCD, ";
        $query .= "   VALUE(T3.GET_CREDIT,0) AS CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE BASE ";
        $query .= " LEFT JOIN RECORD_SCORE_DAT T3 ";
        $query .= "      ON T3.YEAR = BASE.YEAR ";
        $query .= "     AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '9990009' ";
        $query .= "     AND T3.SCHREGNO = BASE.SCHREGNO ";
        //前学年度の修得単位
        $query .= " ), CREDIT_PREV AS ( ";
        $query .= " SELECT ";
        $query .= "   BASE.YEAR, ";
        $query .= "   BASE.SCHREGNO, ";
        $query .= "   T3.CLASSCD, ";
        $query .= "   T3.SCHOOL_KIND, ";
        $query .= "   T3.CURRICULUM_CD, ";
        $query .= "   T3.SUBCLASSCD, ";
        $query .= "   VALUE(T3.GET_CREDIT,0) AS CREDIT ";
        $query .= " FROM ";
        $query .= "     GRADE_PREV BASE ";
        $query .= " LEFT JOIN RECORD_SCORE_DAT T3 ";
        $query .= "      ON T3.YEAR = BASE.YEAR ";
        $query .= "     AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '9990009' ";
        $query .= "     AND T3.SCHREGNO = BASE.SCHREGNO ";
        //1、2年時の修得単位
        $query .= " ), TOTAL_CREDIT AS ( ";
        $query .= " SELECT * FROM CREDIT_PREV UNION SELECT * FROM CREDIT ";
        $query .= " ), NOPASS_SUBCLS AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     SM.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     TOTAL_CREDIT T1 ";
        $query .= " LEFT JOIN V_SUBCLASS_MST SM ";
        $query .= "      ON SM.YEAR = T1.YEAR ";
        $query .= "     AND SM.CLASSCD = T1.CLASSCD ";
        $query .= "     AND SM.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     AND SM.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     AND SM.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.CREDIT < 1 ";
        $query .= " ORDER BY T1.SCHREGNO, T1.YEAR, T1.SUBCLASSCD ";
        //未修得科目
        $query .= " ), MAKE_REMARKS AS ( ";
        $query .= " SELECT ";
        $query .= "     NS.SCHREGNO, ";
        $query .= "     LISTAGG(NS.SUBCLASSABBV, ',') AS REMARKS ";
        $query .= " FROM ";
        $query .= "    NOPASS_SUBCLS NS ";
        $query .= " GROUP BY ";
        $query .= "     NS.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     NS.SCHREGNO ";
        $query .= " ) ";
        //メイン表
        $query .= " SELECT ";
        $query .= "   BASE.YEAR AS YEAR2, ";
        $query .= "   BASE.HR_CLASS_NAME1, ";
        $query .= "   CAST(BASE.ATTENDNO AS INT) ATTENDNO, ";
        $query .= "   BASE.SCHREGNO, ";
        $query .= "   BASE.NAME, ";
        $query .= "   SOUTEN.TOTAL_POINT AS SOUTEN, ";
        if ($model->grade == "01") {
            $query .= "   '' AS YEAR1, ";
            $query .= "   '' AS SOUTEN_SUB1, ";
        } else {
            $query .= "   T2.YEAR AS YEAR1, ";
            $query .= "   SOUTEN_PREV.TOTAL_POINT AS SOUTEN_SUB1, ";
        }
        $query .= "   DECIMAL(ROUND(FLOAT(RANK.AVG)*10,0)/10,5,1) AS AVG, ";
        $query .= "   REMARK.REMARKS ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE BASE ";
        $query .= " LEFT JOIN ";
        $query .= "   RECORD_RANK_SDIV_SOUTEN_DAT SOUTEN ";
        $query .= "    ON SOUTEN.YEAR = BASE.YEAR ";
        $query .= "   AND SOUTEN.SEMESTER = '9' ";
        $query .= "   AND SOUTEN.TESTKINDCD = '99' ";
        $query .= "   AND SOUTEN.TESTITEMCD = '00' ";
        $query .= "   AND SOUTEN.SCORE_DIV = '08' ";
        $query .= "   AND SOUTEN.CLASSCD = '99' ";
        $query .= "   AND SOUTEN.SCHOOL_KIND = 'H' ";
        $query .= "   AND SOUTEN.CURRICULUM_CD = '99' ";
        $query .= "   AND SOUTEN.SUBCLASSCD = '999999' ";
        $query .= "   AND SOUTEN.SCHREGNO = BASE.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "   GRADE_PREV T2 ";
        $query .= "    ON T2.SCHREGNO = BASE.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "   RECORD_RANK_SDIV_SOUTEN_DAT SOUTEN_PREV ";
        $query .= "    ON SOUTEN_PREV.YEAR = T2.YEAR ";
        $query .= "   AND SOUTEN_PREV.SEMESTER = '9' ";
        $query .= "   AND SOUTEN_PREV.TESTKINDCD = '99' ";
        $query .= "   AND SOUTEN_PREV.TESTITEMCD = '00' ";
        $query .= "   AND SOUTEN_PREV.SCORE_DIV = '08' ";
        $query .= "   AND SOUTEN_PREV.CLASSCD = '99' ";
        $query .= "   AND SOUTEN_PREV.SCHOOL_KIND = 'H' ";
        $query .= "   AND SOUTEN_PREV.CURRICULUM_CD = '99' ";
        $query .= "   AND SOUTEN_PREV.SUBCLASSCD = '999999' ";
        $query .= "   AND SOUTEN_PREV.SCHREGNO = T2.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "   RECORD_RANK_SDIV_DAT RANK ";
        $query .= "    ON RANK.YEAR = BASE.YEAR ";
        $query .= "   AND RANK.SEMESTER = '9' ";
        $query .= "   AND RANK.TESTKINDCD = '99' ";
        $query .= "   AND RANK.TESTITEMCD = '00' ";
        $query .= "   AND RANK.SCORE_DIV = '08' ";
        $query .= "   AND RANK.CLASSCD = '99' ";
        $query .= "   AND RANK.SCHOOL_KIND = 'H' ";
        $query .= "   AND RANK.CURRICULUM_CD = '99' ";
        $query .= "   AND RANK.SUBCLASSCD = '999999' ";
        $query .= "   AND RANK.SCHREGNO = BASE.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "   MAKE_REMARKS REMARK ";
        $query .= "    ON REMARK.SCHREGNO = BASE.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   SOUTEN.TOTAL_POINT IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "   SOUTEN DESC ";
        
        return $query;
    }
}
