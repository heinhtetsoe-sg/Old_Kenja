<?php

require_once('for_php7.php');

class knje390nQuery extends Query
{
/***************メイン画面***************/

    //生徒情報取得
    function getSchInfo($model)
    {
        //卒業生
        if ($model->mode == "grd") {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.HR_NAME, ";
            $query .= "     INT(T1.GRD_ATTENDNO) AS ATTENDNO, ";
            $query .= "     T1.NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     GRD_BASE_MST T1, ";
            $query .= "     GRD_REGD_HDAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "     FISCALYEAR(T1.GRD_DATE) = T2.YEAR AND ";
            $query .= "     T1.GRD_SEMESTER         = T2.SEMESTER AND ";
            $query .= "     T1.GRD_GRADE            = T2.GRADE AND ";
            $query .= "     T1.GRD_HR_CLASS         = T2.HR_CLASS ";

        //在学生
        } else {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T3.HR_NAME, ";
            $query .= "     INT(T1.ATTENDNO) AS ATTENDNO, ";
            $query .= "     T2.NAME_SHOW ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON ";
            $query .= "             T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON ";
            $query .= "             T1.YEAR     = T3.YEAR AND ";
            $query .= "             T1.SEMESTER = T3.SEMESTER AND ";
            $query .= "             T1.GRADE    = T3.GRADE AND ";
            $query .= "             T1.HR_CLASS = T3.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        }

        return $query;
    }

    //生徒詳細情報取得
    function getSchInfoShousai($model)
    {
        $query  = "  WITH SCHREG_ADDR AS (  ";
        $query .= "  SELECT ";
        $query .= "      S1.* ";
        $query .= "  FROM ";
        $query .= "      SCHREG_ADDRESS_DAT S1, ";
        $query .= "      (SELECT ";
        $query .= "          SCHREGNO, ";
        $query .= "          MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "       FROM ";
        $query .= "           SCHREG_ADDRESS_DAT  ";
        $query .= "       WHERE ";
        $query .= "           SCHREGNO = '".$model->schregno."' ";
        $query .= "       AND FISCALYEAR(ISSUEDATE) <= '".CTRL_YEAR."' ";
        $query .= "       GROUP BY ";
        $query .= "           SCHREGNO ) S2  ";
        $query .= "  WHERE ";
        $query .= "      S1.SCHREGNO = '".$model->schregno."'  ";
        $query .= "  AND S1.ISSUEDATE = S2.ISSUEDATE ";
        $query .= "  )  ";
        $query .= "  SELECT ";
        $query .= "      T1.SCHREGNO,  ";
        $query .= "      T3.SCHOOL_KIND,  ";
        $query .= "      N1.ABBV1 AS GAKUBU_NAME, ";
        $query .= "      V1.COURSENAME || V1.MAJORNAME AS COURSE_MAJOR_NAME, ";
        $query .= "      V2.COURSECODENAME, ";
        $query .= "      T4.HR_NAME, ";
        $query .= "      G2.GHR_NAME, ";
        $query .= "      T1.NAME_SHOW,  ";
        $query .= "      T1.NAME_KANA,  ";
        $query .= "      T1.BIRTHDAY,  ";
        $query .= "      N2.NAME2 AS SEX_NAME, ";
        $query .= "      T5.ZIPCD, ";
        $query .= "      T5.ADDR1, ";
        $query .= "      T5.ADDR2, ";
        $query .= "      T5.TELNO, ";
        $query .= "      T1.EMERGENCYTELNO, ";
        $query .= "      T1.EMERGENCYNAME, ";
        $query .= "      T1.EMERGENCYTELNO2, ";
        $query .= "      T1.EMERGENCYNAME2, ";
        $query .= "      T1.EMERGENCYTELNO3, ";
        $query .= "      T1.EMERGENCYNAME3 ";
        $query .= "  FROM ";
        $query .= "      V_SCHREG_BASE_MST T1 ";
        $query .= "      LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "      LEFT JOIN SCHREG_REGD_GDAT T3 ON T2.YEAR = T3.YEAR ";
        $query .= "                                   AND T2.GRADE = T3.GRADE ";
        $query .= "      LEFT JOIN SCHREG_REGD_HDAT T4 ON T2.YEAR = T4.YEAR ";
        $query .= "                                   AND T2.SEMESTER = T4.SEMESTER  ";
        $query .= "                                   AND T2.GRADE || T2.HR_CLASS = T4.GRADE || T4.HR_CLASS  ";
        $query .= "      LEFT JOIN SCHREG_ADDR T5 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "      LEFT JOIN SCHREG_REGD_GHR_DAT G1 ON G1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                                      AND G1.YEAR =T2.YEAR ";
        $query .= "                                      AND G1.SEMESTER = T2.SEMESTER ";
        $query .= "      LEFT JOIN SCHREG_REGD_GHR_HDAT G2 ON G2.YEAR = G1.YEAR ";
        $query .= "                                       AND G2.SEMESTER = G1.SEMESTER ";
        $query .= "                                       AND G2.GHR_CD = G1.GHR_CD ";
        $query .= "      LEFT JOIN V_COURSE_MAJOR_MST V1 ON T2.YEAR = V1.YEAR ";
        $query .= "                                     AND T2.COURSECD = V1.COURSECD  ";
        $query .= "                                     AND T2.MAJORCD = V1.MAJORCD  ";
        $query .= "      LEFT JOIN V_COURSECODE_MST V2 ON T2.YEAR = V2.YEAR ";
        $query .= "                                   AND T2.COURSECODE = V2.COURSECODE  ";
        $query .= "      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T2.YEAR ";
        $query .= "                             AND N1.NAMECD1 = 'A023' ";
        $query .= "                             AND N1.NAME1 = T3.SCHOOL_KIND ";
        $query .= "      LEFT JOIN V_NAME_MST N2 ON T2.YEAR = N2.YEAR ";
        $query .= "                             AND N2.NAMECD1 = 'Z002' ";
        $query .= "                             AND T1.SEX = N2.NAMECD2 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "  AND T2.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //部クラブ名取得
    function getClubName($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.CLUBNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.SDATE DESC, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //レコードの日付取得 (全て表示)
    function getMainYear($model, $flg)
    {
        if ($flg === 'C') {
            $settableName = "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT";
        } else if ($flg === 'A') {
            $settableName = "SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT";
        } else if ($flg === 'E') {
            $settableName = "SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT";
        } else if ($flg === 'F') {
            $settableName = "SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT";
        } else if ($flg === 'G') {
            $settableName = "SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT";
        } else {
            $settableName = "SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT";
        }
        $query  = "  SELECT DISTINCT ";
        $query .= "      YEAR AS VALUE, ";
        $query .= "      YEAR || '年度' AS LABEL ";
        $query .= "  FROM ";
        $query .= "      $settableName ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO = '".$model->schregno."' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //レコードの日付取得 (全て表示)
    function getRecordDate($model, $flg)
    {
        if ($flg === 'B') {
            $settableName = "SCHREG_CHALLENGED_PROFILE_MAIN_DAT";
        } else if ($flg === 'C') {
            $settableName = "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT";
            $setYear = $model->mainfield["YEAR_B"];
        } else if ($flg === 'D') {
            $settableName = "SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT";
            $setYear = $model->mainfield["YEAR_D"];
        } else if ($flg === 'A'){
            $settableName = "SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT";
            $setYear = $model->mainfield["YEAR_A"];
        } else if ($flg === 'E'){
            $settableName = "SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT";
            $setYear = $model->mainfield["YEAR_E"];
        } else if ($flg === 'F'){
            $settableName = "SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT";
            $setYear = $model->mainfield["YEAR_F"];
        } else if ($flg === 'G'){
            $settableName = "SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT";
            $setYear = $model->mainfield["YEAR_G"];
        } else {
            $settableName = "SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT";
            $setYear = $model->mainfield["YEAR_H"];
        }
        $query  = "  SELECT ";
        $query .= "      RECORD_DATE AS VALUE, ";
        $query .= "      CASE WHEN RECORD_DATE = 'NEW' THEN '最終更新' ELSE REPLACE(RECORD_DATE,'-','/') END AS LABEL ";
        $query .= "  FROM ";
        $query .= "      $settableName ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO = '".$model->schregno."' ";
        if ($flg !== 'B' && $flg !== 'D') {
            $query .= "  AND YEAR = '".$setYear."' ";
            if ($setYear == CTRL_YEAR) {
                $query .= "  AND RECORD_DATE <> 'NEW' ";
            }
        } else {
            $query .= "  AND RECORD_DATE <> 'NEW' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //レコードの日付取得 (全て表示)
    function getTorikomiRecordDate($model, $flg)
    {
        if ($flg === 'B') {
            $settableName = "SCHREG_CHALLENGED_PROFILE_MAIN_DAT";
        } else if ($flg === 'C') {
            $settableName = "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT";
        } else if ($flg === 'D') {
            $settableName = "SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT";
        } else {
            $settableName = "SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT";
        }
        
        if ($flg === 'A') {
            $query  = " SELECT ";
            $query .= "     MAX(RECORD_DATE) AS VALUE, ";
            if ($model->main_year != CTRL_YEAR) {
                $query .= "     '最終更新' AS LABEL ";
            } else {
                $query .= "     '最新' AS LABEL ";
            }
            $query .= " FROM ";
            $query .= "     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " AND YEAR = '".$model->main_year."' ";
            $query .= " UNION  ";
            $query .= " SELECT ";
            $query .= "      T1.RECORD_DATE AS VALUE, ";
            $query .= "      REPLACE(T1.RECORD_DATE,'-','/') AS LABEL ";
            $query .= "  FROM ";
            $query .= "      SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT T1 ";
            $query .= "  WHERE ";
            $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
            $query .= "  AND T1.YEAR = '".$model->main_year."' ";
            $query .= "  AND T1.RECORD_DATE NOT IN ( ";
            $query .= "                             SELECT ";
            $query .= "                                 MAX(M1.RECORD_DATE) ";
            $query .= "                             FROM ";
            $query .= "                                 SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT M1 ";
            $query .= "                             WHERE ";
            $query .= "                                 M1.SCHREGNO = T1.SCHREGNO ";
            $query .= "                             AND M1.YEAR = T1.YEAR ";
            $query .= "                            ) ";
            $query .= "  ORDER BY ";
            $query .= "      VALUE DESC ";
        } else {
            $query  = " SELECT ";
            $query .= "      RECORD_DATE AS VALUE, ";
            if (($model->main_year != CTRL_YEAR) && $flg === 'C') {
                $query .= "      CASE WHEN RECORD_DATE = 'NEW' THEN '最終更新' ELSE REPLACE(RECORD_DATE,'-','/') END AS LABEL ";
            } else {
                $query .= "      CASE WHEN RECORD_DATE = 'NEW' THEN '最新' ELSE REPLACE(RECORD_DATE,'-','/') END AS LABEL ";
            }
            $query .= "  FROM ";
            $query .= "      $settableName ";
            $query .= "  WHERE ";
            $query .= "      SCHREGNO = '".$model->schregno."' ";
            if ($flg === 'C') {
                $query .= "  AND YEAR = '".$model->main_year."' ";
            }
            $query .= "  ORDER BY ";
            $query .= "      VALUE DESC ";
        }

        return $query;
    }

    //更新データ一覧取得
    function selectQuery($model)
    {
        $query  = "  WITH B_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      'B-1' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) UPDATED, ";
        $query .= "      '1' AS KINDCD, ";
        $query .= "      '基本情報' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ'  END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_PROFILE_MAIN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'B-2' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) UPDATED, ";
        $query .= "      '1' AS KINDCD, ";
        $query .= "      '基本情報' AS KINDNAME, ";
        $query .= "      '教育歴' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'B-3' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) UPDATED, ";
        $query .= "      '1' AS KINDCD, ";
        $query .= "      '基本情報' AS KINDNAME, ";
        $query .= "      '医療' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'B-4' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) UPDATED, ";
        $query .= "      '1' AS KINDCD, ";
        $query .= "      '基本情報' AS KINDNAME, ";
        $query .= "      '健康管理' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'AB-5' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) UPDATED, ";
        $query .= "      '1' AS KINDCD, ";
        $query .= "      '基本情報' AS KINDNAME, ";
        $query .= "      '福祉' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'B-6' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) UPDATED, ";
        $query .= "      '1' AS KINDCD, ";
        $query .= "      '基本情報' AS KINDNAME, ";
        $query .= "      '労働' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_PROFILE_WORK_RECORD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        
        $query .= "  ), A_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      'A-1' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '2' AS KINDCD, ";
        $query .= "      'アセスメント表' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '作成データ' AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'A-1' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '2' AS KINDCD, ";
        $query .= "      'アセスメント表' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '作成データ' AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'A-2' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '2' AS KINDCD, ";
        $query .= "      'アセスメント表' AS KINDNAME, ";
        $query .= "      '検査' AS UPDATED_DATA, ";
        $query .= "      CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '作成データ' AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'A-3' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '2' AS KINDCD, ";
        $query .= "      'アセスメント表' AS KINDNAME, ";
        $query .= "      '教科等の実態' AS UPDATED_DATA, ";
        $query .= "      CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '作成データ' AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";

        $query .= "  ), C_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      'C-2' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '3' AS KINDCD, ";
        $query .= "      '支援計画' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' AND T1.YEAR <> '".CTRL_YEAR."' THEN CHAR(T1.YEAR) || '年度 最終更新データ' ";
        $query .= "      WHEN T1.RECORD_DATE = 'NEW' AND T1.YEAR = '".CTRL_YEAR."' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.YEAR, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'C-2' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '3' AS KINDCD, ";
        $query .= "      '支援計画' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' AND T1.YEAR <> '".CTRL_YEAR."' THEN CHAR(T1.YEAR) || '年度 最終更新データ' ";
        $query .= "      WHEN T1.RECORD_DATE = 'NEW' AND T1.YEAR = '".CTRL_YEAR."' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.YEAR, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        
        $query .= "  ), D_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      'D-2' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '4' AS KINDCD, ";
        $query .= "      '移行支援計画' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'D-2' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '4' AS KINDCD, ";
        $query .= "      '移行支援計画' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'D-3' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '4' AS KINDCD, ";
        $query .= "      '移行支援計画' AS KINDNAME, ";
        $query .= "      '発達検査' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'D-1' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '4' AS KINDCD, ";
        $query .= "      '移行支援計画' AS KINDNAME, ";
        $query .= "      'スムーズな移行に向けての支援(医療・地域・労働・家庭地域生活)' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  AND (T1.MEDICAL_GOAL_AFTER_GRADUATION IS NOT NULL ";
        $query .= "  OR  T1.MEDICAL_STATUS IS NOT NULL ";
        $query .= "  OR  T1.MEDICAL_SHORT_TERM_GOAL IS NOT NULL ";
        $query .= "  OR  T1.MEDICAL_TANGIBLE_SUPPORT IS NOT NULL ";
        $query .= "  OR  T1.WELFARE_GOAL_AFTER_GRADUATION IS NOT NULL ";
        $query .= "  OR  T1.WELFARE_STATUS IS NOT NULL ";
        $query .= "  OR  T1.WELFARE_SHORT_TERM_GOAL IS NOT NULL ";
        $query .= "  OR  T1.WELFARE_TANGIBLE_SUPPORT IS NOT NULL ";
        $query .= "  OR  T1.WORK_GOAL_AFTER_GRADUATION IS NOT NULL ";
        $query .= "  OR  T1.WORK_STATUS IS NOT NULL ";
        $query .= "  OR  T1.WORK_SHORT_TERM_GOAL IS NOT NULL ";
        $query .= "  OR  T1.WORK_TANGIBLE_SUPPORT IS NOT NULL ";
        $query .= "  OR  T1.COMMU_GOAL_AFTER_GRADUATION IS NOT NULL ";
        $query .= "  OR  T1.COMMU_STATUS IS NOT NULL ";
        $query .= "  OR  T1.COMMU_SHORT_TERM_GOAL IS NOT NULL ";
        $query .= "  OR  T1.COMMU_TANGIBLE_SUPPORT IS NOT NULL ";
        $query .= "  ) ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'D-4' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '4' AS KINDCD, ";
        $query .= "      '移行支援計画' AS KINDNAME, ";
        $query .= "      'スムーズな移行に向けての支援(相談の記録)' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CONSULTATION_RECORD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      'D-5' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '4' AS KINDCD, ";
        $query .= "      '移行支援計画' AS KINDNAME, ";
        $query .= "      '進路指導計画' AS UPDATED_DATA, ";
        $query .= "      CASE WHEN T1.RECORD_DATE = 'NEW' THEN '最新データ' ";
        $query .= "      ELSE CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '履歴データ' END AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        
        $query .= "  ), E_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      'E-1' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '5' AS KINDCD, ";
        $query .= "      'サポートブック' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '作成データ' AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        
        $query .= "  ), F_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      'F-1' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '6' AS KINDCD, ";
        $query .= "      '引継資料(担任)' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '作成データ' AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        
        $query .= "  ), G_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      'G-1' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '7' AS KINDCD, ";
        $query .= "      '引継資料(事業者)' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '作成データ' AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";
        
        $query .= "  ), H_DATA AS ( ";
        $query .= "  SELECT ";
        $query .= "      'H-1' AS SORT, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      MAX(T1.UPDATED) AS UPDATED, ";
        $query .= "      '8' AS KINDCD, ";
        $query .= "      '関係者間資料' AS KINDNAME, ";
        $query .= "      'メイン画面' AS UPDATED_DATA, ";
        $query .= "      CHAR(REPLACE(T1.RECORD_DATE,'-','/')) || '作成データ' AS UPDATED_RECORD_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  GROUP BY ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.RECORD_DATE ";

        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      B_DATA T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.UPDATED = (SELECT ";
        $query .= "                        MAX(M1.UPDATED) ";
        $query .= "                    FROM ";
        $query .= "                        B_DATA M1 ";
        $query .= "                    WHERE ";
        $query .= "                        T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "                    ) ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      A_DATA T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.UPDATED = (SELECT ";
        $query .= "                        MAX(M1.UPDATED) ";
        $query .= "                    FROM ";
        $query .= "                        A_DATA M1 ";
        $query .= "                    WHERE ";
        $query .= "                        T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "                    ) ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      C_DATA T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.UPDATED = (SELECT ";
        $query .= "                        MAX(M1.UPDATED) ";
        $query .= "                    FROM ";
        $query .= "                        C_DATA M1 ";
        $query .= "                    WHERE ";
        $query .= "                        T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "                    ) ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      D_DATA T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.UPDATED = (SELECT ";
        $query .= "                        MAX(M1.UPDATED) ";
        $query .= "                    FROM ";
        $query .= "                        D_DATA M1 ";
        $query .= "                    WHERE ";
        $query .= "                        T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "                    ) ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      E_DATA T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.UPDATED = (SELECT ";
        $query .= "                        MAX(M1.UPDATED) ";
        $query .= "                    FROM ";
        $query .= "                        E_DATA M1 ";
        $query .= "                    WHERE ";
        $query .= "                        T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "                    ) ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      F_DATA T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.UPDATED = (SELECT ";
        $query .= "                        MAX(M1.UPDATED) ";
        $query .= "                    FROM ";
        $query .= "                        F_DATA M1 ";
        $query .= "                    WHERE ";
        $query .= "                        T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "                    ) ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      G_DATA T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.UPDATED = (SELECT ";
        $query .= "                        MAX(M1.UPDATED) ";
        $query .= "                    FROM ";
        $query .= "                        G_DATA M1 ";
        $query .= "                    WHERE ";
        $query .= "                        T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "                    ) ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      H_DATA T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.UPDATED = (SELECT ";
        $query .= "                        MAX(M1.UPDATED) ";
        $query .= "                    FROM ";
        $query .= "                        H_DATA M1 ";
        $query .= "                    WHERE ";
        $query .= "                        T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "                    ) ";

        $query .= "  ORDER BY ";
        $query .= "      SORT ";
        return $query;
    }

/***************共通***************/
    
    //マスター一覧のリスト
    function getMasterList($model) {
        if ($model->cmd === 'challenged_master') {
            $settable = 'CHALLENGED_NAME_MST';
            $orderfield = 'CHALLENGECD';
        } else if ($model->cmd === 'challenged_training_master') {
            $settable = 'CHALLENGED_TRAINING_MST';
            $orderfield = 'NAMECD';
        } else if ($model->cmd === 'team_member_master') {
            $settable = 'TEAM_MEMBER_MST';
            $orderfield = 'MEMBERCD';
        } else if ($model->cmd === 'aftertime_need_service_master') {
            $settable = 'AFTERTIME_NEED_SERVICE_NAME_MST';
            $orderfield = 'NAMECD';
        } else if ($model->cmd === 'medical_care_master') {
            $settable = 'MEDICAL_CARE_NAME_MST';
            $orderfield = 'NAMECD';
        } else if ($model->cmd === 'medical_center') {
            $settable = 'MEDICAL_CENTER_MST';
            $orderfield = 'CENTERCD';
        } else if ($model->cmd === 'checkname_master') {
            $settable = 'CHECK_NAME_MST';
            $orderfield = 'CHECK_CD';
        }
        $query  = " SELECT ";
        if ($model->cmd === 'challenged_master') {
            $query .= "     * ";
        } else if ($model->cmd === 'challenged_training_master') {
            $query .= "     TRAINING_CONTENTS AS NAME ";
        } else if ($model->cmd === 'checkname_master') {
            $query .= "     CHECK_CD AS CODE, CHECK_NAME AS NAME ";
        } else {
            $query .= "     NAME ";
        }
        $query .= " FROM ";
        $query .= "     $settable ";
        $query .= " ORDER BY ";
        $query .= "     $orderfield ";

        return $query;
    }

    //福祉サービス一覧取得
    function getSearchWelfareService($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SERVICE_CENTERCD || '-' || T1.SERVICE_CENTERCD_EDABAN AS SERVICE_CENTERCD_SET, ";
        $query .= "     N1.NAME1 AS KENIKI_NAME, ";
        $query .= "     T1.NAME AS CENTER_NAME, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2 ";
        $query .= " FROM ";
        $query .= "     WELFARE_USE_SERVICE_CENTER_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E040' ";
        $query .= "                          AND N1.NAMECD2 = T1.AREACD ";
        $query .= " WHERE ";
        if ($model->searchfield["AREACD"]) {
            $query .= "     T1.AREACD = '{$model->searchfield["AREACD"]}' ";
        }
        if ($model->searchfield["AREACD"] && $model->searchfield["NAME"]) {
            $query .= " AND ";
        }
        if ($model->searchfield["NAME"]) {
            $query .= "     T1.NAME LIKE '%{$model->searchfield["NAME"]}%' ";
        }
        if (($model->searchfield["AREACD"] || $model->searchfield["NAME"]) && $model->searchfield["ADDR1"]) {
            $query .= " AND ";
        }
        if ($model->searchfield["ADDR1"]) {
            $query .= "     T1.ADDR1 LIKE '%{$model->searchfield["ADDR1"]}%' ";
        }
        if (($model->searchfield["AREACD"] || $model->searchfield["NAME"] || $model->searchfield["ADDR1"]) && $model->searchfield["ADDR2"]) {
            $query .= " AND ";
        }
        if ($model->searchfield["ADDR2"]) {
            $query .= "     T1.ADDR2 LIKE '%{$model->searchfield["ADDR2"]}%' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      T1.AREACD, ";
        $query .= "      T1.NAME ";

        return $query;
    }

    //検査機関取得
    function getSearchCheckCenter($model)
    {
        $query  = " SELECT ";
        $query .= "     N1.NAME1 AS KENIKI_NAME, ";
        $query .= "     T1.NAME AS CENTER_NAME, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2 ";
        $query .= " FROM ";
        $query .= "     CHECK_CENTER_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E040' ";
        $query .= "                          AND N1.NAMECD2 = T1.AREACD ";
        $query .= " WHERE ";
        if ($model->searchfield["AREACD"]) {
            $query .= "     T1.AREACD = '{$model->searchfield["AREACD"]}' ";
        }
        if ($model->searchfield["AREACD"] && $model->searchfield["NAME"]) {
            $query .= " AND ";
        }
        if ($model->searchfield["NAME"]) {
            $query .= "     T1.NAME LIKE '%{$model->searchfield["NAME"]}%' ";
        }
        if (($model->searchfield["AREACD"] || $model->searchfield["NAME"]) && $model->searchfield["ADDR1"]) {
            $query .= " AND ";
        }
        if ($model->searchfield["ADDR1"]) {
            $query .= "     T1.ADDR1 LIKE '%{$model->searchfield["ADDR1"]}%' ";
        }
        if (($model->searchfield["AREACD"] || $model->searchfield["NAME"] || $model->searchfield["ADDR1"]) && $model->searchfield["ADDR2"]) {
            $query .= " AND ";
        }
        if ($model->searchfield["ADDR2"]) {
            $query .= "     T1.ADDR2 LIKE '%{$model->searchfield["ADDR2"]}%' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      T1.AREACD, ";
        $query .= "      T1.NAME ";

        return $query;
    }

    //担任名称
    function getStaffName($model) {
        $query .= " SELECT ";
        $query .= "   STAFFNAME ";
        $query .= " FROM ";
        $query .= "   STAFF_MST ";
        $query .= " WHERE ";
        $query .= "   STAFFCD = '".STAFFCD."' ";
        return $query;
    }

    //クラス名称取得
    function getHrName($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE = '".$model->grade."' AND ";
        $query .= "     HR_CLASS = '".$model->hr_class."' ";

        return $query;
    }

    //学校情報を取得
    function getSchoolInfo($schoolcd) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.FINSCHOOL_NAME, ";
        $query .= "     T1.FINSCHOOL_DISTCD AS RITSUCD, ";
        $query .= "     L3.NAME1 AS RITSU ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                 AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOLCD = '{$schoolcd}' ";

        return $query;
    }

    //名称マスタ
    function getNameMst($cd)
    {
        $query  = "  SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        if ($cd === 'E031' || $cd === 'E032' || $cd === 'E033' || $cd === 'E034' || $cd === 'E035' || $cd === 'E063') {
            $query .= "      NAME1 AS LABEL ";
        } else {
            $query .= "      NAMECD2 || ':' || NAME1 AS LABEL ";
        }
        $query .= "  FROM ";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD1 = '".$cd."' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }
    
    //続柄名取得
    function getTsuzukigaraName($cd)
    {
        $query  = "  SELECT ";
        $query .= "      NAME1 ";
        $query .= "  FROM ";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD1 = 'H201' ";
        $query .= "  AND NAMECD2 = '".$cd."' ";

        return $query;
    }

    //学級種別名取得
    function getClassShubetsuName($classShubetsu)
    {
        $query  = "  SELECT ";
        $query .= "      NAME1 ";
        $query .= "  FROM ";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD1 = 'E038' ";
        $query .= "  AND NAMECD2 = '".$classShubetsu."' ";

        return $query;
    }
    
    //診療科名(コンボ)を取得
    function getNamecdComboName()
    {
        $query  = "  SELECT ";
        $query .= "      NAMECD AS VALUE, ";
        $query .= "      NAMECD || ':' || NAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      MEDICAL_DEPARTMENTS_MST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }
    
    //診療科名を取得
    function getNamecdName($namecd)
    {
        $query  = "  SELECT ";
        $query .= "      NAME ";
        $query .= "  FROM ";
        $query .= "      MEDICAL_DEPARTMENTS_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD = '".$namecd."' ";

        return $query;
    }

    //医療機関名を取得
    function getCentercdName($centercd)
    {
        $query  = "  SELECT ";
        $query .= "      NAME ";
        $query .= "  FROM ";
        $query .= "      MEDICAL_CENTER_MST ";
        $query .= "  WHERE ";
        $query .= "      CENTERCD = '".$centercd."' ";

        return $query;
    }

    //(コンボ表示制御用)補装具、日常生活用具名(コンボ)を取得
    function getItemdivComboName($flg)
    {
        if ($flg === '1') {
            $query  = "  SELECT ";
            $query .= "      CHALLENGECD AS VALUE, ";
            $query .= "      CHALLENGECD || ':' || NAME AS LABEL ";
            $query .= "  FROM ";
            $query .= "      CHALLENGED_NAME_MST ";
            $query .= "  WHERE ";
            $query .= "      CHALLENGECD IN ( ";
            $query .= "         SELECT ";
            $query .= "             DIV ";
            $query .= "         FROM ";
            $query .= "             MEDICAL_PROSTHETICS_NAME_MST ";
            $query .= "         GROUP BY ";
            $query .= "             DIV ";
            $query .= "      ) ";
            $query .= "  ORDER BY ";
            $query .= "      VALUE ";
        } else if ($flg === '2') {
            $query  = "  SELECT ";
            $query .= "      NAMECD2 AS VALUE, ";
            $query .= "      NAMECD2 || ':' || NAME1 AS LABEL ";
            $query .= "  FROM ";
            $query .= "      V_NAME_MST ";
            $query .= "  WHERE ";
            $query .= "      YEAR = '".CTRL_YEAR."' ";
            $query .= "      AND NAMECD1 = 'E062' ";
            $query .= "  ORDER BY ";
            $query .= "      VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     NAMESPARE1 AS VALUE, ";
            $query .= "     CASE NAMESPARE1 WHEN '1' THEN '自立支援給付' ";
            $query .= "                     WHEN '2' THEN '児童福祉法' END AS LABEL ";
            $query .= " FROM ";
            $query .= "     (SELECT DISTINCT ";
            $query .= "         NAMESPARE1 ";
            $query .= "     FROM ";
            $query .= "         V_NAME_MST ";
            $query .= "     WHERE ";
            $query .= "         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "         NAMECD1 = 'E039' AND ";
            $query .= "         VALUE(NAMESPARE1,'') != '' ";
            $query .= "     ) T1 ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //補装具、日常生活用具名(コンボ)を取得
    function getItemcdComboName($flg, $itemdiv)
    {
        if ($flg == "3") {
            $query  = " SELECT ";
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAME1 ";
            $query .= " FROM ";
            $query .= "     V_NAME_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "     NAMECD1 = 'E039' ";
            if ($itemdiv) {
                $query .= " AND NAMESPARE1 = '".$itemdiv."' ";
            }
        } else {
            $query  = " SELECT ";
            $query .= "     NAMECD AS VALUE, ";
            $query .= "     NAMECD || ':' || NAME AS LABEL ";
            $query .= " FROM ";
            if ($flg === '1') {
                $query .= "     MEDICAL_PROSTHETICS_NAME_MST ";
            } else {
                $query .= "     MEDICAL_DAILYWANTS_NAME_MST ";
            }
            if ($itemdiv) {
                $query .= " WHERE ";
                $query .= "     DIV = '".$itemdiv."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //補装具、日常生活用具名を取得
    function getItemcdName($flg)
    {
        $query  = "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        if ($flg === '1') {
            $query .= "      MEDICAL_PROSTHETICS_NAME_MST ";
        } else {
            $query .= "      MEDICAL_DAILYWANTS_NAME_MST ";
        }

        return $query;
    }
    //サービス利用事業所(コンボ)を取得
    function getWelfareServiceCentercdComboName()
    {
        $query  = "  SELECT ";
        $query .= "      SERVICE_CENTERCD || '-' || SERVICE_CENTERCD_EDABAN AS VALUE, ";
        $query .= "      SERVICE_CENTERCD || '-' || SERVICE_CENTERCD_EDABAN || ':' || NAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      WELFARE_USE_SERVICE_CENTER_MST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //サービス利用事業所を取得
    function getWelfareServiceCentercdName($service_centercd, $service_centercd_edaban)
    {
        $query  = "  SELECT ";
        $query .= "      NAME ";
        $query .= "  FROM ";
        $query .= "      WELFARE_USE_SERVICE_CENTER_MST ";
        $query .= "  WHERE ";
        $query .= "      SERVICE_CENTERCD = '".$service_centercd."' ";
        $query .= "  AND SERVICE_CENTERCD_EDABAN = '".$service_centercd_edaban."' ";

        return $query;
    }

    //福祉事業所(コンボ)を取得
    function getWelfareCentercdComboName($areacd)
    {
        $query  = "  SELECT ";
        $query .= "      CENTERCD AS VALUE, ";
        $query .= "      CENTERCD || ':' || NAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      WELFARE_ADVICE_CENTER_MST ";
        if (strlen($areacd)) {
            $query .= "  WHERE ";
            $query .= "      AREACD = '".$areacd."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //福祉事業所名を取得
    function getWelfareCentercdName($centercd)
    {
        $query  = "  SELECT ";
        $query .= "      NAME ";
        $query .= "  FROM ";
        $query .= "      WELFARE_ADVICE_CENTER_MST ";
        $query .= "  WHERE ";
        $query .= "      CENTERCD = '".$centercd."' ";

        return $query;
    }

    //労働相談事業所(コンボ)を取得
    function getAdviceCentercdComboName()
    {
        $query  = "  SELECT ";
        $query .= "      CENTERCD AS VALUE, ";
        $query .= "      CENTERCD || ':' || NAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      WORK_ADVICE_CENTER_MST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //労働相談事業所名を取得
    function getAdviceCentercdName($centercd)
    {
        $query  = "  SELECT ";
        $query .= "      NAME ";
        $query .= "  FROM ";
        $query .= "      WORK_ADVICE_CENTER_MST ";
        $query .= "  WHERE ";
        $query .= "      CENTERCD = '".$centercd."' ";

        return $query;
    }

    //検査機関名(コンボ)を取得
    function getCheckCentercdComboName()
    {
        $query  = "  SELECT ";
        $query .= "      CENTERCD AS VALUE, ";
        $query .= "      CENTERCD || ':' || NAME AS LABEL ";
        $query .= "  FROM ";
        $query .= "      CHECK_CENTER_MST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //検査機関名を取得
    function getCheckCentercdName($centercd)
    {
        $query  = "  SELECT ";
        $query .= "      NAME ";
        $query .= "  FROM ";
        $query .= "      CHECK_CENTER_MST ";
        $query .= "  WHERE ";
        $query .= "      CENTERCD = '".$centercd."' ";

        return $query;
    }

/*************** 家族構成 ********************/

    //家族構成を取得
    function getFamilyGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RELANO = '".$model->getRelano."' ";

        return $query;
    }

    //家族構成の一覧
    function getFamilyRecordList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RELANO ";

        return $query;
    }



/*************** 基本情報 ***************/

    //基本情報データ取得
    function getSubQuery1($model, $setSanshouData="")
    {

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_MAIN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if (!$setSanshouData) {
            $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        } else {
            $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";
        }

        return $query;
    }
    //手帳の障害名を取得
    function getChallengedCardNameMst() {
        $query  = "  SELECT ";
        $query .= "      CARDNAME_CD AS VALUE, ";
        $query .= "      CARDNAME_CD || ':' || CARDNAME AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      CHALLENGED_CARD_NAME_MST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }
    //受給者証の種別を取得
    function getChallengedCertifNameMst() {
        $query  = "  SELECT ";
        $query .= "      CERTIFNAME_CD AS VALUE, ";
        $query .= "      CERTIFNAME_CD || ':' || CERTIFNAME AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      CHALLENGED_CERTIF_NAME_MST ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

/**教育歴（SubForm1_2）**/
    //教育歴用の学校の種別を取得(固定)
    function getEducateRecordDiv() {
        $query  = "  SELECT ";
        $query .= "      '1' AS VALUE, ";
        $query .= "      '1' || ':' || '保育園' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '2' AS VALUE, ";
        $query .= "      '2' || ':' || '幼稚園' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '3' AS VALUE, ";
        $query .= "      '3' || ':' || '療育施設等' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '4' AS VALUE, ";
        $query .= "      '4' || ':' || '小学校' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '5' AS VALUE, ";
        $query .= "      '5' || ':' || '中学校' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '6' AS VALUE, ";
        $query .= "      '6' || ':' || '特別支援学校' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '7' AS VALUE, ";
        $query .= "      '7' || ':' || '高校' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '8' AS VALUE, ";
        $query .= "      '8' || ':' || '大学' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //名称マスタより取得(L007)
    function getNameMstL007() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L007' ";
        $query .= " ORDER BY ";
        $query .= "      NAMECD2 ";

        return $query;
    }

    //教育歴の一覧
    function getSubQuery1EducateRecordList($model, $entrydate)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.S_YEAR_MONTH, ";
        $query .= "     T1.E_YEAR_MONTH, ";
        $query .= "     T1.RECORD_DIV, ";
        $query .= "     T1.RECORD_NO, ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

    //教育歴の取得
    function getSubQuery1EducateGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV = '".$model->getRecordDiv."' ";
        $query .= " AND T1.RECORD_NO  = '".$model->getRecordNo."' ";
        $query .= " AND T1.RECORD_SEQ = ".$model->getRecordSeq." ";

        return $query;
    }

    //教育歴校種の名称マスタL019取得
    function getL019namecd2($recordDiv)
    {
        $query  = " SELECT ";
        $query .= "     MAX(NAMECD2) AS NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1= 'L019' ";
        $query .= " AND T1.NAMESPARE1 = '".$recordDiv."' ";

        return $query;
    }
 
/**医療（SubForm1_3）**/
    //医療の一覧
    function getSubQuery1MedicalRecordList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_DIV, ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

    //医療の取得
    function getSubQuery1MedicalGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV = '".$model->getRecordDiv."' ";
        $query .= " AND T1.RECORD_SEQ = ".$model->getRecordSeq." ";

        return $query;
    }

/**健康管理（SubForm1_4）**/
    //健康管理の一覧
    function getSubQuery1HealthcareRecordList($model, $recordDiv)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV = '".$recordDiv."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_DIV, ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

    //健康管理の取得
    function getSubQuery1HealthcareGetData($model, $recordDiv, $setSeq="")
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV = '".$recordDiv."' ";
        if ($recordDiv === '1' || $recordDiv === '2' || $setSeq != "") {
            $query .= " AND T1.RECORD_SEQ = ".$model->getRecordSeq." ";
        }

        return $query;
    }

    function getSubQuery1HealthcareRecordCnt($model, $recordDiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND RECORD_DIV = '".$recordDiv."' ";

        return $query;
    }

    //医療的ケアの名称を取得
    function getMedicalCareNameMst($namecd) {
        $query  = "  SELECT ";
        $query .= "      NAMECD AS VALUE, ";
        $query .= "      NAMECD || ':' || NAME AS LABEL, ";
        $query .= "      NAME ";
        $query .= "  FROM  ";
        $query .= "      MEDICAL_CARE_NAME_MST ";
        if ($namecd) {
            $query .= " WHERE NAMECD = '".$namecd."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

/**福祉（SubForm1_5）**/
    //福祉の種別を取得(固定)
    function getWelfareRecordDiv() {
        $query  = "  SELECT ";
        $query .= "      T1.NAMECD2 AS VALUE, ";
        $query .= "      T1.NAMECD2 || ':' || T2.NAME1 AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      V_NAME_MST T1 ";
        $query .= "      LEFT JOIN NAME_MST T2 ";
        $query .= "        ON T2.NAMECD1 = T1.NAMECD1 ";
        $query .= "       AND T2.NAMECD2 = T1.NAMECD2 ";
        $query .= "  WHERE  ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.NAMECD1 = 'E068' ";
        $query .= "  ORDER BY ";
        $query .= "      T1.NAMECD2 ";

        return $query;
    }
    
    //福祉の一覧
    function getSubQuery1WelfareRecordList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        if ($model->field["RECORD_DIV"] != "") {
            $query .= " AND T1.RECORD_DIV = '".$model->field["RECORD_DIV"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_DIV, ";
        $query .= "     T1.RECORD_NO, ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

    //福祉の項目データ一覧
    function getSubQuery1WelfareRecordItemcdList($model, $record_div, $record_no, $record_seq) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_ITEMCD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE  = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV   = '".$record_div."' ";
        $query .= " AND T1.RECORD_NO    = '".$record_no."' ";
        $query .= " AND T1.RECORD_SEQ   =  ".$record_seq." ";
        $query .= " ORDER BY ";
        $query .= "     T1.ITEMDIV ";

        return $query;
    }

    //福祉の取得
    function getSubQuery1WelfareGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.RECORD_DATE, ";
        $query .= "     T1.RECORD_DIV, ";
        $query .= "     T1.RECORD_NO, ";
        $query .= "     T1.RECORD_SEQ, ";
        $query .= "     T1.SERVICE_NAME, ";
        $query .= "     T1.ITEMCD, ";
        $query .= "     T1.CENTERCD, ";
        $query .= "     T1.SERVICE_CENTER_TEXT, ";
        $query .= "     T1.SERVICE_CENTERCD || '-' || T1.SERVICE_CENTERCD_EDABAN AS SERVICE_CENTERCD_SET, ";
        $query .= "     T1.SERVICE_CHARGE, ";
        $query .= "     T1.ITEMCD, ";
        $query .= "     T1.ITEMCD2, ";
        $query .= "     T1.SUPPLY_DATE, ";
        $query .= "     T1.WELFARE_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV = '".$model->getRecordDiv."' ";
        $query .= " AND T1.RECORD_NO  = '".$model->getRecordNo."' ";
        $query .= " AND T1.RECORD_SEQ = ".$model->getRecordSeq." ";

        return $query;
    }

    //福祉のレコード件数取得
    function getSubQuery1WelfareGetRecordCnt($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV = '".$model->field["RECORD_DIV"]."' ";

        return $query;
    }

    //福祉の項目データ取得
    function getSubQuery1WelfareGetItemcdData($model) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_ITEMCD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE  = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV   = '".$model->getRecordDiv."' ";
        $query .= " AND T1.RECORD_NO    = '".$model->getRecordNo."' ";
        $query .= " AND T1.RECORD_SEQ   =  ".$model->getRecordSeq." ";

        return $query;
    }

    //事業所から圏域を取得
    function getSubQuery1WelfareGetAreacd($centercd) {
        $query  = " SELECT ";
        $query .= "     AREACD ";
        $query .= " FROM ";
        $query .= "     WELFARE_ADVICE_CENTER_MST ";
        $query .= " WHERE ";
        $query .= "     CENTERCD = '".$centercd."' ";

        return $query;
    }

/**労働（SubForm1_6）**/
    //労働の一覧
    function getSubQuery1WorkRecordList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_WORK_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

    //労働の取得
    function getSubQuery1WorkGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_PROFILE_WORK_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_SEQ = ".$model->getRecordSeq." ";

        return $query;
    }

/** 生育歴 */
    //生育歴の取得
    function getSubQuery1GrowthGetData($model)
    {
        $query .= " SELECT ";
        $query .= "  REMARK1 ";
        $query .= " FROM ";
        $query .= "  SCHREG_CHALLENGED_PROFILE_OTHER_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "  SCHREGNO = '".$model->schregno."' ";
        $query .= "  AND RECORD_DATE = 'NEW' ";
        $query .= "  AND RECORD_DIV = '001' ";
        $query .= "  AND RECORD_SEQ = 1 ";

        return $query;
    }
/** 訓練機関 */
    //訓練機関の取得
    function getSubQuery1TrainingGetData($model)
    {
        $query .= " SELECT ";
        $query .= "  REMARK1 ";
        $query .= " FROM ";
        $query .= "  SCHREG_CHALLENGED_PROFILE_OTHER_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "  SCHREGNO = '".$model->schregno."' ";
        $query .= "  AND RECORD_DATE = 'NEW' ";
        $query .= "  AND RECORD_DIV = '002' ";
        $query .= "  AND RECORD_SEQ = 1 ";

        return $query;
    }
/** 視力・聴力 */
    //視力・聴力の取得
    function getSubQuery1HealthGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.R_BAREVISION, ";
        $query .= "     T1.L_BAREVISION, ";
        $query .= "     T1.R_VISION, ";
        $query .= "     T1.L_VISION, ";
        $query .= "     T1.R_EAR_DB, ";
        $query .= "     T1.L_EAR_DB, ";
        $query .= "     T2.DATE, ";
        $query .= "     T3.DET_REMARK1, ";
        $query .= "     T3.DET_REMARK2, ";
        $query .= "     T3.DET_REMARK3, ";
        $query .= "     T3.DET_REMARK4 ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DAT T1 ";
        $query .= "     LEFT JOIN MEDEXAM_HDAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "      AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_DET_DETAIL_DAT T3 ";
        $query .= "       ON T3.YEAR = T1.YEAR ";
        $query .= "      AND T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "      AND T3.DET_SEQ = '019' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR   = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO   = '".$model->schregno."' ";

        return $query;
    }

/***************A アセスメント表 ***************/

    //指定年度(またはログイン年度)のRECORD_DATEのMAXを取得
    function getMaxRecordDataQuery($model, $year)
    {
        $query  = "  SELECT ";
        $query .= "      MAX(RECORD_DATE) AS MAX_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO       = '".$model->schregno."' ";
        $query .= "  AND YEAR           = '".$year."' ";
        $query .= "  AND RECORD_DATE    <> 'NEW' ";
        $query .= "  GROUP BY ";
        $query .= "      SCHREGNO, ";
        $query .= "      YEAR ";

        return $query;
    }

    //項目名取得
    function getChallengedAssessmentStatusGrowupDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->main_year."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(DATA_DIV) ";

        return $query;
    }

    //受験報告データ（進学）取得
    function getSubQuery2($model, $setSanshouData="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.* ";
        for ($i = 1; $i <= $model->subFrm2_maxItemCnt; $i++) {
            $query .= "    ,L".$i.".STATUS AS DIV".$i."_STATUS ";
            $query .= "    ,L".$i.".GROWUP AS DIV".$i."_GROWUP ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT T1 ";
        for ($i = 1; $i <= $model->subFrm2_maxItemCnt; $i++) {
            $query .= "     LEFT JOIN SCHREG_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT L".$i." ";
            $query .= "          ON L".$i.".YEAR        = T1.YEAR ";
            $query .= "         AND L".$i.".SCHREGNO    = T1.SCHREGNO ";
            $query .= "         AND L".$i.".RECORD_DATE = T1.RECORD_DATE ";
            $query .= "         AND L".$i.".DATA_DIV    = '".$i."' ";
        }
        $query .= " WHERE ";
        if ($model->cmd == "subform2_yomikomi") {
            $query .= "     T1.YEAR = '".$model->field2["YOMIKOMI_YEAR"]."' ";
        } else {
            $query .= "     T1.YEAR = '".$model->main_year."' ";
        }
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        if (!$setSanshouData) {
            $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        } else {
            $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";
        }

        return $query;
    }

/**発達検査（SubForm2_2）**/
    //発達検査の一覧
    function getSubQuery2CheckRecordList($model, $setSanshouData="")
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->main_year."' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        if (!$setSanshouData) {
            $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        } else {
            $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

    //発達検査の取得
    function getSubQuery2CheckGetData($model, $setSanshouData="")
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = '".$model->getYear."' ";
        $query .= " AND T1.SCHREGNO   = '".$model->schregno."' ";
        if (!$setSanshouData) {
            $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        } else {
            $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";
        }
        $query .= " AND T1.RECORD_SEQ = ".$model->getRecordSeq." ";

        return $query;
    }


/**教科等の実態（SubForm2_3）**/
    function getSubQuery2GradeKindSchregGroupDat($model, $setSanshouData = "") {

        $query .= " SELECT DISTINCT ";
        $query .= "   SCH.YEAR ";
        $query .= " , SCH.SEMESTER ";
        $query .= " , SCH.SCHREGNO ";

        $query .= " , COMP.CLASSCD ";
        $query .= " , COMP.SCHOOL_KIND ";
        $query .= " , COMP.CURRICULUM_CD ";
        $query .= " , COMP.SUBCLASSCD ";
        $query .= " , SUB.SUBCLASSNAME ";
        $query .= " , SUB.SUBCLASSABBV ";

        $query .= " , COMP.CLASSCD || '-' || COMP.SCHOOL_KIND || '-' || COMP.CURRICULUM_CD || '-' || COMP.SUBCLASSCD VALUE";
        $query .= " , COMP.CLASSCD || '-' || COMP.SCHOOL_KIND || '-' || COMP.CURRICULUM_CD || '-' || COMP.SUBCLASSCD || ':' || SUB.SUBCLASSNAME LABEL ";

        $query .= " FROM GRADE_KIND_SCHREG_GROUP_DAT SCH ";
        $query .= " INNER JOIN GRADE_KIND_COMP_GROUP_DAT COMP ";
        $query .= "   ON SCH.YEAR = COMP.YEAR ";
        $query .= "   AND SCH.SEMESTER = COMP.SEMESTER ";
        $query .= "   AND SCH.GAKUBU_SCHOOL_KIND = COMP.GAKUBU_SCHOOL_KIND ";
        $query .= "   AND SCH.GHR_CD = COMP.GHR_CD ";
        $query .= "   AND SCH.GRADE = COMP.GRADE ";
        $query .= "   AND SCH.HR_CLASS = COMP.HR_CLASS ";
        $query .= "   AND SCH.CONDITION = COMP.CONDITION ";
        $query .= "   AND SCH.GROUPCD = COMP.GROUPCD ";

        $query .= " INNER JOIN SUBCLASS_MST SUB ";
        $query .= "   ON COMP.CLASSCD = SUB.CLASSCD ";
        $query .= "   AND COMP.SCHOOL_KIND = SUB.SCHOOL_KIND ";
        $query .= "   AND COMP.CURRICULUM_CD = SUB.CURRICULUM_CD ";
        $query .= "   AND COMP.SUBCLASSCD = SUB.SUBCLASSCD ";

        $query .= " WHERE SCH.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SCH.SEMESTER = '9' ";
        $query .= "   AND SCH.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //教科等の実態の取得(履歴用)
    function getSubQuery2StatusSubclassList($model, $setSanshouData = "") {

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || SUB.SUBCLASSNAME LABEL ";
        $query .= " FROM SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT T1 ";
        $query .= " INNER JOIN SUBCLASS_MST SUB ";
        $query .= "   ON T1.CLASSCD = SUB.CLASSCD ";
        $query .= "   AND T1.SCHOOL_KIND = SUB.SCHOOL_KIND ";
        $query .= "   AND T1.CURRICULUM_CD = SUB.CURRICULUM_CD ";
        $query .= "   AND T1.SUBCLASSCD = SUB.SUBCLASSCD ";

        $query .= " WHERE T1.YEAR       = '".$model->main_year."' ";
        $query .= "   AND T1.SCHREGNO   = '".$model->schregno."' ";
        if (!$setSanshouData) {
            $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        } else {
            $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";

        return $query;
    }

    //教科等の実態の取得
    function getSubQuery2StatusSubclassData($model, $setSanshouData = "") {

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT T1 ";
        $query .= " WHERE T1.YEAR       = '".$model->getYear."' ";
        $query .= "   AND T1.SCHREGNO   = '".$model->schregno."' ";
        if (!$setSanshouData) {
            $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        } else {
            $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";
        }

        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode('-', $model->field2['SUBCLASS']);
        $query .= "   AND T1.CLASSCD = '".$classCd."' ";
        $query .= "   AND T1.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "   AND T1.CURRICULUM_CD = '".$curriculumCd."' ";
        $query .= "   AND T1.SUBCLASSCD = '".$subclassCd."' ";

        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";

        return $query;
    }


    /***************支援計画***************/

    //項目名取得
    function getChallengedSupportplanStatusDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_SUPPORTPLAN_STATUS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->main_year."' ";
        $query .= " ORDER BY ";
        $query .= "     DATA_DIV ";

        return $query;
    }

    //支援計画データ取得
    function getSubQuery3($model, $maxDataDiv)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        if ($maxDataDiv > 0) {
            for ($i = 1; $i <= $maxDataDiv; $i++) {
                $query .= "    ,L".$i.".STATUS AS DIV".$i."_STATUS ";
                $query .= "    ,L".$i.".STATUS2 AS DIV".$i."_STATUS2 ";
                $query .= "    ,L".$i.".STATUS3 AS DIV".$i."_STATUS3 ";
            }
        }
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT T1 ";
        if ($maxDataDiv > 0) {
            for ($i = 1; $i <= $maxDataDiv; $i++) {
                $query .= "     LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT L".$i." ";
                $query .= "          ON L".$i.".YEAR        = T1.YEAR ";
                $query .= "         AND L".$i.".SCHREGNO    = T1.SCHREGNO ";
                $query .= "         AND L".$i.".RECORD_DATE = T1.RECORD_DATE ";
                $query .= "         AND L".$i.".DATA_DIV    = '".$i."' ";
            }
        }
        $query .= " WHERE ";
        if ($model->cmd == "subform3_yomikomi") {
            $query .= "     T1.YEAR         = '".$model->field3["YOMIKOMI_YEAR"]."' ";
        } else {
            $query .= "     T1.YEAR         = '".$model->main_year."' ";
        }
        $query .= " AND T1.SCHREGNO     = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE  = '".$model->record_date."' ";

        return $query;
    }


/***************D 移行支援計画***************/

    //移行支援計画データ取得
    function getSubQuery4($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.STATUS AS LIFESTYLE_STATUS, ";
        $query .= "     L2.STATUS AS SOCIALITY_STATUS, ";
        $query .= "     L3.STATUS AS COMMUNICATION_STATUS, ";
        $query .= "     L4.STATUS AS PHYSICAL_ACTIVITY_STATUS, ";
        $query .= "     L5.STATUS AS STUDY_STATUS, ";
        $query .= "     L6.STATUS AS INTERESTING_STATUS ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                     AND L1.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                     AND L1.DATA_DIV = '01' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                     AND L2.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                     AND L2.DATA_DIV = '02' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                     AND L3.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                     AND L3.DATA_DIV = '03' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT L4 ON L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                     AND L4.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                     AND L4.DATA_DIV = '04' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT L5 ON L5.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                     AND L5.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                     AND L5.DATA_DIV = '05' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT L6 ON L6.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                     AND L6.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                     AND L6.DATA_DIV = '06' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";

        return $query;
    }

/**発達検査（SubForm4_2）**/
    //発達検査の一覧
    function getSubQuery4CheckRecordList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

    //発達検査の取得
    function getSubQuery4CheckGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_SEQ = ".$model->getRecordSeq." ";

        return $query;
    }

/**スムーズな以降に向けての支援（SubForm4_3）**/
    //種別を取得(固定)
    function getSmoothDataDiv() {
        $query  = "  SELECT ";
        $query .= "      '1' AS VALUE, ";
        $query .= "      '1' || ':' || '医療・福祉・労働・家庭地域生活' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '2' AS VALUE, ";
        $query .= "      '2' || ':' || '相談の記録' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }


    //医療・福祉・労働・家庭地域生活の一覧（テーブルはmain）
    function getSubQuery4MainGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";

        return $query;
    }
    
    //相談の記録の取得
    function getSubQuery4ConsultationGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CONSULTATION_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_SEQ = ".$model->getRecordSeq." ";

        return $query;
    }

    //相談の記録の一覧
    function getSubQuery4ConsultationRecordList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CONSULTATION_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }

/**進路指導計画（SubForm4_4）**/
    //進路指導計画の種別を取得(固定)
    function getCareerguidanceRecordDiv() {
        $query  = "  SELECT ";
        $query .= "      '1' AS VALUE, ";
        $query .= "      '1' || ':' || '相談懇談' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '2' AS VALUE, ";
        $query .= "      '2' || ':' || '校内実習' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '3' AS VALUE, ";
        $query .= "      '3' || ':' || '職場見学' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '4' AS VALUE, ";
        $query .= "      '4' || ':' || '職場実習' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '5' AS VALUE, ";
        $query .= "      '5' || ':' || '年間の成果、課題' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '6' AS VALUE, ";
        $query .= "      '6' || ':' || '決定進路内容' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //進路指導計画の一覧
    function getSubQuery4CareerguidanceRecordList($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV = '".$model->field4["RECORD_DIV"]."' ";
        if ($model->field4["RECORD_DIV"] === '6') {
            if (!$flg) {
                $query .= " AND T1.RECORD_NO <> '3' ";
            } else {
                $query .= " AND T1.RECORD_NO  = '3' ";
                $query .= " AND T1.RECORD_SEQ =  1 ";
            }
        } 
        $query .= " ORDER BY ";
        $query .= "     T1.RECORD_NO, ";
        $query .= "     T1.RECORD_SEQ ";

        return $query;
    }
    
    //進路指導計画の取得
    function getSubQuery4CareerguidanceGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV = '".$model->getRecordDiv."' ";
        $query .= " AND T1.RECORD_NO  = '".$model->getRecordNo."' ";
        $query .= " AND T1.RECORD_SEQ = ".$model->getRecordSeq." ";
        
        return $query;
    }

    //進路指導計画(年間の成果、課題)の取得
    function getSubQuery4CareerguidanceResultChallengeGetData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CAREER_GUIDANCE_RESULT, ";
        $query .= "     T1.CAREER_GUIDANCE_CHALLENGE ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO   = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND T1.RECORD_DIV = '".$model->field4["RECORD_DIV"]."' ";
        $query .= " AND T1.RECORD_NO  = '1' ";
        $query .= " AND T1.RECORD_SEQ =  1 ";

        return $query;
    }

/***************サポートブック***************/
    //指定年度(またはログイン年度)のRECORD_DATEのMAXを取得
    function getMaxRecordData5Query($model)
    {
        $query  = "  SELECT ";
        $query .= "      MAX(RECORD_DATE) AS MAX_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO       = '".$model->schregno."' ";
        $query .= "  AND YEAR           = '".$model->main_year."' ";
        $query .= "  AND RECORD_DATE    <> 'NEW' ";
        $query .= "  GROUP BY ";
        $query .= "      SCHREGNO, ";
        $query .= "      YEAR ";

        return $query;
    }

    //サポートブック取得
    function getSubQuery5($model, $setSanshouData="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->main_year."' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        if (!$setSanshouData) {
            $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        } else {
            $query .= " AND T1.RECORD_DATE = '".$setSanshouData."' ";
        }

        return $query;
    }

/***************引継資料(担任)***************/
    //指定年度(またはログイン年度)のRECORD_DATEのMAXを取得
    function getMaxRecordData6Query($model)
    {
        $query  = "  SELECT ";
        $query .= "      MAX(RECORD_DATE) AS MAX_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO       = '".$model->schregno."' ";
        $query .= "  AND YEAR           = '".$model->main_year."' ";
        $query .= "  AND RECORD_DATE    <> 'NEW' ";
        $query .= "  GROUP BY ";
        $query .= "      SCHREGNO, ";
        $query .= "      YEAR ";

        return $query;
    }
    //引継資料(担任)データ取得
    function getSubQuery6($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.CAN_BE_NO_SUPPORT, ";
        $query .= "     L1.CAN_BE_SOME_SUPPORT, ";
        $query .= "     L1.MEANS, ";
        $query .= "     L1.SHORT_TERM_GOAL, ";
        $query .= "     L1.GOAL_FUTURE ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_TEACHER_TAKEOVER_DOCUMENTS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                  AND L1.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                  AND L1.DATA_DIV = '".$model->field6["DATA_DIV"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        
        return $query;
    }
    
/***************引継資料(事業者)***************/
    //指定年度(またはログイン年度)のRECORD_DATEのMAXを取得
    function getMaxRecordData7Query($model)
    {
        $query  = "  SELECT ";
        $query .= "      MAX(RECORD_DATE) AS MAX_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO       = '".$model->schregno."' ";
        $query .= "  AND YEAR           = '".$model->main_year."' ";
        $query .= "  AND RECORD_DATE    <> 'NEW' ";
        $query .= "  GROUP BY ";
        $query .= "      SCHREGNO, ";
        $query .= "      YEAR ";

        return $query;
    }
    //引継資料(事業者)データ取得
    function getSubQuery7($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.OUTLINE, ";
        $query .= "     L1.MEASURE, ";
        $query .= "     L1.FUTURE_DEVELOPMENT, ";
        $query .= "     L1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_OPERATOR_TAKEOVER_DOCUMENTS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                  AND L1.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                  AND L1.DATA_DIV = '".$model->field7["DATA_DIV"]."' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        
        return $query;
    }

/***************関係者間資料***************/
    //時間(時)を取得
    function getTimeHour() {
        for ($i=0; $i < 25; $i++) {
            if ($i == 0) {
                $query  = "  SELECT ";
            } else {
                $query  .= "  UNION SELECT ";
            }
            $query .= "      '".sprintf("%02d", $i)."' AS VALUE, ";
            $query .= "      '".sprintf("%02d", $i)."' AS LABEL ";
            $query .= "  FROM  ";
            $query .= "      SYSIBM.SYSDUMMY1 ";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }
    
    //時間(分)を取得
    function getTimeMinutes() {
        $query  = "  SELECT ";
        $query .= "      '00' AS VALUE, ";
        $query .= "      '00' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '15' AS VALUE, ";
        $query .= "      '15' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '30' AS VALUE, ";
        $query .= "      '30' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  UNION ";
        $query .= "  SELECT ";
        $query .= "      '45' AS VALUE, ";
        $query .= "      '45' AS LABEL ";
        $query .= "  FROM  ";
        $query .= "      SYSIBM.SYSDUMMY1 ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //指定年度(またはログイン年度)のRECORD_DATEのMAXを取得
    function getMaxRecordData8Query($model)
    {
        $query  = "  SELECT ";
        $query .= "      MAX(RECORD_DATE) AS MAX_DATE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO       = '".$model->schregno."' ";
        $query .= "  AND YEAR           = '".$model->main_year."' ";
        $query .= "  AND RECORD_DATE    <> 'NEW' ";
        $query .= "  GROUP BY ";
        $query .= "      SCHREGNO, ";
        $query .= "      YEAR ";

        return $query;
    }

    //関係者間資料データ取得
    function getSubQuery8($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.DOCUMENTS AS PROBLEM_EVENT_DOCUMENTS, ";
        $query .= "     L2.DOCUMENTS AS PROBLEM_ANALYSIS_DOCUMENTS, ";
        $query .= "     L3.DOCUMENTS AS PROBLEM_DEAL_DOCUMENTS, ";
        $query .= "     L4.DOCUMENTS AS STAFF_ROLE_DOCUMENTS, ";
        $query .= "     L5.DOCUMENTS AS GUARDIAN_COOPERATION_DOCUMENTS, ";
        $query .= "     L6.DOCUMENTS AS WELFARE_COOPERATION_DOCUMENTS, ";
        $query .= "     L7.DOCUMENTS AS OTHERS_REMARK_DOCUMENTS ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                                AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                AND L1.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                AND L1.DATA_DIV = '01' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                                                AND L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                AND L2.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                AND L2.DATA_DIV = '02' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT L3 ON L3.YEAR = T1.YEAR ";
        $query .= "                                                                AND L3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                AND L3.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                AND L3.DATA_DIV = '03' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT L4 ON L4.YEAR = T1.YEAR ";
        $query .= "                                                                AND L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                AND L4.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                AND L4.DATA_DIV = '04' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT L5 ON L5.YEAR = T1.YEAR ";
        $query .= "                                                                AND L5.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                AND L5.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                AND L5.DATA_DIV = '05' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT L6 ON L6.YEAR = T1.YEAR ";
        $query .= "                                                                AND L6.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                AND L6.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                AND L6.DATA_DIV = '06' ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT L7 ON L7.YEAR = T1.YEAR ";
        $query .= "                                                                AND L7.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                                                AND L7.RECORD_DATE = T1.RECORD_DATE ";
        $query .= "                                                                AND L7.DATA_DIV = '07' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->main_year."' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.RECORD_DATE = '".$model->record_date."' ";
        
        return $query;
    }

/***************更新、追加、削除***************/

    //Mainデータカウント
    function &getCheckMainDataQuery($db, $model, $fieldNo)
    {
        $setMainTable = "";
        if ($fieldNo === '1') {
            //B
            $setMainTable = "SCHREG_CHALLENGED_PROFILE_MAIN_DAT";
        } else if ($fieldNo === '2') {
            //A
            $setMainTable = "SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT";
        } else if ($fieldNo === '3') {
            //C
            $setMainTable = "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT";
        } else if ($fieldNo === '4') {
            //D
            $setMainTable = "SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT";
            //サポートブック
        } else if ($fieldNo === '5') {
            $setMainTable = "SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT";
            //引継資料(担当者)
        } else if ($fieldNo === '6') {
            $setMainTable = "SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT";
            //引継資料(事業者)
        } else if ($fieldNo === '7') {
            $setMainTable = "SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT";
            //関係者間資料
        } else {
            $setMainTable = "SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT";
        }
        //Mainデータの有無確認
        $query  = "  SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "      $setMainTable ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO = '".$model->schregno."' ";
        if ($fieldNo === '2' || $fieldNo === '3' || $fieldNo === '5' || $fieldNo === '6' || $fieldNo === '7' || $fieldNo === '8') {
            $query .= " AND YEAR       = '".$model->main_year."' ";
        }
        $query .= " AND RECORD_DATE = '".$model->record_date."' ";
        $mainCountData = $db->getOne($query);
        
        return $mainCountData;
    }

    //データチェック
    function &getCheckData($db, $model, $tablename, $fielddata, $fieldNo, $cmd, $flg, $itemdiv="")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ".$tablename." ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO   = '".$model->schregno."' ";
        if ($fieldNo === '2' || $fieldNo === '3' || $fieldNo === '5' || $fieldNo === '6' || $fieldNo === '7' || $fieldNo === '8') {
            $query .= " AND YEAR       = '".$model->main_year."' ";
        }
        $query .= " AND RECORD_DATE = '".$model->record_date."' ";
        //以下、レコードタイプ(固定型)のみ
        if ($flg === 'record_kotei') {
            if ($cmd === 'healthcare1') {
                $query .= " AND RECORD_DIV = '3' ";
                $query .= " AND RECORD_SEQ = 1 ";
            } else if ($cmd === 'careerguidance4') {
                $query .= " AND RECORD_DIV = '".$model->field4["RECORD_DIV"]."' ";
                if ($model->field4["RECORD_DIV"] === '5') {
                    $query .= " AND RECORD_NO  = '1' ";
                } else if ($model->field4["RECORD_DIV"] === '6') {
                    $query .= " AND RECORD_NO  = '3' ";
                }
                $query .= " AND RECORD_SEQ = 1 ";
            } else if ($cmd === 'welfare1') {
                $query .= " AND RECORD_DIV = '".$model->getRecordDiv."' ";
                $query .= " AND RECORD_NO  = '".$model->getRecordNo."' ";
                $query .= " AND RECORD_SEQ =  ".$model->getRecordSeq." ";
                $query .= " AND ITEMDIV    = '".$itemdiv."' ";
            }
        } else if ($flg === 'main2') {
            $query .= " AND DATA_DIV = '".$fielddata."' ";//$fielddataはここではval[3]またはfield["DATA_DIV"]の値
        }
        $getCnt = $db->getOne($query);
        return $getCnt;
    }

/****メインテーブル　更新処理****/
    //UPDATE (mainテーブルのみ)
    function &getUpdateMainQuery($db, $model, $fields, $tablename, $fielddata, $fieldNo, $cmd)
    {
        //フィールド名/　$key
        //サイズ/　$val[0]
        //名称/　$val[1]
        //データ形式/　$val[2]
        
        //データ有無チェック
        $getCount = knje390nQuery::getCheckData($db, $model, $tablename, $fielddata, $fieldNo, $cmd, "main");
        $data = array();
        foreach ($fielddata as $key => $val) {
            if ($val[2] === 'DATE') {
                $fields[$key] = str_replace("/", "-", $fields[$key]);
            }
            $data[$key][$val[2]] = $fields[$key];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        if ($getCount > 0) {
            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";
            if ($fieldNo === '2' || $fieldNo === '3' || $fieldNo === '5' || $fieldNo === '6' || $fieldNo === '7' || $fieldNo === '8') {
                $where .= " AND YEAR = '".$model->main_year."' ";
            }
            $where .= " AND RECORD_DATE = '".$model->record_date."' ";
            $db->query(Query::updateSQL($data, $tablename, $where));
        } else {
            if ($fieldNo === '2' || $fieldNo === '3' || $fieldNo === '5' || $fieldNo === '6' || $fieldNo === '7' || $fieldNo === '8') {
                $data["YEAR"][TEXT]             = $model->main_year;
            }
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["RECORD_DATE"][TEXT]      = $model->record_date;
            $db->query(Query::insertSQL($data, $tablename));
        }

        return;
    }

    //UPDATE (main画面にて、別テーブル)※($fieldNo === '2', $fieldNo === '3', $fieldNo === '4', $fieldNo === '8')
    function &getUpdateMain2Query($db, $model, $fields, $tablename, $fielddata, $fieldNo, $cmd)
    {
        //フィールド名/　$key
        //サイズ/　$val[0]
        //名称/　$val[1]
        //データ形式/　$val[2]
        //DATA_DIV/　$val[3]
        
        foreach ($fielddata as $key => $val) {
            $data = array();
            //データ有無チェック
            $getCount = knje390nQuery::getCheckData($db, $model, $tablename, $val[3], $fieldNo, $cmd, "main2");
            if (strstr($key, 'STATUS2')) {
                $data["STATUS2"][TEXT]  = $fields[$key];
            } else if (strstr($key, 'STATUS3')) {
                $data["STATUS3"][TEXT]  = $fields[$key];
            } else if (strstr($key, 'STATUS')) {
                $data["STATUS"][TEXT]   = $fields[$key];
            } else if (strstr($key, 'GROWUP')) {
                $data["GROWUP"][TEXT]   = $fields[$key];
            } else {
                $data["DOCUMENTS"][TEXT]   = $fields[$key];
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            if ($getCount == 0) {
                if ($fieldNo === '2' || $fieldNo === '3' || $fieldNo === '8') {
                    $data["YEAR"][TEXT]     = $model->main_year;
                }
                $data["SCHREGNO"][TEXT] = $model->schregno;
                $data["RECORD_DATE"][TEXT] = $model->record_date;
                $data["DATA_DIV"][TEXT] = $val[3];
                $db->query(Query::insertSQL($data, $tablename));
            } else {
                $where  = " WHERE ";
                $where .= "     SCHREGNO = '".$model->schregno."' ";
                if ($fieldNo === '2' || $fieldNo === '3' || $fieldNo === '8') {
                    $where .= " AND YEAR = '".$model->main_year."' ";
                }
                $where .= " AND RECORD_DATE = '".$model->record_date."' ";
                $where .= " AND DATA_DIV = '".$val[3]."' ";
                $db->query(Query::updateSQL($data, $tablename, $where));
            }
        }

        if ($fieldNo === '2' || $fieldNo === '3') {
            $maxDataDiv = 0;
            if ($fields["MAX_DATA_DIV"] > 0) $maxDataDiv = $fields["MAX_DATA_DIV"];

            $query  = " DELETE FROM ";
            $query .=       $tablename;
            $query .= " WHERE ";
            $query .= "     SCHREGNO    = '".$model->schregno."' ";
            $query .= " AND YEAR        = '".$model->main_year."' ";
            $query .= " AND RECORD_DATE = '".$model->record_date."' ";
            $query .= " AND INT(DATA_DIV)    > {$maxDataDiv} ";
            $db->query($query);
        }

        return;
    }

    //UPDATE (main画面にて、別テーブル)※($fieldNo === '6', $fieldNo === '7')
    function &getUpdateMain2_2Query($db, $model, $fields, $tablename, $fielddata, $fieldNo, $cmd)
    {
        //フィールド名/　$key
        //サイズ/　$val[0]
        //名称/　$val[1]
        //データ形式/　$val[2]
        
        foreach ($fielddata as $key => $val) {
            $data = array();
            //データ有無チェック($fieldNo === '6', $fieldNo === '7')
            $getCount = knje390nQuery::getCheckData($db, $model, $tablename, $fields["DATA_DIV"], $fieldNo, $cmd, "main2");
            if ($val[2] === 'DATE') {
                $fields[$key] = str_replace("/", "-", $fields[$key]);
            }
            $data[$key][$val[2]] = $fields[$key];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            if ($getCount == 0) {
                $data["YEAR"][TEXT]     = $model->main_year;
                $data["SCHREGNO"][TEXT] = $model->schregno;
                $data["RECORD_DATE"][TEXT] = $model->record_date;
                $data["DATA_DIV"][TEXT] = $fields["DATA_DIV"];
                $db->query(Query::insertSQL($data, $tablename));
            } else {
                $where  = " WHERE ";
                $where .= "     SCHREGNO = '".$model->schregno."' ";
                $where .= " AND YEAR = '".$model->main_year."' ";
                $where .= " AND RECORD_DATE = '".$model->record_date."' ";
                $where .= " AND DATA_DIV = '".$fields["DATA_DIV"]."' ";
                $db->query(Query::updateSQL($data, $tablename, $where));
            }
        }
        
        return;
    }

/****レコードタイプテーブル　更新処理****/

    //RECORD_SEQのMAXを取得
    function &getMaxRecordSeq($db, $model, $fields, $tablename, $fieldNo, $cmd, $cmd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     MAX(RECORD_SEQ) ";
        $query .= " FROM ";
        $query .= "     ".$tablename." ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO   = '".$model->schregno."' ";
        if ($fieldNo === '2' || $fieldNo === '3') {
            $query .= " AND YEAR       = '".$model->main_year."' ";
        }
        $query .= " AND RECORD_DATE = '".$model->record_date."' ";
        if ($cmd !== 'work1' && $cmd !== 'check2' && $cmd !== 'check4' && $cmd !== 'smooth4') {
            //フィールド値がNULLの場合は固定値になるため、'1'をセット
            if ($fields["RECORD_DIV"] == "" ) {
                $fields["RECORD_DIV"] = ($cmd === 'healthcare1' && $cmd2 === 'care') ? '2' : '1';
            }
            $query .= " AND RECORD_DIV = '".$fields["RECORD_DIV"]."' ";
            if ($cmd === 'educate1' || $cmd === 'welfare1' || $cmd === 'careerguidance4') {
                if ($fields["RECORD_NO"] == "" ) {
                    $fields["RECORD_NO"] = '1';
                }
                $query .= " AND RECORD_NO  = '".$fields["RECORD_NO"]."' ";
            }
        }
        $getMaxSeq = $db->getOne($query);
        return $getMaxSeq;
    }

    function getMedexamCntQuery($model, $tablename, $where)
    {
        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= " ".$tablename." ";
        $query .= $where;
        return $query;
    }

    //UPDATE (視力・聴力タイプの更新)
    function &getUpdateMedexamQuery($db, $model, $fields, $tablename, $fielddata)
    {
        $data = array();
        $data["YEAR"][TEXT] = $model->main_year;
        $data["SCHREGNO"][TEXT] = $model->schregno;
        $data["R_BAREVISION"][TEXT] = $fields["R_BAREVISION"];
        $data["L_BAREVISION"][TEXT] = $fields["L_BAREVISION"];
        $data["R_VISION"][TEXT]     = $fields["R_VISION"];
        $data["L_VISION"][TEXT]     = $fields["L_VISION"];
        $data["R_EAR_DB"][TEXT]     = $fields["R_EAR_DB"];
        $data["L_EAR_DB"][TEXT]     = $fields["L_EAR_DB"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "   YEAR = '".$model->main_year."' ";
        $where .= "   AND SCHREGNO = '".$model->schregno."' ";

        $cnt = $db->getOne(knje390nQuery::getMedexamCntQuery($model, $tablename, $where));

        if ($cnt > 0) {
            $query = Query::updateSQL($data, $tablename, $where);
        } else {
            $query = Query::insertSQL($data, $tablename);
        }
        $db->query($query);
        
        return;
    }

    //UPDATE (視力・聴力タイプの更新)
    function &getUpdateMedexamSubQuery($db, $model, $fields, $tablename, $fielddata)
    {
        $data = array();
        $data["YEAR"][TEXT] = $model->main_year;
        $data["SCHREGNO"][TEXT] = $model->schregno;
        $data["DET_SEQ"][TEXT] = '019';
        $data["DET_REMARK1"][TEXT] = $fields["DET_REMARK1"];
        $data["DET_REMARK2"][TEXT] = $fields["DET_REMARK2"];
        $data["DET_REMARK3"][TEXT] = $fields["DET_REMARK3"];
        $data["DET_REMARK4"][TEXT] = $fields["DET_REMARK4"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $where  = " WHERE ";
        $where .= "   YEAR = '".$model->main_year."' ";
        $where .= "   AND SCHREGNO = '".$model->schregno."' ";
        $where .= "   AND DET_SEQ = '019' ";

        $cnt = $db->getOne(knje390nQuery::getMedexamCntQuery($model, $tablename, $where));

        if ($cnt > 0) {
            $query = Query::updateSQL($data, $tablename, $where);
        } else {
            $query = Query::insertSQL($data, $tablename);
        }
        $db->query($query);
        
        return;
    }

    function &getUpdateMedexamHdatQuery($db, $model, $dval)
    {
        $tblname = "MEDEXAM_HDAT";
        $data = array();
        $data["YEAR"][TEXT] = $model->main_year;
        $data["SCHREGNO"][TEXT] = $model->schregno;
        $data["DATE"][DATE] = str_replace("/", "-", $dval);
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $where  = " WHERE ";
        $where .= "   YEAR = '".$model->main_year."' ";
        $where .= "   AND SCHREGNO = '".$model->schregno."' ";

        $cnt = $db->getOne(knje390nQuery::getMedexamCntQuery($model, $tblname, $where));
        if ($cnt > 0) {
            $query = Query::updateSQL($data, $tblname, $where);
        } else {
            $query = Query::insertSQL($data, $tblname);
        }
        $db->query($query);
        
        return;
    }

    //UPDATE (レコードタイプの更新)
    function &getUpdateRecordQuery($db, $model, $fields, $tablename, $fielddata, $fieldNo, $cmd)
    {
        //フィールド名/　$key
        //サイズ/　$val[0]
        //名称/　$val[1]
        //データ形式/　$val[2]
        $data = array();
        foreach ($fielddata as $key => $val) {
            if ($val[2] === 'DATE') {
                $fields[$key] = str_replace("/", "-", $fields[$key]);
            }
            if ($key !== 'RECORD_DIV' && $key !== 'RECORD_NO') {
                if ($key === 'SERVICE_CENTERCD_SET') {
                    $servicd_centercd_array = array();
                    $servicd_centercd_array = explode("-", $fields[$key]);
                    $data["SERVICE_CENTERCD"][$val[2]] = $servicd_centercd_array[0];
                    $data["SERVICE_CENTERCD_EDABAN"][$val[2]] = $servicd_centercd_array[1];
                } else {
                    $data[$key][$val[2]] = $fields[$key];
                }
            }
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHREGNO = '".$model->schregno."' ";
        if ($fieldNo === '2' || $fieldNo === '3') {
            $where .= " AND YEAR = '".$model->main_year."' ";
            $data["YEAR"][TEXT] = $model->main_year;
        }
        $where .= " AND RECORD_DATE = '".$model->record_date."' ";
        if ($cmd !== 'work1' && $cmd !== 'check2' && $cmd !== 'check4' && $cmd !== 'smooth4') {
            $where .= " AND RECORD_DIV = '".$model->getRecordDiv."' ";
            $data["RECORD_DIV"][TEXT] = $model->getRecordDiv;
            if ($cmd === 'educate1' || $cmd === 'welfare1' || $cmd === 'careerguidance4') {
                $where .= " AND RECORD_NO  = '".$model->getRecordNo."' ";
                $data["RECORD_NO"][TEXT] = $model->getRecordNo;
            }
        }
        $where .= " AND RECORD_SEQ = ".$model->getRecordSeq." ";

        $query = "SELECT COUNT(*) FROM {$tablename} {$where}";
        $recCnt = $db->getOne($query);
        if ($recCnt > 0) {
            $query = Query::updateSQL($data, $tablename, $where);
            $db->query($query);
        } else {
            $data["SCHREGNO"][TEXT] = $model->schregno;
            $data["RECORD_DATE"][TEXT] = $model->record_date;
            $data["RECORD_SEQ"][NUMBER] = $model->getRecordSeq;
            $query = Query::insertSQL($data, $tablename);
            $db->query($query);
        }

        return;
    }

    //UPDATE (レコードタイプの更新)
    function &getUpdateRecordSubQuery($db, $model, $fields, $tablename, $fielddata, $fieldNo, $cmd) {
        //フィールド名/　$key
        //サイズ/　$val[0]
        //名称/　$val[1]
        //データ形式/　$val[2]

        $flg = ($model->getRecordDiv == "3") ? 3 : $model->getRecordNo - 1;
        foreach ($model->itemdiv[$flg] as $ikey => $ival) {

            //データ有無チェック
            $getCount = knje390nQuery::getCheckData($db, $model, $tablename, $fielddata, $fieldNo, $cmd, "record_kotei", $ival);
            $data = array();
            foreach ($fielddata as $key => $val) {
                if ($val[2] === 'DATE') {
                    $fields[$key."_".$ival] = str_replace("/", "-", $fields[$key."_".$ival]);
                }
                if ($key !== 'RECORD_DIV' && $key !== 'RECORD_NO' && $key !== 'ITEMDIV') {
                    $data[$key][$val[2]] = $fields[$key."_".$ival];
                }
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            if ($getCount > 0) {
                $where  = " WHERE ";
                $where .= "     SCHREGNO        = '".$model->schregno."' ";
                $where .= " AND RECORD_DATE     = '".$model->record_date."' ";
                $where .= " AND RECORD_DIV      = '".$model->getRecordDiv."' ";
                $where .= " AND RECORD_NO       = '".$model->getRecordNo."' ";
                $where .= " AND RECORD_SEQ      =  ".$model->getRecordSeq." ";
                $where .= " AND ITEMDIV         = '".$ival."' ";
                $db->query(Query::updateSQL($data, $tablename, $where));
            } else {
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["RECORD_DATE"][TEXT]      = $model->record_date;
                $data["RECORD_DIV"][TEXT]       = $model->getRecordDiv;
                $data["RECORD_NO"][TEXT]        = $model->getRecordNo;
                $data["RECORD_SEQ"][NUMBER]     = $model->getRecordSeq;
                $data["ITEMDIV"][TEXT]          = $ival;
                $db->query(Query::insertSQL($data, $tablename));
            }
        }
        return;
    }

    //UPDATE (レコードタイプ(固定型)の更新)
    function &getUpdateRecord2Query($db, $model, $fields, $tablename, $fielddata, $fieldNo, $cmd)
    {
        //フィールド名/　$key
        //サイズ/　$val[0]
        //名称/　$val[1]
        //データ形式/　$val[2]
        
        //データ有無チェック
        $getCount = knje390nQuery::getCheckData($db, $model, $tablename, $fielddata, $fieldNo, $cmd, "record_kotei");
        $data = array();
        foreach ($fielddata as $key => $val) {
            if ($val[2] === 'DATE') {
                $fields[$key] = str_replace("/", "-", $fields[$key]);
            }
            if ($key !== 'RECORD_DIV' && $key !== 'RECORD_NO') {
                $data[$key][$val[2]] = $fields[$key];
            }
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        if ($getCount > 0) {
            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";
            if ($fieldNo === '2' || $fieldNo === '3') {
                $where .= " AND YEAR = '".$model->main_year."' ";
            }
            $where .= " AND RECORD_DATE = '".$model->record_date."' ";
            if ($cmd === 'healthcare1') {
                $where .= " AND RECORD_DIV = '3' ";
            } else if ($cmd === 'careerguidance4') {
                $where .= " AND RECORD_DIV = '".$fields["RECORD_DIV"]."' ";
                if ($fields["RECORD_DIV"] === '5') {
                    $where .= " AND RECORD_NO  = '1' ";
                } else if ($fields["RECORD_DIV"] === '6') {
                    $where .= " AND RECORD_NO  = '3' ";
                }
            }
            $where .= " AND RECORD_SEQ = 1 ";
            $db->query(Query::updateSQL($data, $tablename, $where));
        } else {
            if ($fieldNo === '2' || $fieldNo === '3') {
                $data["YEAR"][TEXT]             = $model->main_year;
            }
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["RECORD_DATE"][TEXT]      = $model->record_date;
            if ($cmd === 'healthcare1') {
                $data["RECORD_DIV"][TEXT]   = 3;
            } else if ($cmd === 'careerguidance4') {
                $data["RECORD_DIV"][TEXT] = $fields["RECORD_DIV"];
                if ($fields["RECORD_DIV"] === '5') {
                    $data["RECORD_NO"][TEXT] = 1;
                } else if ($fields["RECORD_DIV"] === '6') {
                    $data["RECORD_NO"][TEXT] = 3;
                }
            }
            $data["RECORD_SEQ"][NUMBER]   = 1;
            $db->query(Query::insertSQL($data, $tablename));
        
        }
        return;
    }
    
    //INSERT (レコードタイプの更新)
    function &getInsertRecordQuery($db, $model, $fields, $tablename, $fielddata, $fieldNo, $cmd, $cmd2 = "")
    {
        //フィールド名/　$key
        //サイズ/　$val[0]
        //名称/　$val[1]
        //データ形式/　$val[2]
        
        //RECORD_SEQチェック
        $getMaxSeq = knje390nQuery::getMaxRecordSeq($db, $model, $fields, $tablename, $fieldNo, $cmd, $cmd2);
        $data = array();
        foreach ($fielddata as $key => $val) {
            if ($val[2] === 'DATE') {
                $fields[$key] = str_replace("/", "-", $fields[$key]);
            }
            if (($key === 'RECORD_DIV' || $key === 'RECORD_NO') && $fields[$key] == "") {
                //フィールドがない場合は固定値となるため、'1'をセット
                $fields[$key] = ($cmd === 'healthcare1' && $cmd2 === 'care') ? '2' : '1';
            }
            if ($key === 'SERVICE_CENTERCD_SET') {
                $servicd_centercd_array = array();
                $servicd_centercd_array = explode("-", $fields[$key]);
                $data["SERVICE_CENTERCD"][$val[2]] = $servicd_centercd_array[0];
                $data["SERVICE_CENTERCD_EDABAN"][$val[2]] = $servicd_centercd_array[1];
            } else {
                $data[$key][$val[2]] = $fields[$key];
            }
        }
        if ($fieldNo === '2' || $fieldNo === '3') {
            $data["YEAR"][TEXT]             = $model->main_year;
        }
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["RECORD_DATE"][TEXT]      = $model->record_date;
        $data["RECORD_SEQ"][NUMBER]         = $getMaxSeq+1;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $db->query(Query::insertSQL($data, $tablename));
        return $getMaxSeq+1;
    }

    // DELETE
    function &getDeleteRecordQuery($db, $model, $fields, $tablename, $fielddata, $fieldNo, $cmd)
    {
        $query  = " DELETE FROM ";
        $query .= "     $tablename ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        if ($fieldNo === '2' || $fieldNo === '3') {
            $query .= " AND YEAR = '".$model->getYear."' ";
        }
        $query .= " AND RECORD_DATE = '".$model->record_date."' ";
        if ($cmd !== 'work1' && $cmd !== 'check2' && $cmd !== 'check4' && $cmd !== 'smooth4') {
            $query .= " AND RECORD_DIV = '".$model->getRecordDiv."' ";
            if ($cmd === 'educate1' || $cmd === 'welfare1' || $cmd === 'careerguidance4') {
                $query .= " AND RECORD_NO  = '".$model->getRecordNo."' ";
            }
        }
        $query .= " AND RECORD_SEQ = ".$model->getRecordSeq." ";
        $db->query($query);
        
        return ;
    }
    
    //家族構成の更新及び追加***********************/
    //連番の最大値を取得
    function getRela_Maxno($db, $model) {

        $query  = " SELECT ";
        $query .= "     MAX(INT(RELANO)) AS MAX ";
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        
        $max_relano = $db->getOne($query);
        return $max_relano;
    }

    //INSERT (レコードタイプの更新)
    function &getInsertUpdateFamilyQuery($db, $model, $fields, $cmd)
    {
        $data = array();
        $data["RELANAME"][TEXT]         = $fields["RELANAME"];
        $data["RELAKANA"][TEXT]         = $fields["RELAKANA"];
        $data["RELATIONSHIP"][TEXT]     = $fields["RELATIONSHIP"];
        $data["REMARK"][TEXT]           = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        if ($cmd === 'insert') {
            $getMaxRelano = knje390nQuery::getRela_Maxno($db, $model);
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["RELANO"][TEXT]           = $getMaxRelano+1;
            $db->query(Query::insertSQL($data, "SCHREG_RELA_DAT"));
            
            return $getMaxRelano+1;
        } else {
            $where  = " WHERE ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";
            $where .= " AND RELANO = '".$model->getRelano."' ";
            $db->query(Query::updateSQL($data, "SCHREG_RELA_DAT", $where));
            
            return $model->getRelano;
        }
    }

    // DELETE
    function &getDeleteFamilyQuery($db, $model)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " AND RELANO   = '".$model->getRelano."' ";
        $db->query($query);
        
        return ;
    }

    //INSERT (教科毎の実態の更新)
    function &getInsertUpdateStatusSubclassQuery($db, $model, $fields, $cmd)
    {
        $data = array();
        $data["STATUS"][TEXT]        = $fields["SUBCLASS_STATUS"];
        $data["FUTURE_CARE"][TEXT]   = $fields["SUBCLASS_FUTURE_CARE"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        if ($cmd === 'insert') {
            $data["YEAR"][TEXT]          = $model->main_year;
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["RECORD_DATE"][TEXT]   = $model->record_date;
    
            $data["CLASSCD"][TEXT]       = $fields["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]   = $fields["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT] = $fields["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]    = $fields["SUBCLASSCD"];

            $db->query(Query::insertSQL($data, "SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT"));
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->main_year."' ";
            $where .= " AND SCHREGNO = '".$model->schregno."' ";
            $where .= " AND RECORD_DATE = '".$model->record_date."' ";
            $where .= " AND CLASSCD = '".$fields["CLASSCD"]."' ";
            $where .= " AND SCHOOL_KIND = '".$fields["SCHOOL_KIND"]."' ";
            $where .= " AND CURRICULUM_CD = '".$fields["CURRICULUM_CD"]."' ";
            $where .= " AND SUBCLASSCD = '".$fields["SUBCLASSCD"]."' ";

            $db->query(Query::updateSQL($data, "SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT", $where));
        }

        return;
    }

    // DELETE (教科毎の実態の更新)
    function &getDeleteStatusSubclassQuery($db, $model, $fields)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->main_year."' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        $query .= " AND RECORD_DATE = '".$model->record_date."' ";
        $query .= " AND CLASSCD = '".$fields["CLASSCD"]."' ";
        $query .= " AND SCHOOL_KIND = '".$fields["SCHOOL_KIND"]."' ";
        $query .= " AND CURRICULUM_CD = '".$fields["CURRICULUM_CD"]."' ";
        $query .= " AND SUBCLASSCD = '".$fields["SUBCLASSCD"]."' ";

        $db->query($query);
        
        return ;
    }

/***************履 歴 作 成***************/
    
    //フィールド名取得（共通）
    function &getFieldName($settableName)
    {
        $query  = "  SELECT   ";
        $query .= "      COLUMN_NAME ";
        $query .= "  FROM   ";
        $query .= "      SYSIBM.SQLCOLUMNS  ";
        $query .= "  WHERE   ";
        $query .= "      TABLE_NAME = '".$settableName."' ";
        $query .= "  ORDER BY ";
        $query .= "      ORDINAL_POSITION ";
        
        return $query;
    }
    
    function getUpdateRirekiData($db, $model, $setBackupDate, $cmd)
    {
        $settableArray = array();
        if ($cmd === 'subform1') {
            //B
            $settableArray = array("0" => "SCHREG_CHALLENGED_PROFILE_MAIN_DAT",
                                   "1" => "SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT",
                                   "2" => "SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT",
                                   "3" => "SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT",
                                   "4" => "SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT",
                                   "5" => "SCHREG_CHALLENGED_PROFILE_WORK_RECORD_DAT");
        } else if ($cmd === 'subform2') {
            //A ※Aは画面のボタンはカットし、履歴を取る処理はない
            $settableArray = array("0" => "SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT",
                                   "1" => "SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT",
                                   "2" => "SCHREG_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT",
                                   "3" => "SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT");
        } else if ($cmd === 'subform3') {
            //C
            $settableArray = array("0" => "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT",
                                   "1" => "SCHREG_CHALLENGED_SUPPORTPLAN_CHECK_RECORD_DAT",
                                   "2" => "SCHREG_CHALLENGED_SUPPORTPLAN_SCHOOL_RECORD_DAT",
                                   "3" => "SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT",
                                   "4" => "SCHREG_CHALLENGED_SUPPORTPLAN_WELFARE_RECORD_DAT",
                                   "5" => "SCHREG_CHALLENGED_SUPPORTPLAN_WORK_RECORD_DAT");
        } else {
            //D
            $settableArray = array("0" => "SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT",
                                   "1" => "SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT",
                                   "2" => "SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT",
                                   "3" => "SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CONSULTATION_RECORD_DAT",
                                   "4" => "SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT");
        }
        
        foreach ($settableArray as $settableName){
        
            //フィールド名を取得
            $filedarray = array();
            $query = knje390nQuery::getFieldName($settableName);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $filedarray[] = $row["COLUMN_NAME"];
            }
            $result->free();
        
            //履歴テーブルDELETE
            $query  = "  DELETE ";
            $query .= "  FROM ";
            $query .= "      $settableName ";
            $query .= "  WHERE ";
            $query .= "      SCHREGNO = '".$model->schregno."' ";
            if ($cmd === 'subform2' || $cmd === 'subform3') {
                //$query .= "  AND YEAR = '".$model->main_year."' ";
                $query .= "  AND YEAR = FISCALYEAR(DATE('".$setBackupDate."')) ";
            }
            $query .= "  AND RECORD_DATE = '".$setBackupDate."' ";
            $db->query($query);
            
            //履歴テーブルINSERT
            $query  = "  INSERT INTO $settableName ";
            $query .= "  SELECT ";
            $conma = "";
            foreach ($filedarray as $fieldName) {
                if ($fieldName === 'RECORD_DATE') {
                    $query .= "  $conma '".$setBackupDate."' ";
                } else if ($fieldName === 'YEAR') {
                    $query .= "  $conma FISCALYEAR(DATE('".$setBackupDate."')) ";
                } else if ($fieldName === 'UPDATED') {
                    $query .= "  $conma sysdate() ";
                } else {
                    $query .= "  $conma $fieldName ";
                }
                $conma = ",";
            }
            $query .= "  FROM ";
            $query .= "      $settableName ";
            $query .= "  WHERE ";
            $query .= "      SCHREGNO = '".$model->schregno."' ";
            if ($cmd === 'subform2' || $cmd === 'subform3') {
                $query .= "  AND YEAR = '".$model->main_year."' ";
            }
            $query .= "  AND RECORD_DATE = '".$model->record_date."' ";
            $db->query($query);
        }

        return ;
    }
    
/***************年 度 データ 作 成***************/

    function getUpdateNewData($db, $model, $cmd)
    {
        $setMainTable = "";
        $settableArray = array();
        $setMainFieldYear = "";
        $setMainFieldRecordDate = "";
        if ($cmd === 'subform2') {
            //A
            $setMainTable = "SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT";
            $settableArray = array("0" => "SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT",
                                   "1" => "SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT",
                                   "2" => "SCHREG_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT",
                                   "3" => "SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT"
                                );
            $setMainFieldYear = $model->mainfield["YEAR_A"];
            $setMainFieldRecordDate = $model->mainfield["RECORD_DATE_A"];
        } else if ($cmd === 'subform3') {
            //C
            $setMainTable = "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT";
            $settableArray = array("0" => "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT",
                                   "1" => "SCHREG_CHALLENGED_SUPPORTPLAN_CHECK_RECORD_DAT",
                                   "2" => "SCHREG_CHALLENGED_SUPPORTPLAN_SCHOOL_RECORD_DAT",
                                   "3" => "SCHREG_CHALLENGED_SUPPORTPLAN_STATUS_DAT",
                                   "4" => "SCHREG_CHALLENGED_SUPPORTPLAN_WELFARE_RECORD_DAT",
                                   "5" => "SCHREG_CHALLENGED_SUPPORTPLAN_WORK_RECORD_DAT");
            $setMainFieldYear = $model->mainfield["YEAR_C"];
            $setMainFieldRecordDate = $model->mainfield["RECORD_DATE_C"];
        } else if ($cmd === 'subform5') {
            //サポートブック(E)
            $setMainTable = "SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT";
            $settableArray = array("0" => "SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT");
            $setMainFieldYear = $model->mainfield["YEAR_E"];
            $setMainFieldRecordDate = $model->mainfield["RECORD_DATE_E"];
        } else if ($cmd === 'subform6') {
            //引継資料(担任)(F)
            $setMainTable = "SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT";
            $settableArray = array("0" => "SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT",
                                   "1" => "SCHREG_CHALLENGED_TEACHER_TAKEOVER_DOCUMENTS_DAT");
            $setMainFieldYear = $model->mainfield["YEAR_F"];
            $setMainFieldRecordDate = $model->mainfield["RECORD_DATE_F"];
        } else if ($cmd === 'subform7') {
            //引継資料(事業者)(G)
            $setMainTable = "SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT";
            $settableArray = array("0" => "SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT",
                                   "1" => "SCHREG_CHALLENGED_OPERATOR_TAKEOVER_DOCUMENTS_DAT");
            $setMainFieldYear = $model->mainfield["YEAR_G"];
            $setMainFieldRecordDate = $model->mainfield["RECORD_DATE_G"];
        } else {
            //関係者間資料(H)
            $setMainTable = "SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT";
            $settableArray = array("0" => "SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT",
                                   "1" => "SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT");
            $setMainFieldYear = $model->mainfield["YEAR_H"];
            $setMainFieldRecordDate = $model->mainfield["RECORD_DATE_H"];
        }
        
        //作成日付変換
        $setWritingDate = "";
        $setWritingDate = str_replace("/", "-", $model->mainfield["WRITING_DATE_MAIN"]);
        
        //年度の最新データの有無確認
        if ($cmd === 'subform3') {
            $query  = "  SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= "  FROM ";
            $query .= "      $setMainTable ";
            $query .= "  WHERE ";
            $query .= "      SCHREGNO = '".$model->schregno."' ";
            $query .= "  AND YEAR = '".CTRL_YEAR."' ";
            $query .= "  AND RECORD_DATE = 'NEW' ";
            $dataCount = $db->getOne($query);
            
            //年度の最新データを本日付でバックアップ
            if ($dataCount > 0) {
                foreach ($settableArray as $settableName){
                
                    //フィールド名を取得
                    $filedarray = array();
                    $query = knje390nQuery::getFieldName($settableName);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $filedarray[] = $row["COLUMN_NAME"];
                    }
                    $result->free();
                
                    //履歴テーブルDELETE
                    $query  = "  DELETE ";
                    $query .= "  FROM ";
                    $query .= "      $settableName ";
                    $query .= "  WHERE ";
                    $query .= "      SCHREGNO = '".$model->schregno."' ";
                    $query .= "  AND YEAR = '".CTRL_YEAR."' ";
                    $query .= "  AND RECORD_DATE = '".CTRL_DATE."' ";
                    $db->query($query);
                    
                    //履歴テーブルINSERT
                    $query  = "  INSERT INTO $settableName ";
                    $query .= "  SELECT ";
                    $conma = "";
                    foreach ($filedarray as $fieldName) {
                        if ($fieldName === 'RECORD_DATE') {
                            $query .= "  $conma '".CTRL_DATE."' ";
                        } else if ($fieldName === 'YEAR') {
                            $query .= "  $conma '".CTRL_YEAR."' ";
                        } else {
                            $query .= "  $conma $fieldName ";
                        }
                        $conma = ",";
                    }
                    $query .= "  FROM ";
                    $query .= "      $settableName ";
                    $query .= "  WHERE ";
                    $query .= "      SCHREGNO = '".$model->schregno."' ";
                    $query .= "  AND YEAR = '".CTRL_YEAR."' ";
                    $query .= "  AND RECORD_DATE = 'NEW' ";
                    $db->query($query);
                }
            }
        }

        //画面指定日付データを最新データにINSERT
        foreach ($settableArray as $settableName){
            //フィールド名を取得
            $filedarray = array();
            $query = knje390nQuery::getFieldName($settableName);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $filedarray[] = $row["COLUMN_NAME"];
            }
            $result->free();
        
            //最新テーブルデータDELETE
            $query  = "  DELETE ";
            $query .= "  FROM ";
            $query .= "      $settableName ";
            $query .= "  WHERE ";
            $query .= "      SCHREGNO = '".$model->schregno."' ";
            $query .= "  AND YEAR = '".CTRL_YEAR."' ";
            if ($cmd === 'subform2' || $cmd === 'subform5' || $cmd === 'subform6' || $cmd === 'subform7' || $cmd === 'subform8') {
                $query .= "  AND RECORD_DATE = '".$setWritingDate."' ";
            } else {
                $query .= "  AND RECORD_DATE = 'NEW' ";
            }
            $db->query($query);
            
            //最新テーブルデータINSERT
            $query  = "  INSERT INTO $settableName ";
            $query .= "  SELECT ";
            $conma = "";
            foreach ($filedarray as $fieldName) {
                if ($fieldName === 'RECORD_DATE') {
                    if ($cmd === 'subform2' || $cmd === 'subform5' || $cmd === 'subform6' || $cmd === 'subform7' || $cmd === 'subform8') {
                        $query .= "  $conma '".$setWritingDate."' ";
                    } else {
                        $query .= "  $conma 'NEW' ";
                    }
                } else if ($fieldName === 'WRITING_DATE') {
                    $query .= "  $conma '".$setWritingDate."' ";
                } else if ($fieldName === 'YEAR') {
                    $query .= "  $conma '".CTRL_YEAR."' ";
                } else if ($fieldName === 'UPDATED') {
                    $query .= "  $conma sysdate() ";
                } else {
                    $query .= "  $conma $fieldName ";
                }
                $conma = ",";
            }
            $query .= "  FROM ";
            $query .= "      $settableName ";
            $query .= "  WHERE ";
            $query .= "      SCHREGNO = '".$model->schregno."' ";
            $query .= "  AND YEAR = '".$setMainFieldYear."' ";
            $query .= "  AND RECORD_DATE = '".$setMainFieldRecordDate."' ";
            $db->query($query);
        }

        return $dataCount;
    }
    

/***************更 新 履 歴 (ログ)******************/

    function getUpdateLog($db, $model, $tablename, $fieldNo, $cmd, $dataCount)
    {
        global $auth;
        if ($auth->auth["uid"] != "") {
            $db = Query::dbCheckOut();
            $settype = "";
            $setYear = "";
            $setDate = "";
            //履歴作成、指定日付を元に新規の場合はタイプごとの全てになる
            if ($tablename == "") {
                $tablename = 'ALL';
            }
            if ($fieldNo === '1') {
                $settype = 'B';
                $setDate = $model->record_date;
                $setYear = "";
                $setfield = $model->field;
            } else if ($fieldNo === '2') {
                $settype = 'A';
                $setDate = $model->record_date;
                $setYear = $model->main_year;
                $setfield = $model->field2;
            } else if ($fieldNo === '3') {
                $settype = 'C';
                $setDate = $model->record_date;
                $setYear = $model->main_year;
                $setfield = $model->field3;
            } else if ($fieldNo === '4'){
                $settype = 'D';
                $setDate = $model->record_date;
                $setYear = "";
                $setfield = $model->field4;
            } else if ($fieldNo === '5') {
                $settype = 'E';
                $setDate = $model->record_date;
                $setYear = $model->main_year;
                $setfield = $model->field5;
            } else if ($fieldNo === '6') {
                $settype = 'F';
                $setDate = $model->record_date;
                $setYear = $model->main_year;
                $setfield = $model->field6;
            } else if ($fieldNo === '7') {
                $settype = 'G';
                $setDate = $model->record_date;
                $setYear = $model->main_year;
                $setfield = $model->field7;
            } else {
                $settype = 'H';
                $setDate = $model->record_date;
                $setYear = $model->main_year;
                $setfield = $model->field8;
            }
            
            //指定日付を元に新規作成
            if ($cmd === 'new') {
                //A アセスメント表は作成日付がそのままRECORD_DATEになる (E, F, G, Hも同一)
                if ($fieldNo === '2' || $fieldNo === '5' || $fieldNo === '6' || $fieldNo === '7' || $fieldNo === '8') {
                    $setDate = str_replace("/", "-", $model->mainfield["WRITING_DATE_MAIN"]);
                } else {
                    $setDate = 'NEW';
                }
                $setYear = CTRL_YEAR;
                //最新データをバックアップした時はログに残す
                if ($dataCount > 0) {
                    $data = array();
                    $data["UPDATED"][NUMBER]        = "sysdate()";
                    $data["USERID"][TEXT]           = $auth->auth["uid"];
                    $data["STAFFCD"][TEXT]          = STAFFCD;
                    $data["DATA_TYPE"][TEXT]        = $settype;
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SCHREGNO"][TEXT]         = $model->schregno;
                    $data["RECORD_DATE"][TEXT]      = CTRL_DATE;
                    //$data["CMD"][TEXT]              = $cmd;
                    //$data["TABLE"][TEXT]            = $tablename;

                    $query = Query::insertSQL($data, "CHALLENGED_TABLE_UPDATE_LOG");
                    $db->query($query);
                }
            //履歴作成（履歴のみ）
            } else if ($cmd === 'rireki') {
                $backupYear = "";
                $backupdate = str_replace("/", "-", $setfield["BACKUP_DATE"]);
                if ($setYear != "") {
                    $backupYear = "FISCALYEAR(DATE('".$backupdate."'))";
                }
                $data = array();
                $data["UPDATED"][NUMBER]        = "sysdate()";
                $data["USERID"][TEXT]           = $auth->auth["uid"];
                $data["STAFFCD"][TEXT]          = STAFFCD;
                $data["DATA_TYPE"][TEXT]        = $settype;
                $data["YEAR"][NUMBER]           = $backupYear;//年度を取得するため、NUMBERにしている
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["RECORD_DATE"][TEXT]      = $backupdate;
                //$data["CMD"][TEXT]              = $cmd;
                //$data["TABLE"][TEXT]            = $tablename;

                $query = Query::insertSQL($data, "CHALLENGED_TABLE_UPDATE_LOG");
                $db->query($query);
                Query::dbCheckIn($db);
                return ;
            }
            $data = array();
            $data["UPDATED"][NUMBER]        = "sysdate()";
            $data["USERID"][TEXT]           = $auth->auth["uid"];
            $data["STAFFCD"][TEXT]          = STAFFCD;
            $data["DATA_TYPE"][TEXT]        = $settype;
            $data["YEAR"][TEXT]             = $setYear;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["RECORD_DATE"][TEXT]      = $setDate;
            //$data["CMD"][TEXT]              = $cmd;
            //$data["TABLE"][TEXT]            = $tablename;

            $query = Query::insertSQL($data, "CHALLENGED_TABLE_UPDATE_LOG");
            $db->query($query);
            Query::dbCheckIn($db);
        }
        return ;
    }
}
?>
