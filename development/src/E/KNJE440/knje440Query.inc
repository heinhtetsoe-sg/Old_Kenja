<?php

require_once('for_php7.php');
class knje440Query extends Query
{

    //教育委員会用の学校コード取得
    public function getSchoolMst($model)
    {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".$model->year."' ";

        return $query;
    }

    //対象年度取得
    public function getYear()
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE,";
        $query .= "     YEAR AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //文書番号
    public function getTuutatu($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.DOC_NUMBER AS VALUE, ";
        $query .= "     T1.DOC_NUMBER AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.DOC_NUMBER = L1.DOC_NUMBER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND L1.REQUEST_ANSWER_PRG = 'KNJE440' ";
        $query .= "     AND L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }


    //データ表示
    public function readQuery($model, $flg, $csv = "")
    {
        $pre_year1 = $model->year - 1;
        $pre_year2 = $model->year - 2;

        $query  = "";
        //学科・性別
        $query .= " WITH T_MAJOR (MAJORCD, MAJOR_NAME) AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.COURSECD || T1.MAJORCD, ";
        $query .= "         T2.MAJORNAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN V_COURSE_MAJOR_MST T2 ";
        $query .= "             ON T2.YEAR = T1.YEAR ";
        $query .= "            AND T2.COURSECD = T1.COURSECD ";
        $query .= "            AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '".$model->year."' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     ) ";
        $query .= " , T_SEX (SEX, SEX_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'Z002' ";
        $query .= "     ) ";
        $query .= " , T_MAJOR_SEX AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         T_MAJOR T1, ";
        $query .= "         T_SEX T2 ";
        $query .= "     ) ";
        if ($flg == "data") {
            $query .= " , T_ADDITION AS ( ";
            $query .= "     SELECT ";
            $query .= "         COURSECD || MAJORCD AS MAJORCD, ";
            $query .= "         SEX ";
            foreach ($model->nameArray as $name) {
                $query .= "         ,SUM(CASE (LARGE_DIV || MIDDLE_DIV || SMALL_DIV) WHEN '".$name."' THEN COUNT ELSE 0 END) AS CNT".$name." ";
            }
            $query .= "     FROM ";
            $query .= "         AFT_DISEASE_ADDITION440_DAT ";
            $query .= "     WHERE ";
            $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "         AND YEAR = '".$model->year."' ";
            $query .= "     GROUP BY ";
            $query .= "         COURSECD, ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            $query .= "     ) ";

            //メイン
            $query .= " SELECT ";
            if ($csv) {
                $query .= "     SUBSTR(T1.MAJORCD, 1, 1) AS COURSECD, ";
                $query .= "     SUBSTR(T1.MAJORCD, 2, 3) AS MAJORCD, ";
                $query .= "     T1.SEX ";
            } else {
                $query .= "     T1.MAJORCD, ";
                $query .= "     T1.SEX, ";
                $query .= "     T1.MAJOR_NAME, ";
                $query .= "     T1.SEX_NAME ";
            }
            foreach ($model->nameArray as $name) {
                $query .= "     ,L1.CNT".$name ;
            }
            if ($csv) {
                $query .= "     ,'".$model->lastColumn."' ";
            }
            $query .= " FROM ";
            $query .= "     T_MAJOR_SEX T1 ";
            $query .= "     LEFT JOIN T_ADDITION L1 ON L1.MAJORCD = T1.MAJORCD AND L1.SEX = T1.SEX ";
            $query .= " ORDER BY ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX ";
        } else {
            //卒業生を抽出
            $query .= ", SCH_INFO AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.COURSECD || T1.MAJORCD AS MAJORCD, ";
            $query .= "         T2.SEX ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1, ";
            $query .= "         SCHREG_BASE_MST T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->year."' AND ";
            $query .= "         T1.SEMESTER = ( SELECT ";
            $query .= "                             MAX(SEMESTER) ";
            $query .= "                         FROM ";
            $query .= "                             SEMESTER_MST ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '".$model->year."' AND ";
            $query .= "                             SEMESTER <> '9' ";
            $query .= "                     ) AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.GRD_DIV = '1' AND ";
            $query .= "         FISCALYEAR(T2.GRD_DATE) = '".$model->year."' ";
            //進路情報取得
            $query .= " ), AFT_INFO AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX, ";
            $query .= "         T2.SENKOU_KIND, ";
            $query .= "         T2.SCHOOL_GROUP, ";
            $query .= "         T3.GRD_DIV ";
            $query .= "     FROM ";
            $query .= "         SCH_INFO T1 ";
            $query .= "         LEFT JOIN AFT_GRAD_COURSE_DAT T2 ON T2.YEAR = '".$model->year."' AND T1.SCHREGNO = T2.SCHREGNO AND T2.PLANSTAT = '1' ";
            $query .= "         LEFT JOIN SCHREG_BASE_MST T3 ON T1.SCHREGNO = T3.SCHREGNO ";
            //進路情報振り分け
            $query .= " ), AFT_DATA AS ( ";
            $query .= "     SELECT ";
            $query .= "         SCHREGNO, ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX, ";
            $query .= "         CASE WHEN (SENKOU_KIND = '0' AND (SCHOOL_GROUP = '01' OR SCHOOL_GROUP = '02' OR SCHOOL_GROUP = '03')) THEN 1 ELSE 0 END AS CNT0101999, ";
            $query .= "         CASE WHEN (SENKOU_KIND = '0' AND (SCHOOL_GROUP = '04' OR SCHOOL_GROUP = '05')) THEN 1 ELSE 0 END AS CNT0102999, ";
            $query .= "         0 AS CNT0103999, ";
            $query .= "         0 AS CNT0104999, ";
            $query .= "         0 AS CNT0105999, ";
            $query .= "         0 AS CNT0106999, ";
            $query .= "         CASE WHEN (SENKOU_KIND = '0' AND (SCHOOL_GROUP = '06' OR SCHOOL_GROUP = '07')) THEN 1 ELSE 0 END AS CNT0299999, ";
            $query .= "         CASE WHEN (SENKOU_KIND = '0' AND SCHOOL_GROUP = '08') THEN 1 ELSE 0 END AS CNT0301999, ";
            $query .= "         CASE WHEN (SENKOU_KIND = '0' AND SCHOOL_GROUP = '99') THEN 1 ELSE 0 END AS CNT0302999, ";
            $query .= "         CASE WHEN (SENKOU_KIND = '0' AND SCHOOL_GROUP = '09') THEN 1 ELSE 0 END AS CNT0499999, ";
            $query .= "         0 AS CNT0699999, ";
            $query .= "         CASE WHEN (VALUE(SENKOU_KIND,'') = '' OR (SENKOU_KIND = '0' AND VALUE(SCHOOL_GROUP,'') = '')) THEN 1 ELSE 0 END AS CNT0799999, ";
            $query .= "         CASE WHEN (VALUE(SENKOU_KIND,'') = '' AND (VALUE(GRD_DIV,'0') = '5' OR VALUE(GRD_DIV,'0') = '6')) THEN 1 ELSE 0 END AS CNT0899999 ";
            $query .= "     FROM ";
            $query .= "         AFT_INFO ";
            //就職者
            $query .= " ), GET_0599999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX, ";
            $query .= "         COUNT(DISTINCT SCHREGNO) AS CNT0599999 ";
            $query .= "     FROM ";
            $query .= "         AFT_INFO ";
            $query .= "     WHERE ";
            $query .= "         SENKOU_KIND = '1' AND ";
            $query .= "         SCHREGNO NOT IN (SELECT ";
            $query .= "                             SCHREGNO ";
            $query .= "                         FROM ";
            $query .= "                             AFT_INFO ";
            $query .= "                         WHERE ";
            $query .= "                             SENKOU_KIND = '0' ";
            $query .= "                         ) ";
            $query .= "     GROUP BY ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            //計（卒業者総数）
            $query .= " ), GET_9999999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX, ";
            $query .= "         COUNT(DISTINCT SCHREGNO) AS CNT9999999 ";
            $query .= "     FROM ";
            $query .= "         SCH_INFO ";
            $query .= "     GROUP BY ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            //Ａのうち就職している生徒
            $query .= " ), GET_9901999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX, ";
            $query .= "         COUNT(DISTINCT SCHREGNO) AS CNT9901999 ";
            $query .= "     FROM ";
            $query .= "         AFT_INFO ";
            $query .= "     WHERE ";
            $query .= "         SENKOU_KIND = '0' AND ";
            $query .= "         SCHOOL_GROUP IN ('01','02','03','04','05') AND ";
            $query .= "         SCHREGNO IN (SELECT ";
            $query .= "                         SCHREGNO ";
            $query .= "                     FROM ";
            $query .= "                         AFT_INFO ";
            $query .= "                     WHERE ";
            $query .= "                         SENKOU_KIND = '1' ";
            $query .= "                     ) ";
            $query .= "     GROUP BY ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            //Ｂのうち就職している生徒
            $query .= " ), GET_9902999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX, ";
            $query .= "         COUNT(DISTINCT SCHREGNO) AS CNT9902999 ";
            $query .= "     FROM ";
            $query .= "         AFT_INFO ";
            $query .= "     WHERE ";
            $query .= "         SENKOU_KIND = '0' AND ";
            $query .= "         SCHOOL_GROUP IN ('06','07') AND ";
            $query .= "         SCHREGNO IN (SELECT ";
            $query .= "                         SCHREGNO ";
            $query .= "                     FROM ";
            $query .= "                         AFT_INFO ";
            $query .= "                     WHERE ";
            $query .= "                         SENKOU_KIND = '1' ";
            $query .= "                     ) ";
            $query .= "     GROUP BY ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            //Ｃのうち就職している生徒
            $query .= " ), GET_9903999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX, ";
            $query .= "         COUNT(DISTINCT SCHREGNO) AS CNT9903999 ";
            $query .= "     FROM ";
            $query .= "         AFT_INFO ";
            $query .= "     WHERE ";
            $query .= "         SENKOU_KIND = '0' AND ";
            $query .= "         SCHOOL_GROUP IN ('08','99') AND ";
            $query .= "         SCHREGNO IN (SELECT ";
            $query .= "                         SCHREGNO ";
            $query .= "                     FROM ";
            $query .= "                         AFT_INFO ";
            $query .= "                     WHERE ";
            $query .= "                         SENKOU_KIND = '1' ";
            $query .= "                     ) ";
            $query .= "     GROUP BY ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            //Ｄのうち就職している生徒
            $query .= " ), GET_9904999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX, ";
            $query .= "         COUNT(DISTINCT SCHREGNO) AS CNT9904999 ";
            $query .= "     FROM ";
            $query .= "         AFT_INFO ";
            $query .= "     WHERE ";
            $query .= "         SENKOU_KIND = '0' AND ";
            $query .= "         SCHOOL_GROUP = '09' AND ";
            $query .= "         SCHREGNO IN (SELECT ";
            $query .= "                         SCHREGNO ";
            $query .= "                     FROM ";
            $query .= "                         AFT_INFO ";
            $query .= "                     WHERE ";
            $query .= "                         SENKOU_KIND = '1' ";
            $query .= "                     ) ";
            $query .= "     GROUP BY ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            //入学志願者数（大学）
            $query .= " ), GET_1001999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX, ";
            $query .= "         COUNT(DISTINCT T1.SCHREGNO) AS CNT1001999 ";
            $query .= "     FROM ";
            $query .= "         SCH_INFO AS T1, ";
            $query .= "         AFT_GRAD_COURSE_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T2.YEAR = '".$model->year."' AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.SENKOU_KIND = '0' AND ";
            $query .= "         T2.SCHOOL_GROUP IN ('01','02','03') ";
            $query .= "     GROUP BY ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX ";
            //入学志願者数（短期大学）
            $query .= " ), GET_1002999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX, ";
            $query .= "         COUNT(DISTINCT T1.SCHREGNO) AS CNT1002999 ";
            $query .= "     FROM ";
            $query .= "         SCH_INFO AS T1, ";
            $query .= "         AFT_GRAD_COURSE_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T2.YEAR = '".$model->year."' AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.SENKOU_KIND = '0' AND ";
            $query .= "         T2.SCHOOL_GROUP IN ('04','05') ";
            $query .= "     GROUP BY ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX ";
            //前年度の卒業生を抽出
            $query .= " ), PRE_SCH_INFO1 AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.COURSECD || T1.MAJORCD AS MAJORCD, ";
            $query .= "         T2.SEX ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1, ";
            $query .= "         SCHREG_BASE_MST T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$pre_year1."' AND ";
            $query .= "         T1.SEMESTER = ( SELECT ";
            $query .= "                             MAX(SEMESTER) ";
            $query .= "                         FROM ";
            $query .= "                             SEMESTER_MST ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '".$pre_year1."' AND ";
            $query .= "                             SEMESTER <> '9' ";
            $query .= "                     ) AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.GRD_DIV = '1' AND ";
            $query .= "         FISCALYEAR(T2.GRD_DATE) = '".$pre_year1."' ";
            //前年度の入学志願者（大学）
            $query .= " ), GET_1101999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX, ";
            $query .= "         COUNT(DISTINCT T1.SCHREGNO) AS CNT1101999 ";
            $query .= "     FROM ";
            $query .= "         PRE_SCH_INFO1 AS T1, ";
            $query .= "         AFT_GRAD_COURSE_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T2.YEAR = '".$pre_year1."' AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.SENKOU_KIND = '0' AND ";
            $query .= "         T2.SCHOOL_GROUP IN ('01','02','03') ";
            $query .= "     GROUP BY ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX ";
            //前年度の入学志願者（短期大学）
            $query .= " ), GET_1102999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX, ";
            $query .= "         COUNT(DISTINCT T1.SCHREGNO) AS CNT1102999 ";
            $query .= "     FROM ";
            $query .= "         PRE_SCH_INFO1 AS T1, ";
            $query .= "         AFT_GRAD_COURSE_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T2.YEAR = '".$pre_year1."' AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.SENKOU_KIND = '0' AND ";
            $query .= "         T2.SCHOOL_GROUP IN ('04','05') ";
            $query .= "     GROUP BY ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX ";
            //前々年度の卒業生を抽出
            $query .= " ), PRE_SCH_INFO2 AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T1.COURSECD || T1.MAJORCD AS MAJORCD, ";
            $query .= "         T2.SEX ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1, ";
            $query .= "         SCHREG_BASE_MST T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$pre_year2."' AND ";
            $query .= "         T1.SEMESTER = ( SELECT ";
            $query .= "                             MAX(SEMESTER) ";
            $query .= "                         FROM ";
            $query .= "                             SEMESTER_MST ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '".$pre_year2."' AND ";
            $query .= "                             SEMESTER <> '9' ";
            $query .= "                     ) AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.GRD_DIV = '1' AND ";
            $query .= "         FISCALYEAR(T2.GRD_DATE) = '".$pre_year2."' ";
            //前々年度の入学志願者（大学）
            $query .= " ), GET_1103999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX, ";
            $query .= "         COUNT(DISTINCT T1.SCHREGNO) AS CNT1103999 ";
            $query .= "     FROM ";
            $query .= "         PRE_SCH_INFO2 AS T1, ";
            $query .= "         AFT_GRAD_COURSE_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T2.YEAR = '".$pre_year2."' AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.SENKOU_KIND = '0' AND ";
            $query .= "         T2.SCHOOL_GROUP IN ('01','02','03') ";
            $query .= "     GROUP BY ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX ";
            //前々年度の入学志願者（短期大学）
            $query .= " ), GET_1104999 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX, ";
            $query .= "         COUNT(DISTINCT T1.SCHREGNO) AS CNT1104999 ";
            $query .= "     FROM ";
            $query .= "         PRE_SCH_INFO2 AS T1, ";
            $query .= "         AFT_GRAD_COURSE_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T2.YEAR = '".$pre_year2."' AND ";
            $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "         T2.SENKOU_KIND = '0' AND ";
            $query .= "         T2.SCHOOL_GROUP IN ('04','05') ";
            $query .= "     GROUP BY ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T1.SEX ";
            $query .= " ), CALC_AFT AS ( ";
            $query .= "     SELECT ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            foreach ($model->nameArray as $name) {
                if ($name != "0599999" && substr($name, 0, 1) != "9" && substr($name, 0, 1) != "1") {
                    $query .= "         ,SUM(CNT".$name.") AS CNT".$name ;
                }
            }
            $query .= "     FROM ";
            $query .= "         AFT_DATA ";
            $query .= "     GROUP BY ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            $query .= " ) ";

            //メイン
            $query .= " SELECT ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX, ";
            $query .= "     T1.MAJOR_NAME, ";
            $query .= "     T1.SEX_NAME ";
            foreach ($model->nameArray as $name) {
                $query .= "     ,CNT".$name ;
            }
            $query .= " FROM ";
            $query .= "     T_MAJOR_SEX T1 ";
            $query .= "     LEFT JOIN CALC_AFT AS L1 ON T1.MAJORCD = L1.MAJORCD AND T1.SEX = L1.SEX ";
            $query .= "     LEFT JOIN GET_0599999 AS L2  ON T1.MAJORCD = L2.MAJORCD  AND T1.SEX = L2.SEX ";
            $query .= "     LEFT JOIN GET_9999999 AS L3  ON T1.MAJORCD = L3.MAJORCD  AND T1.SEX = L3.SEX ";
            $query .= "     LEFT JOIN GET_9901999 AS L4  ON T1.MAJORCD = L4.MAJORCD  AND T1.SEX = L4.SEX ";
            $query .= "     LEFT JOIN GET_9902999 AS L5  ON T1.MAJORCD = L5.MAJORCD  AND T1.SEX = L5.SEX ";
            $query .= "     LEFT JOIN GET_9903999 AS L6  ON T1.MAJORCD = L6.MAJORCD  AND T1.SEX = L6.SEX ";
            $query .= "     LEFT JOIN GET_9904999 AS L7  ON T1.MAJORCD = L7.MAJORCD  AND T1.SEX = L7.SEX ";
            $query .= "     LEFT JOIN GET_1001999 AS L8  ON T1.MAJORCD = L8.MAJORCD  AND T1.SEX = L8.SEX ";
            $query .= "     LEFT JOIN GET_1002999 AS L9  ON T1.MAJORCD = L9.MAJORCD  AND T1.SEX = L9.SEX ";
            $query .= "     LEFT JOIN GET_1101999 AS L10 ON T1.MAJORCD = L10.MAJORCD AND T1.SEX = L10.SEX ";
            $query .= "     LEFT JOIN GET_1102999 AS L11 ON T1.MAJORCD = L11.MAJORCD AND T1.SEX = L11.SEX ";
            $query .= "     LEFT JOIN GET_1103999 AS L12 ON T1.MAJORCD = L12.MAJORCD AND T1.SEX = L12.SEX ";
            $query .= "     LEFT JOIN GET_1104999 AS L13 ON T1.MAJORCD = L13.MAJORCD AND T1.SEX = L13.SEX ";
            $query .= " ORDER BY ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX ";
        }

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = " DELETE FROM AFT_DISEASE_ADDITION440_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->schoolcd."'";
        $query .= "   AND YEAR = '".$model->year."'";
        $db->query($query);

        //INSERT
        for ($i = 0; $i < get_count($model->fields["CODE"]); $i++) {
            //課程学科・性別
            $code_arr = explode("-", $model->fields["CODE"][$i]);

            foreach ($model->nameArray as $name) {
                $cnt_name = "CNT".$name;

                //キー
                $data = array(); //初期化
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                $data["YEAR"][TEXT]                 = $model->year;
                $data["COURSECD"][TEXT]             = substr($code_arr[0], 0, 1);
                $data["MAJORCD"][TEXT]              = substr($code_arr[0], 1);
                $data["SEX"][TEXT]                  = $code_arr[1];
                $data["LARGE_DIV"][TEXT]            = substr($name, 0, 2);
                $data["MIDDLE_DIV"][TEXT]           = substr($name, 2, 2);
                $data["SMALL_DIV"][TEXT]            = substr($name, 4);
                $data["COUNT"][NUMBER]              = ($model->fields[$cnt_name][$i] > 0) ? $model->fields[$cnt_name][$i] : 0;

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            =" sysdate()";

                $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION440_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告履歴コンボ
    public function getReport($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     T1.MAJORCD || ':' || T1.MAJORCD || ':' || CHAR(T1.EXECUTE_DATE) AS VALUE, ";
        $query .= "     L1.MAJORNAME || '　' || RTRIM(CHAR(REPLACE(CHAR(T1.EXECUTE_DATE),'-','/'))) AS LABEL ";
        $query .= " FROM ";
        $query .= "     REPORT_AFT_DISEASE_ADDITION440_DAT T1";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST L1 ";
        $query .= "         ON  L1.YEAR     = T1.YEAR ";
        $query .= "         AND L1.COURSECD = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD  = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（進路統計）
    public function getAdditionDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION440_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD ";

        return $query;
    }

    //UPDATE
    public function &getUpdateReport($model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //DELETE
        //県側・進路統計テーブル
        $query  = "DELETE FROM AFT_DISEASE_ADDITION440_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->year."'";
        $db2->query($query);
        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION440_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->year."'";
        $db->query($query);
        $db2->query($query);

        $course_major = "";

        //INSERT
        //学校側から県側へコピーするデータ取得（進路統計）
        $query = knje440Query::getAdditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・進路統計テーブル
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["LARGE_DIV"][TEXT]            = $row["LARGE_DIV"];
            $data["MIDDLE_DIV"][TEXT]           = $row["MIDDLE_DIV"];
            $data["SMALL_DIV"][TEXT]            = $row["SMALL_DIV"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["COUNT"][NUMBER]              = $row["COUNT"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION440_DAT");
            $db2->query($query);

            if ($row["COURSECD"].$row["MAJORCD"] != $course_major) {
                //県側、学校側・報告テーブル
                //キー
                $data = array(); //初期化
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
                $data["YEAR"][TEXT]                 = $row["YEAR"];
                $data["COURSECD"][TEXT]             = $row["COURSECD"];
                $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
                $data["EXECUTE_DATE"][TEXT]         = str_replace("/", "-", $model->execute_date);
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION440_DAT");
                $db->query($query);
                $db2->query($query);

                $course_major = $row["COURSECD"].$row["MAJORCD"];
            }
        }
        $result->free();
        
        //報告処理
        $data = array();
        $data["ANSWER_FLG"][TEXT]      = "1";
        $data["ANSWER_DATE"][FUNC]     = "SYSDATE()";
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->year."' ";
        $where .= "     AND DOC_NUMBER = {$model->field["DOC_NUMBER"]} ";
        $where .= "     AND EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";

        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
        $db2->query($query);

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }
    
    //CSVデータ処理
    //更新データチェック処理
    public function upCheckQuery($model, $setData, $largeDiv, $middleDiv, $smallDiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM  ";
        $query .= "     AFT_DISEASE_ADDITION440_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->year."'";
        $query .= "   AND COURSECD          = '".$setData["COURSECD"]."'";
        $query .= "   AND MAJORCD           = '".$setData["MAJORCD"]."'";
        $query .= "   AND SEX               = '".$setData["SEX"]."'";
        $query .= "   AND LARGE_DIV         = '".$largeDiv."'";
        $query .= "   AND MIDDLE_DIV        = '".$middleDiv."'";
        $query .= "   AND SMALL_DIV         = '".$smallDiv."'";
        
        return $query;
    }

    //共通処理
    public function insertQuery($db, $model, $setData, $largeDiv, $middleDiv, $smallDiv, $setName)
    {
        //データチェック
        $getCount = $db->getOne(knje440Query::upCheckQuery($model, $setData, $largeDiv, $middleDiv, $smallDiv));
        
        $data = array(); //初期化
        $data["COUNT"][NUMBER]              = $setData[$setName];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        if ($getCount == 0) {
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = $model->year;
            $data["COURSECD"][TEXT]             = $setData["COURSECD"];
            $data["MAJORCD"][TEXT]              = $setData["MAJORCD"];
            $data["SEX"][TEXT]                  = $setData["SEX"];
            $data["LARGE_DIV"][TEXT]            = $largeDiv;
            $data["MIDDLE_DIV"][TEXT]           = $middleDiv;
            $data["SMALL_DIV"][TEXT]            = $smallDiv;
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION440_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
            $where .= "   AND YEAR              = '".$model->year."'";
            $where .= "   AND COURSECD          = '".$setData["COURSECD"]."'";
            $where .= "   AND MAJORCD           = '".$setData["MAJORCD"]."'";
            $where .= "   AND SEX               = '".$setData["SEX"]."'";
            $where .= "   AND LARGE_DIV         = '".$largeDiv."'";
            $where .= "   AND MIDDLE_DIV        = '".$middleDiv."'";
            $where .= "   AND SMALL_DIV         = '".$smallDiv."'";
            $query  = Query::updateSQL($data, "AFT_DISEASE_ADDITION440_DAT", $where);
            $db->query($query);
        }
        
        return;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            foreach ($model->nameArray as $name) {
                $cnt_name = "CNT".$name;
                knje440Query::insertQuery($db, $model, $data_arr[$i], substr($name, 0, 2), substr($name, 2, 2), substr($name, 4), $cnt_name);
            }
            $cnt++;
        }
        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
