<?php

require_once('for_php7.php');
class knje420Query extends Query
{

    //教育委員会用の学校コード取得
    public function getSchoolMst($model)
    {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD, ";
        $query .= "      PREF_CD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".$model->field["YEAR"]."' ";

        return $query;
    }

    //年度取得
    public function getYear()
    {
        $query  = " SELECT ";
        $query .= "      YEAR AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "      SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "      VALUE DESC ";

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
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND L1.REQUEST_ANSWER_PRG = 'KNJE420' ";
        $query .= "     AND L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //データ表示
    public function readQuery($model, $flg = "")
    {
        $query  = "";
        //学科・性別
        $query .= " WITH T_MAJOR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.COURSECD , ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T3.MAJORNAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         INNER JOIN MAJOR_MST T3 ";
        $query .= "             ON  T3.COURSECD     = T1.COURSECD ";
        $query .= "            AND  T3.MAJORCD     = T1.MAJORCD ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.COURSECD , ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T3.MAJORNAME ";
        $query .= "     ) ";
        $query .= " , T_SEX (SEX, SEX_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'Z002' ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('9', '合計') ";
        $query .= "     ) ";
        $query .= " , T_MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         T_MAJOR T1, ";
        $query .= "         T_SEX T2 ";
        $query .= "     ) ";
        //再計算実行時
        $query .= " , T_ADDITION1 AS ( ";
        if ($model->cmd == "recalc") {
            //再計算
            $query .= "     SELECT ";
            $query .= "         S2.COURSECD, ";
            $query .= "         S2.MAJORCD, ";
            $query .= "         S1.SEX, ";
            //進学希望者
            $query .= "         COUNT(L1.SCHREGNO) AS SHINGAKU_GOUKEI, ";
            //就職希望者(学校)
            $query .= "         SUM(CASE WHEN W1.PREF_CD = '".$model->prefcd."' THEN 1 ELSE 0 END) AS SHUSHOKU_KIBOU_KENNAI_GAKKOU, ";
            $query .= "         SUM(CASE WHEN W1.PREF_CD <> '".$model->prefcd."' AND INT(W1.PREF_CD) > 0 AND INT(W1.PREF_CD) <= 47 THEN 1 ELSE 0 END) AS SHUSHOKU_KIBOU_KENGAI_GAKKOU, ";
            //就職希望者(自己・縁故)
            $query .= "         SUM(CASE WHEN W2.PREF_CD = '".$model->prefcd."' THEN 1 ELSE 0 END) AS SHUSHOKU_KIBOU_KENNAI_JIBUN, ";
            $query .= "         SUM(CASE WHEN W2.PREF_CD <> '".$model->prefcd."' AND INT(W2.PREF_CD) > 0 AND INT(W2.PREF_CD) <= 47 THEN 1 ELSE 0 END) AS SHUSHOKU_KIBOU_KENGAI_JIBUN, ";
            //就職希望者(公務員)
            $query .= "         SUM(CASE WHEN W3.PREF_CD = '".$model->prefcd."' THEN 1 ELSE 0 END) AS SHUSHOKU_KIBOU_KENNAI_KOUMUIN, ";
            $query .= "         SUM(CASE WHEN W3.PREF_CD <> '".$model->prefcd."' AND INT(W3.PREF_CD) > 0 AND INT(W3.PREF_CD) <= 47 THEN 1 ELSE 0 END) AS SHUSHOKU_KIBOU_KENGAI_KOUMUIN, ";
            //その他
            $query .= "         SUM(CASE WHEN W4.SENKOU_KIND_SUB IS NOT NULL THEN 1 ELSE 0 END) AS SONOTA_KEIKAKU_ARI, ";
            $query .= "         SUM(CASE WHEN W4.SENKOU_KIND_SUB IS NULL THEN 1 ELSE 0 END) AS SONOTA_KEIKAKU_NASHI, ";
            //就職内定者(学校)
            $query .= "         SUM(CASE WHEN K1.PREF_CD = '".$model->prefcd."' THEN 1 ELSE 0 END) AS SHUSHOKU_NAITEI_KENNAI_GAKKOU, ";
            $query .= "         SUM(CASE WHEN K1.PREF_CD <> '".$model->prefcd."' AND INT(K1.PREF_CD) > 0 AND INT(K1.PREF_CD) <= 47 THEN 1 ELSE 0 END) AS SHUSHOKU_NAITEI_KENGAI_GAKKOU, ";
            //就職内定者(自己・縁故)
            $query .= "         SUM(CASE WHEN K2.PREF_CD = '".$model->prefcd."' THEN 1 ELSE 0 END) AS SHUSHOKU_NAITEI_KENNAI_JIBUN, ";
            $query .= "         SUM(CASE WHEN K2.PREF_CD <> '".$model->prefcd."' AND INT(K2.PREF_CD) > 0 AND INT(K2.PREF_CD) <= 47 THEN 1 ELSE 0 END) AS SHUSHOKU_NAITEI_KENGAI_JIBUN, ";
            //就職内定者(公務員)
            $query .= "         SUM(CASE WHEN K3.PREF_CD = '".$model->prefcd."' THEN 1 ELSE 0 END) AS SHUSHOKU_NAITEI_KENNAI_KOUMUIN, ";
            $query .= "         SUM(CASE WHEN K3.PREF_CD <> '".$model->prefcd."' AND INT(K3.PREF_CD) > 0 AND INT(K3.PREF_CD) <= 47 THEN 1 ELSE 0 END) AS SHUSHOKU_NAITEI_KENGAI_KOUMUIN, ";
            //進学就職
            $query .= "         COUNT(SS2.SCHREGNO) AS SHINGAKU_SHUSHOKU, ";
            //就職希望者の内未内定者
            $query .= "         SUM(CASE WHEN N1.PREF_CD = '".$model->prefcd."' THEN 1 ELSE 0 END) AS SHUSHOKU_KIBOU_KENNAI_MINAITEI, ";
            $query .= "         SUM(CASE WHEN N1.PREF_CD <> '".$model->prefcd."' AND INT(N1.PREF_CD) > 0 AND INT(N1.PREF_CD) <= 47 THEN 1 ELSE 0 END) AS SHUSHOKU_KIBOU_KENGAI_MINAITEI, ";
            //進路希望以外の進路未決定者
            $query .= "         COUNT(N2.SCHREGNO) AS SHINGAKU_IGAI_MIKKETEI ";
            $query .= "     FROM ";
            $query .= "         AFT_GRAD_COURSE_DAT T1";
            $query .= "     INNER JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT S2 ON S2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                  AND S2.YEAR = T1.YEAR ";
            $query .= "                                  AND S2.SEMESTER = '".CTRL_SEMESTER."' ";
            //進学希望者
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT L1 ON L1.YEAR = T1.YEAR ";
            $query .= "                                     AND L1.SEQ = T1.SEQ ";
            $query .= "                                     AND L1.SENKOU_KIND = '0' ";
            //就職希望者(学校紹介)
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT W1 ON W1.YEAR = T1.YEAR ";
            $query .= "                                     AND W1.SEQ = T1.SEQ ";
            $query .= "                                     AND W1.SENKOU_KIND = '1' ";
            $query .= "                                     AND W1.INTRODUCTION_DIV = '1' ";
            //就職希望者(自己・縁故)
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT W2 ON W2.YEAR = T1.YEAR ";
            $query .= "                                     AND W2.SEQ = T1.SEQ ";
            $query .= "                                     AND W2.SENKOU_KIND = '1' ";
            $query .= "                                     AND W2.INTRODUCTION_DIV = '2' ";
            //就職希望者(公務員)
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT W3 ON W3.YEAR = T1.YEAR ";
            $query .= "                                     AND W3.SEQ = T1.SEQ ";
            $query .= "                                     AND W3.SENKOU_KIND = '1' ";
            $query .= "                                     AND W3.INTRODUCTION_DIV = '3' ";
            //その他
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT W4 ON W4.YEAR = T1.YEAR ";
            $query .= "                                     AND W4.SEQ = T1.SEQ ";
            $query .= "                                     AND W4.SENKOU_KIND = '1' ";
            $query .= "                                     AND W4.INTRODUCTION_DIV NOT IN ('1', '2', '3') ";
            //就職内定者(学校紹介)
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT K1 ON K1.YEAR = T1.YEAR ";
            $query .= "                                     AND K1.SEQ = T1.SEQ ";
            $query .= "                                     AND K1.SENKOU_KIND = '1' ";
            $query .= "                                     AND K1.INTRODUCTION_DIV = '1' ";
            $query .= "                                     AND K1.PLANSTAT = '1' ";//1.決定
            //就職内定者(自己・縁故)
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT K2 ON K2.YEAR = T1.YEAR ";
            $query .= "                                     AND K2.SEQ = T1.SEQ ";
            $query .= "                                     AND K2.SENKOU_KIND = '1' ";
            $query .= "                                     AND K2.INTRODUCTION_DIV = '2' ";
            $query .= "                                     AND K2.PLANSTAT = '1' ";//1.決定
            //就職内定者(公務員)
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT K3 ON K3.YEAR = T1.YEAR ";
            $query .= "                                     AND K3.SEQ = T1.SEQ ";
            $query .= "                                     AND K3.SENKOU_KIND = '1' ";
            $query .= "                                     AND K3.INTRODUCTION_DIV = '3' ";
            $query .= "                                     AND K3.PLANSTAT = '2' ";//1.決定
            //進学就職(進学、就職ともにある人)
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT SS1 ON SS1.YEAR = T1.YEAR ";
            $query .= "                                      AND SS1.SEQ = T1.SEQ ";
            $query .= "                                      AND SS1.SENKOU_KIND = '0' ";
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT SS2 ON SS2.YEAR = SS1.YEAR ";
            $query .= "                                      AND SS2.SCHREGNO = SS1.SCHREGNO ";
            $query .= "                                      AND SS2.SENKOU_KIND = '1' ";
            //就職希望者の内未内定者
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT N1 ON N1.YEAR = T1.YEAR ";
            $query .= "                                     AND N1.SEQ = T1.SEQ ";
            $query .= "                                     AND N1.SENKOU_KIND = '1' ";//1.会社
            $query .= "                                     AND N1.PLANSTAT = '3' ";//3.未定
            //進路希望以外の進路未決定者
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT N2 ON N2.YEAR = T1.YEAR ";
            $query .= "                                     AND N2.SEQ = T1.SEQ ";
            $query .= "                                     AND N2.SENKOU_KIND <> '0' ";//0.学校 以外
            $query .= "                                     AND N2.PLANSTAT = '3' ";//3.未定
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     GROUP BY ";
            $query .= "         S2.COURSECD, ";
            $query .= "         S2.MAJORCD, ";
            $query .= "         S1.SEX ";
            $query .= "     ) ";
            //再計算時の進学、就職の合計
            $query .= " , T_ADDITION2 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.*, ";
            //就職希望者数の各合計
            $query .= "         T1.SHUSHOKU_KIBOU_KENNAI_GAKKOU + T1.SHUSHOKU_KIBOU_KENGAI_GAKKOU AS SHUSHOKU_KIBOU_TOTAL_GAKKOU, ";
            $query .= "         T1.SHUSHOKU_KIBOU_KENNAI_JIBUN + T1.SHUSHOKU_KIBOU_KENGAI_JIBUN AS SHUSHOKU_KIBOU_TOTAL_JIBUN, ";
            $query .= "         T1.SHUSHOKU_KIBOU_KENNAI_KOUMUIN + T1.SHUSHOKU_KIBOU_KENGAI_KOUMUIN AS SHUSHOKU_KIBOU_TOTAL_KOUMUIN, ";
            //就職希望者数県内、県外合計
            $query .= "         T1.SHUSHOKU_KIBOU_KENNAI_GAKKOU + T1.SHUSHOKU_KIBOU_KENNAI_JIBUN + T1.SHUSHOKU_KIBOU_KENNAI_KOUMUIN AS SHUSHOKU_KIBOU_KENNAI_GOUKEI, ";
            $query .= "         T1.SHUSHOKU_KIBOU_KENGAI_GAKKOU + T1.SHUSHOKU_KIBOU_KENGAI_JIBUN + T1.SHUSHOKU_KIBOU_KENGAI_KOUMUIN AS SHUSHOKU_KIBOU_KENGAI_GOUKEI, ";
            //就職内定者数の各合計
            $query .= "         T1.SHUSHOKU_NAITEI_KENNAI_GAKKOU + T1.SHUSHOKU_NAITEI_KENGAI_GAKKOU AS SHUSHOKU_NAITEI_TOTAL_GAKKOU, ";
            $query .= "         T1.SHUSHOKU_NAITEI_KENNAI_JIBUN + T1.SHUSHOKU_NAITEI_KENGAI_JIBUN AS SHUSHOKU_NAITEI_TOTAL_JIBUN, ";
            $query .= "         T1.SHUSHOKU_NAITEI_KENNAI_KOUMUIN + T1.SHUSHOKU_NAITEI_KENGAI_KOUMUIN AS SHUSHOKU_NAITEI_TOTAL_KOUMUIN, ";
            //就職内定者数県内、県外合計
            $query .= "         T1.SHUSHOKU_NAITEI_KENNAI_GAKKOU + T1.SHUSHOKU_NAITEI_KENNAI_JIBUN + T1.SHUSHOKU_NAITEI_KENNAI_KOUMUIN AS SHUSHOKU_NAITEI_KENNAI_GOUKEI, ";
            $query .= "         T1.SHUSHOKU_NAITEI_KENGAI_GAKKOU + T1.SHUSHOKU_NAITEI_KENGAI_JIBUN + T1.SHUSHOKU_NAITEI_KENGAI_KOUMUIN AS SHUSHOKU_NAITEI_KENGAI_GOUKEI, ";
            //就職未内定者の合計
            $query .= "         T1.SHUSHOKU_KIBOU_KENNAI_MINAITEI + T1.SHUSHOKU_KIBOU_KENGAI_MINAITEI AS SHUSHOKU_KIBOU_TOTAL_MINAITEI ";
            $query .= "     FROM ";
            $query .= "         T_ADDITION1 T1";
            $query .= "     ) ";
            $query .= " , T_ADDITION3 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.*, ";
            //卒業予定者数
            $query .= "         T1.SHINGAKU_GOUKEI + T1.SHUSHOKU_KIBOU_TOTAL_GAKKOU + T1.SHUSHOKU_KIBOU_TOTAL_JIBUN + T1.SHUSHOKU_KIBOU_TOTAL_KOUMUIN + T1.SONOTA_KEIKAKU_ARI + T1.SONOTA_KEIKAKU_NASHI AS ZENTAI_GOUKEI, ";
            //就職希望者数合計
            $query .= "         T1.SHUSHOKU_KIBOU_KENNAI_GOUKEI + T1.SHUSHOKU_KIBOU_KENGAI_GOUKEI AS SHUSHOKU_KIBOU_TOTAL_GOUKEI, ";
            //就職内定者数合計
            $query .= "         T1.SHUSHOKU_NAITEI_KENNAI_GOUKEI + T1.SHUSHOKU_NAITEI_KENGAI_GOUKEI AS SHUSHOKU_NAITEI_TOTAL_GOUKEI ";
            $query .= "     FROM ";
            $query .= "         T_ADDITION2 T1 ";
            //男データ取得
            $query .= "  ), MAN_DATA AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX, ";
            $query .= "     T1.MAJORNAME, ";
            $query .= "     T1.SEX_NAME, ";
            $query .= "     VALUE(L1.ZENTAI_GOUKEI, 0) AS ZENTAI_GOUKEI, ";
            $query .= "     VALUE(L1.SHINGAKU_GOUKEI, 0) AS SHINGAKU_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_GAKKOU, 0) AS SHUSHOKU_KIBOU_TOTAL_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_GAKKOU, 0) AS SHUSHOKU_KIBOU_KENNAI_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_GAKKOU, 0) AS SHUSHOKU_KIBOU_KENGAI_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_JIBUN, 0) AS SHUSHOKU_KIBOU_TOTAL_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_JIBUN, 0) AS SHUSHOKU_KIBOU_KENNAI_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_JIBUN, 0) AS SHUSHOKU_KIBOU_KENGAI_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_KOUMUIN, 0) AS SHUSHOKU_KIBOU_TOTAL_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_KOUMUIN, 0) AS SHUSHOKU_KIBOU_KENNAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_KOUMUIN, 0) AS SHUSHOKU_KIBOU_KENGAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_GOUKEI, 0) AS SHUSHOKU_KIBOU_TOTAL_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_GOUKEI, 0) AS SHUSHOKU_KIBOU_KENNAI_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_GOUKEI, 0) AS SHUSHOKU_KIBOU_KENGAI_GOUKEI, ";
            $query .= "     VALUE(L1.SONOTA_KEIKAKU_ARI, 0) AS SONOTA_KEIKAKU_ARI, ";
            $query .= "     VALUE(L1.SONOTA_KEIKAKU_NASHI, 0) AS SONOTA_KEIKAKU_NASHI, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_GAKKOU, 0) AS SHUSHOKU_NAITEI_TOTAL_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_GAKKOU, 0) AS SHUSHOKU_NAITEI_KENNAI_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_GAKKOU, 0) AS SHUSHOKU_NAITEI_KENGAI_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_JIBUN, 0) AS SHUSHOKU_NAITEI_TOTAL_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_JIBUN, 0) AS SHUSHOKU_NAITEI_KENNAI_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_JIBUN, 0) AS SHUSHOKU_NAITEI_KENGAI_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_KOUMUIN, 0) AS SHUSHOKU_NAITEI_TOTAL_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_KOUMUIN, 0) AS SHUSHOKU_NAITEI_KENNAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_KOUMUIN, 0) AS SHUSHOKU_NAITEI_KENGAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_GOUKEI, 0) AS SHUSHOKU_NAITEI_TOTAL_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_GOUKEI, 0) AS SHUSHOKU_NAITEI_KENNAI_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_GOUKEI, 0) AS SHUSHOKU_NAITEI_KENGAI_GOUKEI, ";
            $query .= "     VALUE(L1.SHINGAKU_SHUSHOKU, 0) AS SHINGAKU_SHUSHOKU, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_MINAITEI, 0) AS SHUSHOKU_KIBOU_TOTAL_MINAITEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_MINAITEI, 0) AS SHUSHOKU_KIBOU_KENNAI_MINAITEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_MINAITEI, 0) AS SHUSHOKU_KIBOU_KENGAI_MINAITEI, ";
            $query .= "     VALUE(L1.SHINGAKU_IGAI_MIKKETEI, 0) AS SHINGAKU_IGAI_MIKKETEI ";
            $query .= " FROM ";
            $query .= "     T_MAIN T1 ";
            $query .= "     LEFT JOIN T_ADDITION3 L1 ON L1.COURSECD = T1.COURSECD ";
            $query .= "                             AND L1.MAJORCD = T1.MAJORCD ";
            $query .= "                             AND L1.SEX = '1' ";
            //女データ取得
            $query .= " ), WOMAN_DATA AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX, ";
            $query .= "     T1.MAJORNAME, ";
            $query .= "     T1.SEX_NAME, ";
            $query .= "     VALUE(L1.ZENTAI_GOUKEI, 0) AS ZENTAI_GOUKEI, ";
            $query .= "     VALUE(L1.SHINGAKU_GOUKEI, 0) AS SHINGAKU_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_GAKKOU, 0) AS SHUSHOKU_KIBOU_TOTAL_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_GAKKOU, 0) AS SHUSHOKU_KIBOU_KENNAI_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_GAKKOU, 0) AS SHUSHOKU_KIBOU_KENGAI_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_JIBUN, 0) AS SHUSHOKU_KIBOU_TOTAL_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_JIBUN, 0) AS SHUSHOKU_KIBOU_KENNAI_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_JIBUN, 0) AS SHUSHOKU_KIBOU_KENGAI_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_KOUMUIN, 0) AS SHUSHOKU_KIBOU_TOTAL_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_KOUMUIN, 0) AS SHUSHOKU_KIBOU_KENNAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_KOUMUIN, 0) AS SHUSHOKU_KIBOU_KENGAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_GOUKEI, 0) AS SHUSHOKU_KIBOU_TOTAL_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_GOUKEI, 0) AS SHUSHOKU_KIBOU_KENNAI_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_GOUKEI, 0) AS SHUSHOKU_KIBOU_KENGAI_GOUKEI, ";
            $query .= "     VALUE(L1.SONOTA_KEIKAKU_ARI, 0) AS SONOTA_KEIKAKU_ARI, ";
            $query .= "     VALUE(L1.SONOTA_KEIKAKU_NASHI, 0) AS SONOTA_KEIKAKU_NASHI, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_GAKKOU, 0) AS SHUSHOKU_NAITEI_TOTAL_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_GAKKOU, 0) AS SHUSHOKU_NAITEI_KENNAI_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_GAKKOU, 0) AS SHUSHOKU_NAITEI_KENGAI_GAKKOU, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_JIBUN, 0) AS SHUSHOKU_NAITEI_TOTAL_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_JIBUN, 0) AS SHUSHOKU_NAITEI_KENNAI_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_JIBUN, 0) AS SHUSHOKU_NAITEI_KENGAI_JIBUN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_KOUMUIN, 0) AS SHUSHOKU_NAITEI_TOTAL_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_KOUMUIN, 0) AS SHUSHOKU_NAITEI_KENNAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_KOUMUIN, 0) AS SHUSHOKU_NAITEI_KENGAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_GOUKEI, 0) AS SHUSHOKU_NAITEI_TOTAL_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_GOUKEI, 0) AS SHUSHOKU_NAITEI_KENNAI_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_GOUKEI, 0) AS SHUSHOKU_NAITEI_KENGAI_GOUKEI, ";
            $query .= "     VALUE(L1.SHINGAKU_SHUSHOKU, 0) AS SHINGAKU_SHUSHOKU, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_MINAITEI, 0) AS SHUSHOKU_KIBOU_TOTAL_MINAITEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_MINAITEI, 0) AS SHUSHOKU_KIBOU_KENNAI_MINAITEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_MINAITEI, 0) AS SHUSHOKU_KIBOU_KENGAI_MINAITEI, ";
            $query .= "     VALUE(L1.SHINGAKU_IGAI_MIKKETEI, 0) AS SHINGAKU_IGAI_MIKKETEI ";
            $query .= " FROM ";
            $query .= "     T_MAIN T1 ";
            $query .= "     LEFT JOIN T_ADDITION3 L1 ON L1.COURSECD = T1.COURSECD ";
            $query .= "                             AND L1.MAJORCD = T1.MAJORCD ";
            $query .= "                             AND L1.SEX = '2' ";
            //合計データ設定
            $query .= " ), GOUKEI AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     L1.COURSECD, ";
            $query .= "     L1.MAJORCD, ";
            $query .= "     L1.SEX, ";
            $query .= "     L1.MAJORNAME, ";
            $query .= "     L1.SEX_NAME, ";
            $query .= "     L1.ZENTAI_GOUKEI + L2.ZENTAI_GOUKEI AS ZENTAI_GOUKEI, ";
            $query .= "     L1.SHINGAKU_GOUKEI + L2.SHINGAKU_GOUKEI AS SHINGAKU_GOUKEI, ";
            $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_GAKKOU + L2.SHUSHOKU_KIBOU_TOTAL_GAKKOU AS SHUSHOKU_KIBOU_TOTAL_GAKKOU, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_GAKKOU + L2.SHUSHOKU_KIBOU_KENNAI_GAKKOU AS SHUSHOKU_KIBOU_KENNAI_GAKKOU, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_GAKKOU + L2.SHUSHOKU_KIBOU_KENGAI_GAKKOU AS SHUSHOKU_KIBOU_KENGAI_GAKKOU, ";
            $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_JIBUN  + L2.SHUSHOKU_KIBOU_TOTAL_JIBUN AS SHUSHOKU_KIBOU_TOTAL_JIBUN, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_JIBUN + L2.SHUSHOKU_KIBOU_KENNAI_JIBUN AS SHUSHOKU_KIBOU_KENNAI_JIBUN, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_JIBUN + L2.SHUSHOKU_KIBOU_KENGAI_JIBUN AS SHUSHOKU_KIBOU_KENGAI_JIBUN, ";
            $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_KOUMUIN + L2.SHUSHOKU_KIBOU_TOTAL_KOUMUIN AS SHUSHOKU_KIBOU_TOTAL_KOUMUIN, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_KOUMUIN + L2.SHUSHOKU_KIBOU_KENNAI_KOUMUIN AS SHUSHOKU_KIBOU_KENNAI_KOUMUIN, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_KOUMUIN + L2.SHUSHOKU_KIBOU_KENGAI_KOUMUIN AS SHUSHOKU_KIBOU_KENGAI_KOUMUIN, ";
            $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_GOUKEI + L2.SHUSHOKU_KIBOU_TOTAL_GOUKEI AS SHUSHOKU_KIBOU_TOTAL_GOUKEI, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_GOUKEI + L2.SHUSHOKU_KIBOU_KENNAI_GOUKEI AS SHUSHOKU_KIBOU_KENNAI_GOUKEI, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_GOUKEI + L2.SHUSHOKU_KIBOU_KENGAI_GOUKEI AS SHUSHOKU_KIBOU_KENGAI_GOUKEI, ";
            $query .= "     L1.SONOTA_KEIKAKU_ARI + L2.SONOTA_KEIKAKU_ARI AS SONOTA_KEIKAKU_ARI, ";
            $query .= "     L1.SONOTA_KEIKAKU_NASHI + L2.SONOTA_KEIKAKU_NASHI AS SONOTA_KEIKAKU_NASHI, ";
            $query .= "     L1.SHUSHOKU_NAITEI_TOTAL_GAKKOU + L2.SHUSHOKU_NAITEI_TOTAL_GAKKOU AS SHUSHOKU_NAITEI_TOTAL_GAKKOU, ";
            $query .= "     L1.SHUSHOKU_NAITEI_KENNAI_GAKKOU + L2.SHUSHOKU_NAITEI_KENNAI_GAKKOU AS SHUSHOKU_NAITEI_KENNAI_GAKKOU, ";
            $query .= "     L1.SHUSHOKU_NAITEI_KENGAI_GAKKOU + L2.SHUSHOKU_NAITEI_KENGAI_GAKKOU AS SHUSHOKU_NAITEI_KENGAI_GAKKOU, ";
            $query .= "     L1.SHUSHOKU_NAITEI_TOTAL_JIBUN + L2.SHUSHOKU_NAITEI_TOTAL_JIBUN AS SHUSHOKU_NAITEI_TOTAL_JIBUN, ";
            $query .= "     L1.SHUSHOKU_NAITEI_KENNAI_JIBUN + L2.SHUSHOKU_NAITEI_KENNAI_JIBUN AS SHUSHOKU_NAITEI_KENNAI_JIBUN, ";
            $query .= "     L1.SHUSHOKU_NAITEI_KENGAI_JIBUN + L2.SHUSHOKU_NAITEI_KENGAI_JIBUN AS SHUSHOKU_NAITEI_KENGAI_JIBUN, ";
            $query .= "     L1.SHUSHOKU_NAITEI_TOTAL_KOUMUIN + L2.SHUSHOKU_NAITEI_TOTAL_KOUMUIN AS SHUSHOKU_NAITEI_TOTAL_KOUMUIN, ";
            $query .= "     L1.SHUSHOKU_NAITEI_KENNAI_KOUMUIN + L2.SHUSHOKU_NAITEI_KENNAI_KOUMUIN AS SHUSHOKU_NAITEI_KENNAI_KOUMUIN, ";
            $query .= "     L1.SHUSHOKU_NAITEI_KENGAI_KOUMUIN + L2.SHUSHOKU_NAITEI_KENGAI_KOUMUIN AS SHUSHOKU_NAITEI_KENGAI_KOUMUIN, ";
            $query .= "     L1.SHUSHOKU_NAITEI_TOTAL_GOUKEI + L2.SHUSHOKU_NAITEI_TOTAL_GOUKEI AS SHUSHOKU_NAITEI_TOTAL_GOUKEI, ";
            $query .= "     L1.SHUSHOKU_NAITEI_KENNAI_GOUKEI + L2.SHUSHOKU_NAITEI_KENNAI_GOUKEI AS SHUSHOKU_NAITEI_KENNAI_GOUKEI, ";
            $query .= "     L1.SHUSHOKU_NAITEI_KENGAI_GOUKEI + L2.SHUSHOKU_NAITEI_KENGAI_GOUKEI AS SHUSHOKU_NAITEI_KENGAI_GOUKEI, ";
            $query .= "     L1.SHINGAKU_SHUSHOKU + L2.SHINGAKU_SHUSHOKU AS SHINGAKU_SHUSHOKU, ";
            $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_MINAITEI + L2.SHUSHOKU_KIBOU_TOTAL_MINAITEI AS SHUSHOKU_KIBOU_TOTAL_MINAITEI, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_MINAITEI + L2.SHUSHOKU_KIBOU_KENNAI_MINAITEI AS SHUSHOKU_KIBOU_KENNAI_MINAITEI, ";
            $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_MINAITEI + L2.SHUSHOKU_KIBOU_KENGAI_MINAITEI AS SHUSHOKU_KIBOU_KENGAI_MINAITEI, ";
            $query .= "     L1.SHINGAKU_IGAI_MIKKETEI + L2.SHINGAKU_IGAI_MIKKETEI AS SHINGAKU_IGAI_MIKKETEI ";
            $query .= " FROM ";
            $query .= "     MAN_DATA L1 ";
            $query .= "     LEFT JOIN WOMAN_DATA L2  ON L1.COURSECD = L2.COURSECD ";
            $query .= "                             AND L1.MAJORCD = L2.MAJORCD ";
            $query .= "                             AND L1.SEX = L2.SEX ";
            $query .= "                             AND L1.MAJORNAME = L2.MAJORNAME ";
            $query .= "                             AND L1.SEX_NAME = L2.SEX_NAME ";
        //通常時
        } else {
            $query .= "     SELECT ";
            $query .= "         * ";
            $query .= "     FROM ";
            $query .= "         V_AFT_DISEASE_ADDITION420_DAT ";
            $query .= "     WHERE ";
            $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "         AND YEAR = '".$model->field["YEAR"]."' ";
        }
        $query .= "     ) ";
        //メイン
        $query .= " SELECT DISTINCT ";
        if ($flg) {
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX, ";
        } else {
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX, ";
            $query .= "     T1.MAJORNAME, ";
            $query .= "     T1.SEX_NAME, ";
        }
        $query .= "     L1.ZENTAI_GOUKEI, ";
        $query .= "     L1.SHINGAKU_GOUKEI, ";
        $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_GAKKOU, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_GAKKOU, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_GAKKOU, ";
        $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_JIBUN, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_JIBUN, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_JIBUN, ";
        $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_KOUMUIN, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_KOUMUIN, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_KOUMUIN, ";
        $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_GOUKEI, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_GOUKEI, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_GOUKEI, ";
        $query .= "     L1.SONOTA_KEIKAKU_ARI, ";
        $query .= "     L1.SONOTA_KEIKAKU_NASHI, ";
        $query .= "     L1.SHUSHOKU_NAITEI_TOTAL_GAKKOU, ";
        $query .= "     L1.SHUSHOKU_NAITEI_KENNAI_GAKKOU, ";
        $query .= "     L1.SHUSHOKU_NAITEI_KENGAI_GAKKOU, ";
        $query .= "     L1.SHUSHOKU_NAITEI_TOTAL_JIBUN, ";
        $query .= "     L1.SHUSHOKU_NAITEI_KENNAI_JIBUN, ";
        $query .= "     L1.SHUSHOKU_NAITEI_KENGAI_JIBUN, ";
        $query .= "     L1.SHUSHOKU_NAITEI_TOTAL_KOUMUIN, ";
        $query .= "     L1.SHUSHOKU_NAITEI_KENNAI_KOUMUIN, ";
        $query .= "     L1.SHUSHOKU_NAITEI_KENGAI_KOUMUIN, ";
        $query .= "     L1.SHUSHOKU_NAITEI_TOTAL_GOUKEI, ";
        $query .= "     L1.SHUSHOKU_NAITEI_KENNAI_GOUKEI, ";
        $query .= "     L1.SHUSHOKU_NAITEI_KENGAI_GOUKEI, ";
        $query .= "     L1.SHINGAKU_SHUSHOKU, ";
        $query .= "     L1.SHUSHOKU_KIBOU_TOTAL_MINAITEI, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENNAI_MINAITEI, ";
        $query .= "     L1.SHUSHOKU_KIBOU_KENGAI_MINAITEI, ";
        $query .= "     L1.SHINGAKU_IGAI_MIKKETEI ";
        if ($flg) {
            $query .= "     ,'".$model->lastColumn."' ";
        }
        $query .= " FROM ";
        $query .= "     T_MAIN T1 ";
        if ($model->cmd != "recalc") {
            $query .= "     LEFT JOIN T_ADDITION1 L1 ON L1.COURSECD = T1.COURSECD ";
            $query .= "                             AND L1.MAJORCD = T1.MAJORCD ";
            $query .= "                             AND L1.SEX = T1.SEX ";
            $query .= " ORDER BY ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX ";
        } else {
            $query .= "     LEFT JOIN T_ADDITION3 L1 ON L1.COURSECD = T1.COURSECD ";
            $query .= "                             AND L1.MAJORCD = T1.MAJORCD ";
            $query .= "                             AND L1.SEX = T1.SEX ";
            $query .= "     WHERE ";
            $query .= "         T1.SEX <> '9' ";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX, ";
            $query .= "     T1.MAJORNAME, ";
            $query .= "     T1.SEX_NAME, ";
            $query .= "     T1.ZENTAI_GOUKEI, ";
            $query .= "     T1.SHINGAKU_GOUKEI, ";
            $query .= "     T1.SHUSHOKU_KIBOU_TOTAL_GAKKOU, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENNAI_GAKKOU, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENGAI_GAKKOU, ";
            $query .= "     T1.SHUSHOKU_KIBOU_TOTAL_JIBUN, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENNAI_JIBUN, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENGAI_JIBUN, ";
            $query .= "     T1.SHUSHOKU_KIBOU_TOTAL_KOUMUIN, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENNAI_KOUMUIN, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENGAI_KOUMUIN, ";
            $query .= "     T1.SHUSHOKU_KIBOU_TOTAL_GOUKEI, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENNAI_GOUKEI, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENGAI_GOUKEI, ";
            $query .= "     T1.SONOTA_KEIKAKU_ARI, ";
            $query .= "     T1.SONOTA_KEIKAKU_NASHI, ";
            $query .= "     T1.SHUSHOKU_NAITEI_TOTAL_GAKKOU, ";
            $query .= "     T1.SHUSHOKU_NAITEI_KENNAI_GAKKOU, ";
            $query .= "     T1.SHUSHOKU_NAITEI_KENGAI_GAKKOU, ";
            $query .= "     T1.SHUSHOKU_NAITEI_TOTAL_JIBUN, ";
            $query .= "     T1.SHUSHOKU_NAITEI_KENNAI_JIBUN, ";
            $query .= "     T1.SHUSHOKU_NAITEI_KENGAI_JIBUN, ";
            $query .= "     T1.SHUSHOKU_NAITEI_TOTAL_KOUMUIN, ";
            $query .= "     T1.SHUSHOKU_NAITEI_KENNAI_KOUMUIN, ";
            $query .= "     T1.SHUSHOKU_NAITEI_KENGAI_KOUMUIN, ";
            $query .= "     T1.SHUSHOKU_NAITEI_TOTAL_GOUKEI, ";
            $query .= "     T1.SHUSHOKU_NAITEI_KENNAI_GOUKEI, ";
            $query .= "     T1.SHUSHOKU_NAITEI_KENGAI_GOUKEI, ";
            $query .= "     T1.SHINGAKU_SHUSHOKU, ";
            $query .= "     T1.SHUSHOKU_KIBOU_TOTAL_MINAITEI, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENNAI_MINAITEI, ";
            $query .= "     T1.SHUSHOKU_KIBOU_KENGAI_MINAITEI, ";
            $query .= "     T1.SHINGAKU_IGAI_MIKKETEI ";
            $query .= " FROM ";
            $query .= "     GOUKEI T1 ";
            $query .= " WHERE ";
            $query .= "     T1.SEX = '9' ";
            $query .= " ORDER BY ";
            $query .= "     COURSECD, ";
            $query .= "     MAJORCD, ";
            $query .= "     SEX ";
        }

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = "DELETE FROM AFT_DISEASE_ADDITION420_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR      = '".$model->field["YEAR"]."'";
        $db->query($query);

        //INSERT
        //卒業予定者数
        $zentai_goukei_sum = "";
        //進学希望者数
        $shingaku_goukei_sum = "";
        //就職希望者
        $shushoku_kibou_kennai_gakkou_sum = "";
        $shushoku_kibou_kengai_gakkou_sum = "";
        $kibou_gakkou_man_sum = "";
        $kibou_gakkou_woman_sum = "";
        $kibou_gakkou_total_sum = "";
        $shushoku_kibou_kennai_jibun_sum = "";
        $shushoku_kibou_kengai_jibun_sum = "";
        $kibou_jibun_man_sum = "";
        $kibou_jibun_woman_sum = "";
        $kibou_jibun_total_sum = "";
        $shushoku_kibou_kennai_koumuin_sum = "";
        $shushoku_kibou_kengai_koumuin_sum = "";
        $kibou_koumuin_man_sum = "";
        $kibou_koumuin_woman_sum = "";
        $kibou_koumuin_total_sum = "";

        $shushoku_kibou_kennai_total_sum = "";
        $shushoku_kibou_kennai_total_man_sum = "";
        $shushoku_kibou_kennai_total_woman_sum = "";
        $shushoku_kibou_kengai_total_sum = "";
        $shushoku_kibou_kengai_total_man_sum = "";
        $shushoku_kibou_kengai_total_woman_sum = "";
        $shushoku_kibou_total_sum = "";
        $shushoku_kibou_total_man_sum = "";
        $shushoku_kibou_total_woman_sum = "";
        //その他
        $sonota_keikaku_ari_sum = "";
        $sonota_keikaku_nashi_sum = "";
        //就職内定者
        $shushoku_naitei_kennai_gakkou_sum = "";
        $shushoku_naitei_kengai_gakkou_sum = "";
        $naitei_gakkou_man_sum = "";
        $naitei_gakkou_woman_sum = "";
        $naitei_gakkou_total_sum = "";
        $shushoku_naitei_kennai_jibun_sum = "";
        $shushoku_naitei_kengai_jibun_sum = "";
        $naitei_jibun_man_sum = "";
        $naitei_jibun_woman_sum = "";
        $naitei_jibun_total_sum = "";
        $shushoku_naitei_kennai_koumuin_sum = "";
        $shushoku_naitei_kengai_koumuin_sum = "";
        $naitei_koumuin_man_sum = "";
        $naitei_koumuin_woman_sum = "";
        $naitei_koumuin_total_sum = "";

        $shushoku_naitei_kennai_total_sum = "";
        $shushoku_naitei_kennai_total_man_sum = "";
        $shushoku_naitei_kennai_total_woman_sum = "";
        $shushoku_naitei_kengai_total_sum = "";
        $shushoku_naitei_kengai_total_man_sum = "";
        $shushoku_naitei_kengai_total_woman_sum = "";
        $shushoku_naitei_total_sum = "";
        $shushoku_naitei_total_man_sum = "";
        $shushoku_naitei_total_woman_sum = "";
        //進学就職者
        $shingaku_shushoku_sum = "";
        //就職希望者の内未内定者
        $shushoku_kibou_kennai_minaitei_sum = "";
        $shushoku_kibou_kengai_minaitei_sum = "";
        $kibou_minaitei_man_sum = "";
        $kibou_minaitei_woman_sum = "";
        $kibou_minaitei_total_sum = "";
        //進学以外の進路未決定者
        $shingaku_igai_mikketei_sum = "";
        
        //学科
        $gakka = array();
        for ($i = 0; $i < get_count($model->fields["CODE"]); $i++) {
            //学科・性別
            $code_arr = explode("-", $model->fields["CODE"][$i]);
            $gakka[$i] = $code_arr[0].$code_arr[1];
            //学科が変われば初期化する
            if ($i != 0 && $gakka[$i] != $gakka[$i-1]) {
                //卒業予定者数
                $zentai_goukei_sum = "";
                //進学希望者数
                $shingaku_goukei_sum = "";
                //就職希望者
                $shushoku_kibou_kennai_gakkou_sum = "";
                $shushoku_kibou_kengai_gakkou_sum = "";
                $kibou_gakkou_man_sum = "";
                $kibou_gakkou_woman_sum = "";
                $kibou_gakkou_total_sum = "";
                $shushoku_kibou_kennai_jibun_sum = "";
                $shushoku_kibou_kengai_jibun_sum = "";
                $kibou_jibun_man_sum = "";
                $kibou_jibun_woman_sum = "";
                $kibou_jibun_total_sum = "";
                $shushoku_kibou_kennai_koumuin_sum = "";
                $shushoku_kibou_kengai_koumuin_sum = "";
                $kibou_koumuin_man_sum = "";
                $kibou_koumuin_woman_sum = "";
                $kibou_koumuin_total_sum = "";

                $shushoku_kibou_kennai_total_sum = "";
                $shushoku_kibou_kennai_total_man_sum = "";
                $shushoku_kibou_kennai_total_woman_sum = "";
                $shushoku_kibou_kengai_total_sum = "";
                $shushoku_kibou_kengai_total_man_sum = "";
                $shushoku_kibou_kengai_total_woman_sum = "";
                $shushoku_kibou_total_sum = "";
                $shushoku_kibou_total_man_sum = "";
                $shushoku_kibou_total_woman_sum = "";
                //その他
                $sonota_keikaku_ari_sum = "";
                $sonota_keikaku_nashi_sum = "";
                //就職内定者
                $shushoku_naitei_kennai_gakkou_sum = "";
                $shushoku_naitei_kengai_gakkou_sum = "";
                $naitei_gakkou_man_sum = "";
                $naitei_gakkou_woman_sum = "";
                $naitei_gakkou_total_sum = "";
                $shushoku_naitei_kennai_jibun_sum = "";
                $shushoku_naitei_kengai_jibun_sum = "";
                $naitei_jibun_man_sum = "";
                $naitei_jibun_woman_sum = "";
                $naitei_jibun_total_sum = "";
                $shushoku_naitei_kennai_koumuin_sum = "";
                $shushoku_naitei_kengai_koumuin_sum = "";
                $naitei_koumuin_man_sum = "";
                $naitei_koumuin_woman_sum = "";
                $naitei_koumuin_total_sum = "";

                $shushoku_naitei_kennai_total_sum = "";
                $shushoku_naitei_kennai_total_man_sum = "";
                $shushoku_naitei_kennai_total_woman_sum = "";
                $shushoku_naitei_kengai_total_sum = "";
                $shushoku_naitei_kengai_total_man_sum = "";
                $shushoku_naitei_kengai_total_woman_sum = "";
                $shushoku_naitei_total_sum = "";
                $shushoku_naitei_total_man_sum = "";
                $shushoku_naitei_total_woman_sum = "";
                //進学就職者
                $shingaku_shushoku_sum = "";
                //就職希望者の内未内定者
                $shushoku_kibou_kennai_minaitei_sum = "";
                $shushoku_kibou_kengai_minaitei_sum = "";
                $kibou_minaitei_man_sum = "";
                $kibou_minaitei_woman_sum = "";
                $kibou_minaitei_total_sum = "";
                //進学以外の進路未決定者
                $shingaku_igai_mikketei_sum = "";
            }
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = $model->field["YEAR"];
            $data["COURSECD"][TEXT]             = $code_arr[0];
            $data["MAJORCD"][TEXT]              = $code_arr[1];
            $data["SEX"][TEXT]                  = $code_arr[2];
            //共通
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            
            //人数更新
            /*卒業予定者数*/
            $data["LARGE_DIV"][TEXT]            = '99';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["ZENTAI_GOUKEI"][$i];
                $zentai_goukei_sum += $model->fields["ZENTAI_GOUKEI"][$i];
            } else {
                $data["COUNT"][NUMBER]              = $zentai_goukei_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
            
            /*進学希望者数*/
            $data["LARGE_DIV"][TEXT]            = '10';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHINGAKU_GOUKEI"][$i];
                $shingaku_goukei_sum += $model->fields["SHINGAKU_GOUKEI"][$i];
            } else {
                $data["COUNT"][NUMBER]              = $shingaku_goukei_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            /*就職希望者*/
            //学校紹介県内
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '01';
            $data["SMALL_DIV"][TEXT]            = '001';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KIBOU_KENNAI_GAKKOU"][$i];
                $shushoku_kibou_kennai_gakkou_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_GAKKOU"][$i];
                if ($code_arr[2] === '1') {
                    $kibou_gakkou_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_GAKKOU"][$i];
                    $shushoku_kibou_kennai_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_GAKKOU"][$i];
                    $shushoku_kibou_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_GAKKOU"][$i];
                } elseif ($code_arr[2] === '2') {
                    $kibou_gakkou_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_GAKKOU"][$i];
                    $shushoku_kibou_kennai_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_GAKKOU"][$i];
                    $shushoku_kibou_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_GAKKOU"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kennai_gakkou_sum;
                $kibou_gakkou_total_sum += $shushoku_kibou_kennai_gakkou_sum;
                $shushoku_kibou_kennai_total_sum += $shushoku_kibou_kennai_gakkou_sum;
                $shushoku_kibou_total_sum += $shushoku_kibou_kennai_gakkou_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
                        
            //学校紹介県外
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '01';
            $data["SMALL_DIV"][TEXT]            = '002';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KIBOU_KENGAI_GAKKOU"][$i];
                $shushoku_kibou_kengai_gakkou_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_GAKKOU"][$i];
                if ($code_arr[2] === '1') {
                    $kibou_gakkou_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_GAKKOU"][$i];
                    $shushoku_kibou_kengai_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_GAKKOU"][$i];
                    $shushoku_kibou_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_GAKKOU"][$i];
                } elseif ($code_arr[2] === '2') {
                    $kibou_gakkou_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_GAKKOU"][$i];
                    $shushoku_kibou_kengai_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_GAKKOU"][$i];
                    $shushoku_kibou_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_GAKKOU"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kengai_gakkou_sum;
                $kibou_gakkou_total_sum += $shushoku_kibou_kengai_gakkou_sum;
                $shushoku_kibou_kengai_total_sum += $shushoku_kibou_kengai_gakkou_sum;
                $shushoku_kibou_total_sum += $shushoku_kibou_kengai_gakkou_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //学校紹介合計
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '01';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $kibou_gakkou_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $kibou_gakkou_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $kibou_gakkou_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
            
            //自己・縁故県内
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '02';
            $data["SMALL_DIV"][TEXT]            = '001';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KIBOU_KENNAI_JIBUN"][$i];
                $shushoku_kibou_kennai_jibun_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_JIBUN"][$i];
                if ($code_arr[2] === '1') {
                    $kibou_jibun_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_JIBUN"][$i];
                    $shushoku_kibou_kennai_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_JIBUN"][$i];
                    $shushoku_kibou_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_JIBUN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $kibou_jibun_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_JIBUN"][$i];
                    $shushoku_kibou_kennai_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_JIBUN"][$i];
                    $shushoku_kibou_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_JIBUN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kennai_jibun_sum;
                $kibou_jibun_total_sum += $shushoku_kibou_kennai_jibun_sum;
                $shushoku_kibou_kennai_total_sum += $shushoku_kibou_kennai_jibun_sum;
                $shushoku_kibou_total_sum += $shushoku_kibou_kennai_jibun_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
                        
            //自己・縁故県外
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '02';
            $data["SMALL_DIV"][TEXT]            = '002';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KIBOU_KENGAI_JIBUN"][$i];
                $shushoku_kibou_kengai_jibun_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_JIBUN"][$i];
                if ($code_arr[2] === '1') {
                    $kibou_jibun_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_JIBUN"][$i];
                    $shushoku_kibou_kengai_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_JIBUN"][$i];
                    $shushoku_kibou_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_JIBUN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $kibou_jibun_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_JIBUN"][$i];
                    $shushoku_kibou_kengai_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_JIBUN"][$i];
                    $shushoku_kibou_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_JIBUN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kengai_jibun_sum;
                $kibou_jibun_total_sum += $shushoku_kibou_kengai_jibun_sum;
                $shushoku_kibou_kengai_total_sum += $shushoku_kibou_kengai_jibun_sum;
                $shushoku_kibou_total_sum += $shushoku_kibou_kengai_jibun_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //自己・縁故合計
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '02';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $kibou_jibun_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $kibou_jibun_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $kibou_jibun_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
            
            //公務員
            //公務員県内
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '03';
            $data["SMALL_DIV"][TEXT]            = '001';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KIBOU_KENNAI_KOUMUIN"][$i];
                $shushoku_kibou_kennai_koumuin_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_KOUMUIN"][$i];
                if ($code_arr[2] === '1') {
                    $kibou_koumuin_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_KOUMUIN"][$i];
                    $shushoku_kibou_kennai_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_KOUMUIN"][$i];
                    $shushoku_kibou_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_KOUMUIN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $kibou_koumuin_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_KOUMUIN"][$i];
                    $shushoku_kibou_kennai_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_KOUMUIN"][$i];
                    $shushoku_kibou_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_KOUMUIN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kennai_koumuin_sum;
                $kibou_koumuin_total_sum += $shushoku_kibou_kennai_koumuin_sum;
                $shushoku_kibou_kennai_total_sum += $shushoku_kibou_kennai_koumuin_sum;
                $shushoku_kibou_total_sum += $shushoku_kibou_kennai_koumuin_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
                        
            //公務員県外
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '03';
            $data["SMALL_DIV"][TEXT]            = '002';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KIBOU_KENGAI_KOUMUIN"][$i];
                $shushoku_kibou_kengai_koumuin_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_KOUMUIN"][$i];
                if ($code_arr[2] === '1') {
                    $kibou_koumuin_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_KOUMUIN"][$i];
                    $shushoku_kibou_kengai_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_KOUMUIN"][$i];
                    $shushoku_kibou_total_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_KOUMUIN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $kibou_koumuin_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_KOUMUIN"][$i];
                    $shushoku_kibou_kengai_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_KOUMUIN"][$i];
                    $shushoku_koumuin_total_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_KOUMUIN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kengai_koumuin_sum;
                $kibou_koumuin_total_sum += $shushoku_kibou_kengai_koumuin_sum;
                $shushoku_kibou_kengai_total_sum += $shushoku_kibou_kengai_koumuin_sum;
                $shushoku_kibou_total_sum += $shushoku_kibou_kengai_koumuin_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //公務員希望合計
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '03';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $kibou_koumuin_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $kibou_koumuin_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $kibou_koumuin_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //県内就職希望者合計
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '001';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $shushoku_kibou_kennai_total_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $shushoku_kibou_kennai_total_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kennai_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //県外就職希望者合計
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '002';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $shushoku_kibou_kengai_total_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $shushoku_kibou_kengai_total_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kengai_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //就職希望者合計
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $shushoku_kibou_kennai_total_man_sum + $shushoku_kibou_kengai_total_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $shushoku_kibou_kennai_total_woman_sum + $shushoku_kibou_kengai_total_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kennai_total_sum + $shushoku_kibou_kengai_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //その他卒業後の計画有
            $data["LARGE_DIV"][TEXT]            = '21';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '003';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SONOTA_KEIKAKU_ARI"][$i];
                $sonota_keikaku_ari_sum += $model->fields["SONOTA_KEIKAKU_ARI"][$i];
            } else {
                $data["COUNT"][NUMBER]              = $sonota_keikaku_ari_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //その他卒業後の計画無
            $data["LARGE_DIV"][TEXT]            = '21';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '004';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SONOTA_KEIKAKU_NASHI"][$i];
                $sonota_keikaku_nashi_sum += $model->fields["SONOTA_KEIKAKU_NASHI"][$i];
            } else {
                $data["COUNT"][NUMBER]              = $sonota_keikaku_nashi_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            /*就職内定者*/
            //学校紹介
            //学校紹介県内
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '01';
            $data["SMALL_DIV"][TEXT]            = '001';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_NAITEI_KENNAI_GAKKOU"][$i];
                $shushoku_naitei_kennai_gakkou_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_GAKKOU"][$i];
                if ($code_arr[2] === '1') {
                    $naitei_gakkou_man_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_GAKKOU"][$i];
                    $shushoku_naitei_kennai_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_GAKKOU"][$i];
                    $shushoku_naitei_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_GAKKOU"][$i];
                } elseif ($code_arr[2] === '2') {
                    $naitei_gakkou_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_GAKKOU"][$i];
                    $shushoku_naitei_kennai_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_GAKKOU"][$i];
                    $shushoku_naitei_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_GAKKOU"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_naitei_kennai_gakkou_sum;
                $naitei_gakkou_total_sum += $shushoku_naitei_kennai_gakkou_sum;
                $shushoku_naitei_kennai_total_sum += $shushoku_naitei_kennai_gakkou_sum;
                $shushoku_naitei_total_sum += $shushoku_naitei_kennai_gakkou_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
                        
            //学校紹介県外
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '01';
            $data["SMALL_DIV"][TEXT]            = '002';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_NAITEI_KENGAI_GAKKOU"][$i];
                $shushoku_naitei_kengai_gakkou_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_GAKKOU"][$i];
                if ($code_arr[2] === '1') {
                    $naitei_gakkou_man_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_GAKKOU"][$i];
                    $shushoku_naitei_kengai_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_GAKKOU"][$i];
                    $shushoku_naitei_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_GAKKOU"][$i];
                } elseif ($code_arr[2] === '2') {
                    $naitei_gakkou_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_GAKKOU"][$i];
                    $shushoku_naitei_kengai_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_GAKKOU"][$i];
                    $shushoku_naitei_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_GAKKOU"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_naitei_kengai_gakkou_sum;
                $naitei_gakkou_total_sum += $shushoku_naitei_kengai_gakkou_sum;
                $shushoku_naitei_kengai_total_sum += $shushoku_naitei_kengai_gakkou_sum;
                $shushoku_naitei_total_sum += $shushoku_naitei_kengai_gakkou_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //学校紹介合計
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '01';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $naitei_gakkou_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $naitei_gakkou_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $naitei_gakkou_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
            
            //自己・縁故
            //自己・縁故県内
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '02';
            $data["SMALL_DIV"][TEXT]            = '001';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_NAITEI_KENNAI_JIBUN"][$i];
                $shushoku_naitei_kennai_jibun_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_JIBUN"][$i];
                if ($code_arr[2] === '1') {
                    $naitei_jibun_man_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_JIBUN"][$i];
                    $shushoku_naitei_kennai_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_JIBUN"][$i];
                    $shushoku_naitei_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_JIBUN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $naitei_jibun_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_JIBUN"][$i];
                    $shushoku_naitei_kennai_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_JIBUN"][$i];
                    $shushoku_naitei_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_JIBUN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_naitei_kennai_jibun_sum;
                $naitei_jibun_total_sum += $shushoku_naitei_kennai_jibun_sum;
                $shushoku_naitei_kennai_total_sum += $shushoku_naitei_kennai_jibun_sum;
                $shushoku_naitei_total_sum += $shushoku_naitei_kennai_jibun_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
                        
            //自己・縁故県外
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '02';
            $data["SMALL_DIV"][TEXT]            = '002';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_NAITEI_KENGAI_JIBUN"][$i];
                $shushoku_naitei_kengai_jibun_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_JIBUN"][$i];
                if ($code_arr[2] === '1') {
                    $naitei_jibun_man_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_JIBUN"][$i];
                    $shushoku_naitei_kengai_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_JIBUN"][$i];
                    $shushoku_naitei_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_JIBUN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $naitei_jibun_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_JIBUN"][$i];
                    $shushoku_naitei_kengai_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_JIBUN"][$i];
                    $shushoku_naitei_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_JIBUN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_naitei_kengai_jibun_sum;
                $naitei_jibun_total_sum += $shushoku_naitei_kengai_jibun_sum;
                $shushoku_naitei_kengai_total_sum += $shushoku_naitei_kengai_jibun_sum;
                $shushoku_naitei_total_sum += $shushoku_naitei_kengai_jibun_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //自己・縁故合計
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '02';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $naitei_jibun_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $naitei_jibun_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $naitei_jibun_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
            
            //公務員
            //公務員県内
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '03';
            $data["SMALL_DIV"][TEXT]            = '001';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_NAITEI_KENNAI_KOUMUIN"][$i];
                $shushoku_naitei_kennai_koumuin_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_KOUMUIN"][$i];
                if ($code_arr[2] === '1') {
                    $naitei_koumuin_man_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_KOUMUIN"][$i];
                    $shushoku_naitei_kennai_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_KOUMUIN"][$i];
                    $shushoku_naitei_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_KOUMUIN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $naitei_koumuin_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_KOUMUIN"][$i];
                    $shushoku_naitei_kennai_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_KOUMUIN"][$i];
                    $shushoku_naitei_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENNAI_KOUMUIN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_naitei_kennai_koumuin_sum;
                $naitei_koumuin_total_sum += $shushoku_naitei_kennai_koumuin_sum;
                $shushoku_naitei_kennai_total_sum += $shushoku_naitei_kennai_koumuin_sum;
                $shushoku_naitei_total_sum += $shushoku_naitei_kennai_koumuin_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
                        
            //公務員県外
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '03';
            $data["SMALL_DIV"][TEXT]            = '002';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_NAITEI_KENGAI_KOUMUIN"][$i];
                $shushoku_naitei_kengai_koumuin_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_KOUMUIN"][$i];
                if ($code_arr[2] === '1') {
                    $naitei_koumuin_man_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_KOUMUIN"][$i];
                    $shushoku_naitei_kengai_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_KOUMUIN"][$i];
                    $shushoku_naitei_total_man_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_KOUMUIN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $naitei_koumuin_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_KOUMUIN"][$i];
                    $shushoku_naitei_kengai_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_KOUMUIN"][$i];
                    $shushoku_koumuin_total_woman_sum += $model->fields["SHUSHOKU_NAITEI_KENGAI_KOUMUIN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_naitei_kengai_koumuin_sum;
                $naitei_koumuin_total_sum += $shushoku_naitei_kengai_koumuin_sum;
                $shushoku_naitei_kengai_total_sum += $shushoku_naitei_kengai_koumuin_sum;
                $shushoku_naitei_total_sum += $shushoku_naitei_kengai_koumuin_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //公務員内定合計
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '03';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $naitei_koumuin_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $naitei_koumuin_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $naitei_koumuin_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //県内就職内定者合計
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '001';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $shushoku_naitei_kennai_total_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $shushoku_naitei_kennai_total_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_naitei_kennai_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //県外就職内定者合計
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '002';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $shushoku_naitei_kengai_total_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $shushoku_naitei_kengai_total_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_naitei_kengai_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //就職内定者合計
            $data["LARGE_DIV"][TEXT]            = '22';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $shushoku_naitei_kennai_total_man_sum + $shushoku_naitei_kengai_total_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $shushoku_naitei_kennai_total_woman_sum + $shushoku_naitei_kengai_total_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_naitei_kennai_total_sum + $shushoku_naitei_kengai_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //進学就職
            $data["LARGE_DIV"][TEXT]            = '23';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHINGAKU_SHUSHOKU"][$i];
                $shingaku_shushoku_sum += $model->fields["SHINGAKU_SHUSHOKU"][$i];
            } else {
                $data["COUNT"][NUMBER]              = $shingaku_shushoku_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
            
            //就職希望者の内未内定者数
            //就職希望者の内未内定者　県内
            $data["LARGE_DIV"][TEXT]            = '24';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '001';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KIBOU_KENNAI_MINAITEI"][$i];
                $shushoku_kibou_kennai_minaitei_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_MINAITEI"][$i];
                if ($code_arr[2] === '1') {
                    $kibou_minaitei_man_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_MINAITEI"][$i];
                } elseif ($code_arr[2] === '2') {
                    $kibou_minaitei_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENNAI_MINAITEI"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kennai_minaitei_sum;
                $kibou_minaitei_total_sum += $shushoku_kibou_kennai_minaitei_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //就職希望者の内未内定者　県外
            $data["LARGE_DIV"][TEXT]            = '24';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '002';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KIBOU_KENGAI_MINAITEI"][$i];
                $shushoku_kibou_kengai_minaitei_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_MINAITEI"][$i];
                if ($code_arr[2] === '1') {
                    $kibou_minaitei_man_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_MINAITEI"][$i];
                } elseif ($code_arr[2] === '2') {
                    $kibou_minaitei_woman_sum += $model->fields["SHUSHOKU_KIBOU_KENGAI_MINAITEI"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kibou_kengai_minaitei_sum;
                $kibou_minaitei_total_sum += $shushoku_kibou_kengai_minaitei_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //就職希望者の内未内定者　合計
            $data["LARGE_DIV"][TEXT]            = '24';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER]          = $kibou_minaitei_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER]          = $kibou_minaitei_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]              = $kibou_minaitei_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);

            //進学希望以外の進路未決定者数
            $data["LARGE_DIV"][TEXT]            = '30';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHINGAKU_IGAI_MIKKETEI"][$i];
                $shingaku_igai_mikketei_sum += $model->fields["SHINGAKU_IGAI_MIKKETEI"][$i];
            } else {
                $data["COUNT"][NUMBER]              = $shingaku_igai_mikketei_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
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
        $query .= "     CHAR(T1.EXECUTE_DATE) || ':' || T1.COURSECD || ':' || T1.MAJORCD AS VALUE, ";
        $query .= "     RTRIM(CHAR(REPLACE(CHAR(T1.EXECUTE_DATE),'-','/'))) || '　' || L1.MAJORNAME  AS LABEL ";
        $query .= " FROM ";
        $query .= "     REPORT_AFT_DISEASE_ADDITION420_DAT T1";
        $query .= "     INNER JOIN MAJOR_MST L1 ";
        $query .= "         ON  L1.COURSECD     = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD    = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得
    public function getAddition1Dat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION420_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }
    
    public function getAddition2Dat($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION420_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";

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
        //県側・疾病等結果一覧テーブル
        $query  = "DELETE FROM AFT_DISEASE_ADDITION420_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db2->query($query);
        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION420_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db->query($query);
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（疾病等結果一覧）
        $query = knje420Query::getAddition1Dat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["LARGE_DIV"][TEXT]            = $row["LARGE_DIV"];
            $data["MIDDLE_DIV"][TEXT]           = $row["MIDDLE_DIV"];
            $data["SMALL_DIV"][TEXT]            = $row["SMALL_DIV"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            //人数
            $data["COUNT"][NUMBER]              = $row["COUNT"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db2->query($query);
        }
        $result->free();
        
        //報告用データ取得
        $query = knje420Query::getAddition2Dat($model);
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側、学校側・報告テーブル
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row2["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row2["YEAR"];
            $data["COURSECD"][TEXT]             = $row2["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row2["MAJORCD"];
            $data["EXECUTE_DATE"][TEXT]         = str_replace("/", "-", $model->field["EXECUTE_DATE"]);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
            $db2->query($query);
        }
        $result->free();
        
        //報告処理
        $data = array();
        $data["ANSWER_FLG"][TEXT]      = "1";
        $data["ANSWER_DATE"][FUNC]     = "SYSDATE()";
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->field["YEAR"]."' ";
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
        $query .= "     AFT_DISEASE_ADDITION420_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
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
        $getCount = $db->getOne(knje420Query::upCheckQuery($model, $setData, $largeDiv, $middleDiv, $smallDiv));
        
        $data = array(); //初期化
        $data["COUNT"][NUMBER]              = $setData[$setName];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        if ($getCount == 0) {
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = $model->field["YEAR"];
            $data["COURSECD"][TEXT]             = $setData["COURSECD"];
            $data["MAJORCD"][TEXT]              = $setData["MAJORCD"];
            $data["SEX"][TEXT]                  = $setData["SEX"];
            $data["LARGE_DIV"][TEXT]            = $largeDiv;
            $data["MIDDLE_DIV"][TEXT]           = $middleDiv;
            $data["SMALL_DIV"][TEXT]            = $smallDiv;
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
            $where .= "   AND YEAR              = '".$model->field["YEAR"]."'";
            $where .= "   AND COURSECD          = '".$setData["COURSECD"]."'";
            $where .= "   AND MAJORCD           = '".$setData["MAJORCD"]."'";
            $where .= "   AND SEX               = '".$setData["SEX"]."'";
            $where .= "   AND LARGE_DIV         = '".$largeDiv."'";
            $where .= "   AND MIDDLE_DIV        = '".$middleDiv."'";
            $where .= "   AND SMALL_DIV         = '".$smallDiv."'";
            $query  = Query::updateSQL($data, "AFT_DISEASE_ADDITION420_DAT", $where);
            $db->query($query);
        }
        
        return;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            /*卒業予定者数*/
            knje420Query::insertQuery($db, $model, $data_arr[$i], "99", "99", "999", "ZENTAI_GOUKEI");
        
            /*進学希望者数*/
            knje420Query::insertQuery($db, $model, $data_arr[$i], "10", "99", "999", "SHINGAKU_GOUKEI");

            /*就職希望者*/
            //学校紹介県内
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "01", "001", "SHUSHOKU_KIBOU_KENNAI_GAKKOU");

            //学校紹介県外
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "01", "002", "SHUSHOKU_KIBOU_KENGAI_GAKKOU");

            //学校紹介合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "01", "999", "SHUSHOKU_KIBOU_TOTAL_GAKKOU");

            //自己・縁故県内
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "02", "001", "SHUSHOKU_KIBOU_KENNAI_JIBUN");

            //自己・縁故県外
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "02", "002", "SHUSHOKU_KIBOU_KENGAI_JIBUN");
            
            //自己・縁故合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "02", "999", "SHUSHOKU_KIBOU_TOTAL_JIBUN");
            
            //公務員
            //公務員県内
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "03", "001", "SHUSHOKU_KIBOU_KENNAI_KOUMUIN");

            //公務員県外
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "03", "002", "SHUSHOKU_KIBOU_KENGAI_KOUMUIN");
            
            //公務員希望合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "03", "999", "SHUSHOKU_KIBOU_TOTAL_KOUMUIN");

            //県内就職希望者合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "99", "001", "SHUSHOKU_KIBOU_KENNAI_GOUKEI");

            //県外就職希望者合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "99", "002", "SHUSHOKU_KIBOU_KENGAI_GOUKEI");

            //就職希望者合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "20", "99", "999", "SHUSHOKU_KIBOU_TOTAL_GOUKEI");

            //その他卒業後の計画有
            knje420Query::insertQuery($db, $model, $data_arr[$i], "21", "99", "003", "SONOTA_KEIKAKU_ARI");

            //その他卒業後の計画無
            knje420Query::insertQuery($db, $model, $data_arr[$i], "21", "99", "004", "SONOTA_KEIKAKU_NASHI");

            /*就職内定者*/
            //学校紹介
            //学校紹介県内
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "01", "001", "SHUSHOKU_NAITEI_KENNAI_GAKKOU");

            //学校紹介県外
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "01", "002", "SHUSHOKU_NAITEI_KENGAI_GAKKOU");

            //学校紹介合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "01", "999", "SHUSHOKU_NAITEI_TOTAL_GAKKOU");

            //自己・縁故
            //自己・縁故県内
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "02", "001", "SHUSHOKU_NAITEI_KENNAI_JIBUN");

            //自己・縁故県外
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "02", "002", "SHUSHOKU_NAITEI_KENGAI_JIBUN");

            //自己・縁故合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "02", "999", "SHUSHOKU_NAITEI_TOTAL_JIBUN");

            //公務員
            //公務員県内
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "03", "001", "SHUSHOKU_NAITEI_KENNAI_KOUMUIN");

            //公務員県外
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "03", "002", "SHUSHOKU_NAITEI_KENGAI_KOUMUIN");

            //公務員内定合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "03", "999", "SHUSHOKU_NAITEI_TOTAL_KOUMUIN");

            //県内就職内定者合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "99", "001", "SHUSHOKU_NAITEI_KENNAI_GOUKEI");

            //県外就職内定者合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "99", "002", "SHUSHOKU_NAITEI_KENGAI_GOUKEI");

            //就職内定者合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "22", "99", "999", "SHUSHOKU_NAITEI_TOTAL_GOUKEI");

            //進学就職
            knje420Query::insertQuery($db, $model, $data_arr[$i], "23", "99", "999", "SHINGAKU_SHUSHOKU");

            //就職希望者の内未内定者数
            //就職希望者の内未内定者　県内
            knje420Query::insertQuery($db, $model, $data_arr[$i], "24", "99", "001", "SHUSHOKU_KIBOU_KENNAI_MINAITEI");

            //就職希望者の内未内定者　県外
            knje420Query::insertQuery($db, $model, $data_arr[$i], "24", "99", "002", "SHUSHOKU_KIBOU_KENGAI_MINAITEI");

            //就職希望者の内の未内定者　合計
            knje420Query::insertQuery($db, $model, $data_arr[$i], "24", "99", "999", "SHUSHOKU_KIBOU_TOTAL_MINAITEI");

            //進学希望以外の進路未決定者数
            knje420Query::insertQuery($db, $model, $data_arr[$i], "30", "99", "999", "SHINGAKU_IGAI_MIKKETEI");

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
