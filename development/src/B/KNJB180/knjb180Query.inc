<?php

require_once('for_php7.php');

class knjb180Query extends Query {
    //ＣＳＶ出力---授業料(単位情報)
    function getOutputQueryCsv1($model) {
        //対象生徒
        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         GRADE, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '{$model->obj_year}' ";
        $query .= "     AND SEMESTER = '{$model->obj_semester}' ";
        $query .= "  ) ";

        //各生徒が履修している科目に単位加算・固定、親科目、子科目どちらなのかの目印をつける
        $query .= " ,SUBMAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS SUBCLASSCD, ";
            $query .= "         L2.COMBINED_CLASSCD || '-' || L2.COMBINED_SCHOOL_KIND || '-' || L2.COMBINED_CURRICULUM_CD || '-' || L2.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
            $query .= "         L2.ATTEND_CLASSCD || '-' || L2.ATTEND_SCHOOL_KIND || '-' || L2.ATTEND_CURRICULUM_CD || '-' || L2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ";
            $query .= "         L2.CALCULATE_CREDIT_FLG, ";
            $query .= "         CASE WHEN (L1.CLASSCD       = L2.COMBINED_CLASSCD ";
            $query .= "                AND L1.SCHOOL_KIND   = L2.COMBINED_SCHOOL_KIND ";
            $query .= "                AND L1.CURRICULUM_CD = L2.COMBINED_CURRICULUM_CD ";
            $query .= "                AND L1.SUBCLASSCD    = L2.COMBINED_SUBCLASSCD) ";
            $query .= "              THEN 'COMB' ";
            $query .= "              WHEN (L1.CLASSCD       = L2.ATTEND_CLASSCD ";
            $query .= "                AND L1.SCHOOL_KIND   = L2.ATTEND_SCHOOL_KIND ";
            $query .= "                AND L1.CURRICULUM_CD = L2.ATTEND_CURRICULUM_CD ";
            $query .= "                AND L1.SUBCLASSCD    = L2.ATTEND_SUBCLASSCD) ";
            $query .= "              THEN 'ATTE' ";
            $query .= "              ELSE NULL ";
            $query .= "         END AS COMB_ATTE_FLG ";
        } else {
            $query .= "         L1.SUBCLASSCD, ";
            $query .= "         L2.COMBINED_SUBCLASSCD, ";
            $query .= "         L2.ATTEND_SUBCLASSCD, ";
            $query .= "         L2.CALCULATE_CREDIT_FLG, ";
            $query .= "         CASE WHEN L1.SUBCLASSCD = L2.COMBINED_SUBCLASSCD ";
            $query .= "             THEN 'COMB' ";
            $query .= "             WHEN L1.SUBCLASSCD = L2.ATTEND_SUBCLASSCD ";
            $query .= "             THEN 'ATTE' ";
            $query .= "             ELSE NULL ";
            $query .= "         END AS COMB_ATTE_FLG ";
        }
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         CHAIR_DAT L1 ON  L1.YEAR     = T1.YEAR ";
        $query .= "                      AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                      AND L1.CHAIRCD  = T1.CHAIRCD ";
        $query .= "                      AND SUBSTR(L1.SUBCLASSCD,1,2) <= '90' ";
        $query .= "                      AND SUBSTR(L1.SUBCLASSCD,5,1) = '{$model->obj_radio}' "; //4:定時制、5:通信制
        $query .= "     LEFT JOIN ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT L2 ON   L2.YEAR                = T1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                          AND (( L2.COMBINED_CLASSCD         = L1.CLASSCD ";
            $query .= "                                             AND L2.COMBINED_SCHOOL_KIND     = L1.SCHOOL_KIND ";
            $query .= "                                             AND L2.COMBINED_CURRICULUM_CD   = L1.CURRICULUM_CD ";
            $query .= "                                             AND L2.COMBINED_SUBCLASSCD      = L1.SUBCLASSCD) ";
            $query .= "                                          OR   ( L2.ATTEND_CLASSCD           = L1.CLASSCD ";
            $query .= "                                             AND L2.ATTEND_SCHOOL_KIND       = L1.SCHOOL_KIND ";
            $query .= "                                             AND L2.ATTEND_CURRICULUM_CD     = L1.CURRICULUM_CD ";
            $query .= "                                             AND L2.ATTEND_SUBCLASSCD        = L1.SUBCLASSCD)) ";
        } else {
            $query .= "                                          AND (L2.COMBINED_SUBCLASSCD = L1.SUBCLASSCD ";
            $query .= "                                          OR   L2.ATTEND_SUBCLASSCD   = L1.SUBCLASSCD) ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->obj_year}' ";
        $query .= " ) ";

        //各生徒が履修している科目
        //対象外のレコードを取除く(固定で合併元、加算で合併先は対象外)
        $query .= " ,CHAIR AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN ";
        $query .= "     WHERE ";
        $query .= "             CALCULATE_CREDIT_FLG IS NULL ";
        $query .= "         OR (CALCULATE_CREDIT_FLG = '1' AND COMB_ATTE_FLG = 'COMB') ";
        $query .= "         OR (CALCULATE_CREDIT_FLG = '2' AND COMB_ATTE_FLG = 'ATTE') ";
        $query .= "     GROUP BY SCHREGNO, SUBCLASSCD ";
        $query .= "     ) ";

        //各科目の単位数
        $query .= " ,CREDIT AS ( ";
        $query .= "     SELECT ";
        $query .= "         K2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         K1.CLASSCD || '-' || K1.SCHOOL_KIND || '-' || K1.CURRICULUM_CD || '-' || K1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "         K1.SUBCLASSCD, ";
        }
        $query .= "         K1.CREDITS ";
        $query .= "     FROM ";
        $query .= "         SCHNO K2 ";
        $query .= "     LEFT JOIN ";
        $query .= "         CREDIT_MST K1 ON  K1.YEAR       = K2.YEAR ";
        $query .= "                       AND K1.COURSECD   = K2.COURSECD ";
        $query .= "                       AND K1.MAJORCD    = K2.MAJORCD ";
        $query .= "                       AND K1.GRADE      = K2.GRADE ";
        $query .= "                       AND K1.COURSECODE = K2.COURSECODE ";
        $query .= "     ) ";

        //各生徒毎の科目と単位数
        //対象生徒が履修している科目数と合計単位数
        $query .= " ,CNT_SUB AS ( ";
        $query .= "     SELECT ";
        $query .= "         W3.SCHREGNO, ";
        $query .= "         COUNT(W3.SUBCLASSCD) AS CNT_SUB, ";
        $query .= "         SUM(W2.CREDITS)      AS CNT_CRE ";
        $query .= "     FROM ";
        $query .= "         CHAIR W3 ";
        $query .= "     LEFT JOIN ";
        $query .= "         CREDIT W2 ON  W2.SCHREGNO   = W3.SCHREGNO ";
        $query .= "                   AND W2.SUBCLASSCD = W3.SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "         W3.SCHREGNO ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.NAME_SHOW, ";
        $query .= "     '{$model->obj_schoolcd}' AS SCHOOLCD, ";
        $query .= "     '{$model->obj_majorcd}'  AS MAJORCD, ";
        $query .= "     Fiscalyear(T3.ENT_DATE)  AS NENDO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '{$model->obj_saiken}'   AS SAIKEN, ";
        $query .= "     T2.CNT_SUB, ";
        $query .= "     T2.CNT_CRE, ";
        if ($model->obj_radio == '4') { //選択が定時制ならば
            $query .= " '0' AS FLG ";
        } else { //選択が通信制ならば
            $query .= " CASE WHEN T1.COURSECD = '1' ";
            $query .= "      THEN '1' ";
            $query .= "      WHEN T1.COURSECD = '2' ";
            $query .= "      THEN '0' ";
            $query .= "      ELSE '' ";
            $query .= " END  AS FLG ";
        }
        $query .= " FROM ";
        $query .= "     SCHNO T1 ";
        $query .= " INNER JOIN ";
        $query .= "     CNT_SUB T2         ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //ＣＳＶ出力---授業料(生徒情報)
    function getOutputQueryCsv2($model) {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         GRADE, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '{$model->obj_year}' ";
        $query .= "     AND SEMESTER = '{$model->obj_semester}' ";

        $query .= " ), SUBMAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS SUBCLASSCD, ";
            $query .= "         L2.COMBINED_CLASSCD || '-' || L2.COMBINED_SCHOOL_KIND || '-' || L2.COMBINED_CURRICULUM_CD || '-' || L2.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
            $query .= "         L2.ATTEND_CLASSCD || '-' || L2.ATTEND_SCHOOL_KIND || '-' || L2.ATTEND_CURRICULUM_CD || '-' || L2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ";
            $query .= "         L2.CALCULATE_CREDIT_FLG, ";
            $query .= "         CASE WHEN (L1.CLASSCD       = L2.COMBINED_CLASSCD ";
            $query .= "                AND L1.SCHOOL_KIND   = L2.COMBINED_SCHOOL_KIND ";
            $query .= "                AND L1.CURRICULUM_CD = L2.COMBINED_CURRICULUM_CD ";
            $query .= "                AND L1.SUBCLASSCD    = L2.COMBINED_SUBCLASSCD) ";
            $query .= "              THEN 'COMB' ";
            $query .= "              WHEN (L1.CLASSCD       = L2.ATTEND_CLASSCD ";
            $query .= "                AND L1.SCHOOL_KIND   = L2.ATTEND_SCHOOL_KIND ";
            $query .= "                AND L1.CURRICULUM_CD = L2.ATTEND_CURRICULUM_CD ";
            $query .= "                AND L1.SUBCLASSCD    = L2.ATTEND_SUBCLASSCD) ";
            $query .= "              THEN 'ATTE' ";
            $query .= "              ELSE NULL ";
            $query .= "         END AS COMB_ATTE_FLG ";
        } else {
            $query .= "         L1.SUBCLASSCD, ";
            $query .= "         L2.COMBINED_SUBCLASSCD, ";
            $query .= "         L2.ATTEND_SUBCLASSCD, ";
            $query .= "         L2.CALCULATE_CREDIT_FLG, ";
            $query .= "         CASE WHEN L1.SUBCLASSCD = L2.COMBINED_SUBCLASSCD ";
            $query .= "             THEN 'COMB' ";
            $query .= "             WHEN L1.SUBCLASSCD = L2.ATTEND_SUBCLASSCD ";
            $query .= "             THEN 'ATTE' ";
            $query .= "             ELSE NULL ";
            $query .= "         END AS COMB_ATTE_FLG ";
        }
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         CHAIR_DAT L1 ON  L1.YEAR     = T1.YEAR ";
        $query .= "                      AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                      AND L1.CHAIRCD  = T1.CHAIRCD ";
        $query .= "                      AND SUBSTR(L1.SUBCLASSCD,1,2) <= '90' ";
        $query .= "                      AND SUBSTR(L1.SUBCLASSCD,5,1) = '{$model->obj_radio}' "; //4:定時制、5:通信制
        $query .= "     LEFT JOIN ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT L2 ON   L2.YEAR                = T1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                          AND (( L2.COMBINED_CLASSCD         = L1.CLASSCD ";
            $query .= "                                             AND L2.COMBINED_SCHOOL_KIND     = L1.SCHOOL_KIND ";
            $query .= "                                             AND L2.COMBINED_CURRICULUM_CD   = L1.CURRICULUM_CD ";
            $query .= "                                             AND L2.COMBINED_SUBCLASSCD      = L1.SUBCLASSCD) ";
            $query .= "                                          OR   ( L2.ATTEND_CLASSCD           = L1.CLASSCD ";
            $query .= "                                             AND L2.ATTEND_SCHOOL_KIND       = L1.SCHOOL_KIND ";
            $query .= "                                             AND L2.ATTEND_CURRICULUM_CD     = L1.CURRICULUM_CD ";
            $query .= "                                             AND L2.ATTEND_SUBCLASSCD        = L1.SUBCLASSCD)) ";
        } else {
            $query .= "                                          AND (L2.COMBINED_SUBCLASSCD = L1.SUBCLASSCD ";
            $query .= "                                          OR   L2.ATTEND_SUBCLASSCD   = L1.SUBCLASSCD) ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->obj_year}' ";

        $query .= " ),CHAIR AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN ";
        $query .= "     WHERE ";
        $query .= "             CALCULATE_CREDIT_FLG IS NULL ";
        $query .= "         OR (CALCULATE_CREDIT_FLG = '1' AND COMB_ATTE_FLG = 'COMB') ";
        $query .= "         OR (CALCULATE_CREDIT_FLG = '2' AND COMB_ATTE_FLG = 'ATTE') ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     '{$model->obj_schoolcd}' AS SCHOOLCD, ";
        $query .= "     '{$model->obj_majorcd}' AS MAJORCD, ";
        $query .= "     Fiscalyear(T2.ENT_DATE) AS NENDO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME_KANA, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.GUARD_KANA, ";
        $query .= "     T3.GUARD_NAME ";
        $query .= " FROM ";
        $query .= "     SCHNO T1  ";
        $query .= " LEFT JOIN  SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN  GUARDIAN_DAT    T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN CHAIR           T4 ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //チェック情報出力のCSVデータを作成
    function selectStudySubjectData($model) {
        $query .= " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '{$model->obj_year}' ";
        $query .= "     AND SEMESTER = '1' ";
        $query .= " ) ";

        $query .= " ,CHAIR_SCHNO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.GRADE, ";
        $query .= "         W1.HR_CLASS, ";
        $query .= "         W1.ATTENDNO, ";
        $query .= "         W2.YEAR, ";
        $query .= "         W2.SEMESTER, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || W2.SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "         W2.SUBCLASSCD ";
        }
        $query .= "     FROM ";
        $query .= "         SCHNO W1, ";
        $query .= "         CHAIR_DAT W2, ";
        $query .= "         CHAIR_STD_DAT W3 ";
        $query .= "     WHERE ";
        $query .= "             W2.YEAR     = W1.YEAR ";
        $query .= "         AND W2.SEMESTER = W1.SEMESTER ";
        $query .= "         AND SUBSTR(W2.SUBCLASSCD,1,2) <= '90' ";
        $query .= "         AND SUBSTR(W2.SUBCLASSCD,5,1) = '{$model->obj_radio}' "; //4:定時制、5:通信制
        $query .= "         AND W3.YEAR     = W1.YEAR ";
        $query .= "         AND W3.SEMESTER = W1.SEMESTER ";
        $query .= "         AND W3.CHAIRCD  = W2.CHAIRCD ";
        $query .= "         AND W3.SCHREGNO = W1.SCHREGNO ";
        $query .= " ) ";

        $query .= " ,CREDIT AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.GRADE, ";
        $query .= "         W1.HR_CLASS, ";
        $query .= "         W1.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || W2.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "         W2.SUBCLASSCD, ";
        }
        $query .= "         W2.CREDITS, ";
        $query .= "         W2.AUTHORIZE_FLG, ";
        $query .= "         W2.COMP_UNCONDITION_FLG ";
        $query .= "     FROM ";
        $query .= "         SCHNO W1 ";
        $query .= "     INNER JOIN ";
        $query .= "         CREDIT_MST W2 ON  W2.YEAR       = W1.YEAR ";
        $query .= "                       AND W2.COURSECD   = W1.COURSECD ";
        $query .= "                       AND W2.MAJORCD    = W1.MAJORCD ";
        $query .= "                       AND W2.GRADE      = W1.GRADE ";
        $query .= "                       AND W2.COURSECODE = W1.COURSECODE ";
        $query .= " ) ";

        //単位が固定なのか加算なのかのフラグと、その科目が親なのか子なのかを表示して
        //次のセレクト文で対象外の科目を排除する
        $query .= " ,SUBMAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.NAME_SHOW, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T4.SUBCLASSNAME, ";
        $query .= "     T2.CREDITS, ";
        $query .= "     T2.AUTHORIZE_FLG, ";
        $query .= "     T2.COMP_UNCONDITION_FLG, ";
        $query .= "     L1.CALCULATE_CREDIT_FLG, ";
        $query .= "     CASE WHEN T1.SUBCLASSCD = L1.COMBINED_SUBCLASSCD ";
        $query .= "         THEN 'COMB' ";
        $query .= "         WHEN T1.SUBCLASSCD = L1.ATTEND_SUBCLASSCD ";
        $query .= "         THEN 'ATTE' ";
        $query .= "         ELSE NULL ";
        $query .= "     END AS COMB_ATTE_FLG ";
        $query .= " FROM ";
        $query .= "     CHAIR_SCHNO T1 ";
        $query .= " LEFT JOIN CREDIT          T2 ON  T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "                              AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " LEFT JOIN SCHREG_BASE_MST T3 ON  T3.SCHREGNO   = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " LEFT JOIN SUBCLASS_MST    T4 ON  T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD = T1.SUBCLASSCD ";
        } else {
            $query .= " LEFT JOIN SUBCLASS_MST    T4 ON  T4.SUBCLASSCD = T1.SUBCLASSCD ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT L1 ON   L1.YEAR = T1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND (L1.COMBINED_CLASSCD || '-' || L1.COMBINED_SCHOOL_KIND || '-' || L1.COMBINED_CURRICULUM_CD || '-' || L1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                 OR   L1.ATTEND_CLASSCD || '-' || L1.ATTEND_SCHOOL_KIND || '-' || L1.ATTEND_CURRICULUM_CD || '-' || L1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
        } else {
            $query .= "                                      AND (L1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                                      OR   L1.ATTEND_SUBCLASSCD   = T1.SUBCLASSCD) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " ) ";

        //ここで合併があるものに関しては単位固定⇒合併先、単位加算⇒合併元に限定している
        $query .= " SELECT  ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     NAME_SHOW, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     CREDITS, ";
        $query .= "     AUTHORIZE_FLG, ";
        $query .= "     COMP_UNCONDITION_FLG ";
        $query .= " FROM ";
        $query .= "     SUBMAIN ";
        $query .= " WHERE ";
        $query .= "         CALCULATE_CREDIT_FLG IS NULL ";
        $query .= "     OR (CALCULATE_CREDIT_FLG = '1' AND COMB_ATTE_FLG = 'COMB') ";
        $query .= "     OR (CALCULATE_CREDIT_FLG = '2' AND COMB_ATTE_FLG = 'ATTE') ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     NAME_SHOW, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     CREDITS, ";
        $query .= "     AUTHORIZE_FLG, ";
        $query .= "     COMP_UNCONDITION_FLG ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }
}
?>
