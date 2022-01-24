<?php

require_once('for_php7.php');
class knje410Query extends Query
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
        $query .= "     AND L1.REQUEST_ANSWER_PRG = 'KNJE410' ";
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
            //進学
            $query .= "         SUM(CASE WHEN L1.SCHOOL_SORT = '01' THEN 1 ELSE 0 END) AS SHINGAKU_DAIGAKU, ";
            $query .= "         SUM(CASE WHEN L1.SCHOOL_SORT = '02' THEN 1 ELSE 0 END) AS SHINGAKU_TANDAI, ";
            $query .= "         SUM(CASE WHEN L1.SCHOOL_SORT = '03' THEN 1 ELSE 0 END) AS SHINGAKU_SENMON, ";
            $query .= "         SUM(CASE WHEN L1.SCHOOL_SORT <> '01' AND L1.SCHOOL_SORT <> '02' AND L1.SCHOOL_SORT <> '03' THEN 1 ELSE 0 END) AS SHINGAKU_SONOTA, ";
            $query .= "         0 AS SHINGAKU_REMARK, ";
            //就職
            $query .= "         SUM(CASE WHEN L2.PREF_CD = '".$model->prefcd."' THEN 1 ELSE 0 END) AS SHUSHOKU_KENNAI, ";
            $query .= "         SUM(CASE WHEN L2.SCHOOL_SORT = '06' AND L1.PREF_CD = '".$model->prefcd."' THEN 1 ELSE 0 END) AS SHUSHOKU_KENNAI_KOUMUIN, ";
            //学校マスタの学校コードが国内にある
            if ($model->prefcd > 0 && $model->prefcd <= 47) {
                $query .= "         SUM(CASE WHEN L2.PREF_CD <> '".$model->prefcd."' AND INT(L2.PREF_CD) > 0 AND INT(L2.PREF_CD) <= 47 THEN 1 ELSE 0 END) AS SHUSHOKU_KENGAI, ";
                $query .= "         SUM(CASE WHEN L2.SCHOOL_SORT = '06' AND L2.PREF_CD <> '".$model->prefcd."' AND INT(L2.PREF_CD) > 0 AND INT(L2.PREF_CD) <= 47 THEN 1 ELSE 0 END) AS SHUSHOKU_KENGAI_KOUMUIN, ";
                $query .= "         SUM(CASE WHEN (L1.PREF_CD IS NULL AND L2.PREF_CD IS NULL) OR INT(L2.PREF_CD) <= 0 OR INT(L2.PREF_CD) > 47 THEN 1 ELSE 0 END) AS SHUSHOKU_SONOTA, ";
            //学校マスタの学校コードが学校が国外（その他にカウント）※基本的にありえない
            } else {
                $query .= "         SUM(0) AS SHUSHOKU_KENGAI, ";
                $query .= "         SUM(0) AS SHUSHOKU_KENGAI_KOUMUIN, ";
                $query .= "         SUM(1) AS SHUSHOKU_SONOTA, ";
            }
            $query .= "         0 AS SHUSHOKU_REMARK, ";
            $query .= "         COUNT(L3.SCHREGNO) AS SONOTA_GOUKEI ";
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
            //就職希望者
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT L2 ON L2.YEAR = T1.YEAR ";
            $query .= "                                     AND L2.SEQ = T1.SEQ ";
            $query .= "                                     AND L2.SENKOU_KIND = '1' ";
            //その他
            $query .= "     LEFT JOIN AFT_GRAD_COURSE_DAT L3 ON L3.YEAR = T1.YEAR ";
            $query .= "                                     AND L3.SEQ = T1.SEQ ";
            $query .= "                                     AND L2.SENKOU_KIND NOT IN ('0', '1') ";
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
            $query .= "         T1.SHINGAKU_DAIGAKU + T1.SHINGAKU_TANDAI + T1.SHINGAKU_SENMON + T1.SHINGAKU_SONOTA + T1.SHINGAKU_REMARK AS SHINGAKU_GOUKEI, ";
            $query .= "         T1.SHUSHOKU_KENNAI + T1.SHUSHOKU_KENNAI_KOUMUIN + T1.SHUSHOKU_KENGAI + T1.SHUSHOKU_KENGAI_KOUMUIN + T1.SHUSHOKU_SONOTA + T1.SHUSHOKU_REMARK AS SHUSHOKU_GOUKEI ";
            $query .= "     FROM ";
            $query .= "         T_ADDITION1 T1";
            $query .= "     ) ";
            $query .= " , T_ADDITION3 AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.*, ";
            $query .= "         T1.SHINGAKU_GOUKEI + T1.SHUSHOKU_GOUKEI + T1.SONOTA_GOUKEI AS ZENTAI_GOUKEI ";
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
            $query .= "     VALUE(L1.SHINGAKU_DAIGAKU, 0) AS SHINGAKU_DAIGAKU, ";
            $query .= "     VALUE(L1.SHINGAKU_TANDAI, 0) AS SHINGAKU_TANDAI, ";
            $query .= "     VALUE(L1.SHINGAKU_SENMON, 0) AS SHINGAKU_SENMON, ";
            $query .= "     VALUE(L1.SHINGAKU_SONOTA, 0) AS SHINGAKU_SONOTA, ";
            $query .= "     VALUE(L1.SHINGAKU_REMARK, 0) AS SHINGAKU_REMARK, ";
            $query .= "     VALUE(L1.SHINGAKU_GOUKEI, 0) AS SHINGAKU_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KENNAI, 0) AS SHUSHOKU_KENNAI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KENNAI_KOUMUIN, 0) AS SHUSHOKU_KENNAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KENGAI, 0) AS SHUSHOKU_KENGAI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KENGAI_KOUMUIN, 0) AS SHUSHOKU_KENGAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_SONOTA, 0) AS SHUSHOKU_SONOTA, ";
            $query .= "     VALUE(L1.SHUSHOKU_REMARK, 0) AS SHUSHOKU_REMARK, ";
            $query .= "     VALUE(L1.SHUSHOKU_GOUKEI, 0) AS SHUSHOKU_GOUKEI, ";
            $query .= "     VALUE(L1.SONOTA_GOUKEI, 0) AS SONOTA_GOUKEI, ";
            $query .= "     VALUE(L1.ZENTAI_GOUKEI, 0) AS ZENTAI_GOUKEI ";
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
            $query .= "     VALUE(L1.SHINGAKU_DAIGAKU, 0) AS SHINGAKU_DAIGAKU, ";
            $query .= "     VALUE(L1.SHINGAKU_TANDAI, 0) AS SHINGAKU_TANDAI, ";
            $query .= "     VALUE(L1.SHINGAKU_SENMON, 0) AS SHINGAKU_SENMON, ";
            $query .= "     VALUE(L1.SHINGAKU_SONOTA, 0) AS SHINGAKU_SONOTA, ";
            $query .= "     VALUE(L1.SHINGAKU_REMARK, 0) AS SHINGAKU_REMARK, ";
            $query .= "     VALUE(L1.SHINGAKU_GOUKEI, 0) AS SHINGAKU_GOUKEI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KENNAI, 0) AS SHUSHOKU_KENNAI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KENNAI_KOUMUIN, 0) AS SHUSHOKU_KENNAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_KENGAI, 0) AS SHUSHOKU_KENGAI, ";
            $query .= "     VALUE(L1.SHUSHOKU_KENGAI_KOUMUIN, 0) AS SHUSHOKU_KENGAI_KOUMUIN, ";
            $query .= "     VALUE(L1.SHUSHOKU_SONOTA, 0) AS SHUSHOKU_SONOTA, ";
            $query .= "     VALUE(L1.SHUSHOKU_REMARK, 0) AS SHUSHOKU_REMARK, ";
            $query .= "     VALUE(L1.SHUSHOKU_GOUKEI, 0) AS SHUSHOKU_GOUKEI, ";
            $query .= "     VALUE(L1.SONOTA_GOUKEI, 0) AS SONOTA_GOUKEI, ";
            $query .= "     VALUE(L1.ZENTAI_GOUKEI, 0) AS ZENTAI_GOUKEI ";
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
            $query .= "     L1.SHINGAKU_DAIGAKU + L2.SHINGAKU_DAIGAKU AS SHINGAKU_DAIGAKU, ";
            $query .= "     L1.SHINGAKU_TANDAI + L2.SHINGAKU_TANDAI AS SHINGAKU_TANDAI, ";
            $query .= "     L1.SHINGAKU_SENMON + L2.SHINGAKU_SENMON AS SHINGAKU_SENMON, ";
            $query .= "     L1.SHINGAKU_SONOTA + L2.SHINGAKU_SONOTA AS SHINGAKU_SONOTA, ";
            $query .= "     L1.SHINGAKU_REMARK + L2.SHINGAKU_REMARK AS SHINGAKU_REMARK, ";
            $query .= "     L1.SHINGAKU_GOUKEI + L2.SHINGAKU_GOUKEI AS SHINGAKU_GOUKEI, ";
            $query .= "     L1.SHUSHOKU_KENNAI + L2.SHUSHOKU_KENNAI AS SHUSHOKU_KENNAI, ";
            $query .= "     L1.SHUSHOKU_KENNAI_KOUMUIN + L2.SHUSHOKU_KENNAI_KOUMUIN AS SHUSHOKU_KENNAI_KOUMUIN, ";
            $query .= "     L1.SHUSHOKU_KENGAI + L2.SHUSHOKU_KENGAI AS SHUSHOKU_KENGAI, ";
            $query .= "     L1.SHUSHOKU_KENGAI_KOUMUIN + L2.SHUSHOKU_KENGAI_KOUMUIN AS SHUSHOKU_KENGAI_KOUMUIN, ";
            $query .= "     L1.SHUSHOKU_SONOTA + L2.SHUSHOKU_SONOTA  AS SHUSHOKU_SONOTA, ";
            $query .= "     L1.SHUSHOKU_REMARK + L2.SHUSHOKU_REMARK  AS SHUSHOKU_REMARK, ";
            $query .= "     L1.SHUSHOKU_GOUKEI + L2.SHUSHOKU_GOUKEI  AS SHUSHOKU_GOUKEI, ";
            $query .= "     L1.SONOTA_GOUKEI + L2.SONOTA_GOUKEI AS SONOTA_GOUKEI, ";
            $query .= "     L1.ZENTAI_GOUKEI + L2.ZENTAI_GOUKEI AS ZENTAI_GOUKEI ";
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
            $query .= "         V_AFT_DISEASE_ADDITION410_DAT ";
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
        $query .= "     L1.SHINGAKU_DAIGAKU, ";
        $query .= "     L1.SHINGAKU_TANDAI, ";
        $query .= "     L1.SHINGAKU_SENMON, ";
        $query .= "     L1.SHINGAKU_SONOTA, ";
        $query .= "     L1.SHINGAKU_REMARK, ";
        $query .= "     L1.SHINGAKU_GOUKEI, ";
        $query .= "     L1.SHUSHOKU_KENNAI, ";
        $query .= "     L1.SHUSHOKU_KENNAI_KOUMUIN, ";
        $query .= "     L1.SHUSHOKU_KENGAI, ";
        $query .= "     L1.SHUSHOKU_KENGAI_KOUMUIN, ";
        $query .= "     L1.SHUSHOKU_SONOTA, ";
        $query .= "     L1.SHUSHOKU_REMARK, ";
        $query .= "     L1.SHUSHOKU_GOUKEI, ";
        $query .= "     L1.SONOTA_GOUKEI, ";
        $query .= "     L1.ZENTAI_GOUKEI ";
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
        //再計算
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
            $query .= "     T1.SHINGAKU_DAIGAKU, ";
            $query .= "     T1.SHINGAKU_TANDAI, ";
            $query .= "     T1.SHINGAKU_SENMON, ";
            $query .= "     T1.SHINGAKU_SONOTA, ";
            $query .= "     T1.SHINGAKU_REMARK, ";
            $query .= "     T1.SHINGAKU_GOUKEI, ";
            $query .= "     T1.SHUSHOKU_KENNAI, ";
            $query .= "     T1.SHUSHOKU_KENNAI_KOUMUIN, ";
            $query .= "     T1.SHUSHOKU_KENGAI, ";
            $query .= "     T1.SHUSHOKU_KENGAI_KOUMUIN, ";
            $query .= "     T1.SHUSHOKU_SONOTA, ";
            $query .= "     T1.SHUSHOKU_REMARK, ";
            $query .= "     T1.SHUSHOKU_GOUKEI, ";
            $query .= "     T1.SONOTA_GOUKEI, ";
            $query .= "     T1.ZENTAI_GOUKEI ";
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
        $query  = "DELETE FROM AFT_DISEASE_ADDITION410_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR      = '".$model->field["YEAR"]."'";
        $db->query($query);

        //INSERT
        //進学
        $shingaku_daigaku_sum = "";
        $shingaku_tandai_sum = "";
        $shingaku_senmon_sum = "";
        $shingaku_sonota_sum = "";
        $shingaku_remark_sum = "";
        $shingaku_man_sum = "";
        $shingaku_woman_sum = "";
        $shingaku_total_sum = "";
        
        //就職
        $shushoku_kennai_sum = "";
        $shushoku_kennai_koumuin_sum = "";
        $shushoku_kengai_sum = "";
        $shushoku_kengai_koumuin_sum = "";
        $shushoku_sonota_sum = "";
        $shushoku_remark_sum = "";
        $shushoku_man_sum = "";
        $shushoku_woman_sum = "";
        $shushoku_total_sum = "";
        
        //その他
        $sonota_goukei_sum = "";
        
        //総合計
        $total_man_sum = "";
        $total_woman_sum = "";
        $total_sum = "";
        
        //学科
        $gakka = array();
        for ($i = 0; $i < get_count($model->fields["CODE"]); $i++) {
            //学科・性別
            $code_arr = explode("-", $model->fields["CODE"][$i]);
            $gakka[$i] = $code_arr[0].$code_arr[1];
            //学科が変われば初期化する
            if ($i != 0 && $gakka[$i] != $gakka[$i-1]) {
                $shingaku_daigaku_sum = "";
                $shingaku_tandai_sum = "";
                $shingaku_senmon_sum = "";
                $shingaku_sonota_sum = "";
                $shingaku_remark_sum = "";
                $shingaku_man_sum = "";
                $shingaku_woman_sum = "";
                $shingaku_total_sum = "";
                //就職
                $shushoku_kennai_sum = "";
                $shushoku_kennai_koumuin_sum = "";
                $shushoku_kengai_sum = "";
                $shushoku_kengai_koumuin_sum = "";
                $shushoku_sonota_sum = "";
                $shushoku_remark_sum = "";
                $shushoku_man_sum = "";
                $shushoku_woman_sum = "";
                $shushoku_total_sum = "";
                //その他
                $sonota_goukei_sum = "";
                //総合計
                $total_man_sum = "";
                $total_woman_sum = "";
                $total_sum = "";
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
            /*進学希望者*/
            //大学
            $data["LARGE_DIV"][TEXT]            = '10';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '004';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHINGAKU_DAIGAKU"][$i];
                $shingaku_daigaku_sum += $model->fields["SHINGAKU_DAIGAKU"][$i];
                
                if ($code_arr[2] === '1') {
                    $shingaku_man_sum += $model->fields["SHINGAKU_DAIGAKU"][$i];
                    $total_man_sum += $model->fields["SHINGAKU_DAIGAKU"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shingaku_woman_sum += $model->fields["SHINGAKU_DAIGAKU"][$i];
                    $total_woman_sum += $model->fields["SHINGAKU_DAIGAKU"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shingaku_daigaku_sum;
                $shingaku_total_sum += $shingaku_daigaku_sum;
                $total_sum += $shingaku_daigaku_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);
            
            //短大
            $data["LARGE_DIV"][TEXT]            = '10';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '005';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHINGAKU_TANDAI"][$i];
                $shingaku_tandai_sum += $model->fields["SHINGAKU_TANDAI"][$i];
                if ($code_arr[2] === '1') {
                    $shingaku_man_sum += $model->fields["SHINGAKU_TANDAI"][$i];
                    $total_man_sum += $model->fields["SHINGAKU_TANDAI"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shingaku_woman_sum += $model->fields["SHINGAKU_TANDAI"][$i];
                    $total_woman_sum += $model->fields["SHINGAKU_TANDAI"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shingaku_tandai_sum;
                $shingaku_total_sum += $shingaku_tandai_sum;
                $total_sum += $shingaku_tandai_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);

            //専門
            $data["LARGE_DIV"][TEXT]            = '10';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '006';
            $data["COUNT"][NUMBER]              = $model->fields["SHINGAKU_SENMON"][$i];
            if ($code_arr[2] !== '9') {
                $shingaku_senmon_sum += $model->fields["SHINGAKU_SENMON"][$i];
                if ($code_arr[2] === '1') {
                    $shingaku_man_sum += $model->fields["SHINGAKU_SENMON"][$i];
                    $total_man_sum += $model->fields["SHINGAKU_SENMON"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shingaku_woman_sum += $model->fields["SHINGAKU_SENMON"][$i];
                    $total_woman_sum += $model->fields["SHINGAKU_SENMON"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shingaku_senmon_sum;
                $shingaku_total_sum += $shingaku_senmon_sum;
                $total_sum += $shingaku_senmon_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);
            
            //その他
            $data["LARGE_DIV"][TEXT]            = '10';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '008';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHINGAKU_SONOTA"][$i];
                $shingaku_sonota_sum += $model->fields["SHINGAKU_SONOTA"][$i];
                if ($code_arr[2] === '1') {
                    $shingaku_man_sum += $model->fields["SHINGAKU_SONOTA"][$i];
                    $total_man_sum += $model->fields["SHINGAKU_SONOTA"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shingaku_woman_sum += $model->fields["SHINGAKU_SONOTA"][$i];
                    $total_woman_sum += $model->fields["SHINGAKU_SONOTA"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shingaku_sonota_sum;
                $shingaku_total_sum += $shingaku_sonota_sum;
                $total_sum += $shingaku_sonota_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);

            //追加文
            $data["LARGE_DIV"][TEXT]            = '10';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '009';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHINGAKU_REMARK"][$i];
                $shingaku_remark_sum += $model->fields["SHINGAKU_REMARK"][$i];
                if ($code_arr[2] === '1') {
                    $shingaku_man_sum += $model->fields["SHINGAKU_REMARK"][$i];
                    $total_man_sum += $model->fields["SHINGAKU_REMARK"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shingaku_woman_sum += $model->fields["SHINGAKU_REMARK"][$i];
                    $total_woman_sum += $model->fields["SHINGAKU_REMARK"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shingaku_remark_sum;
                $shingaku_total_sum += $shingaku_remark_sum;
                $total_sum += $shingaku_remark_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);

            //進学希望者合計
            $data["LARGE_DIV"][TEXT]            = '10';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER] = $shingaku_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER] = $shingaku_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]     = $shingaku_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);

            /*就職希望者*/
            //県内
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '01';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KENNAI"][$i];
                $shushoku_kennai_sum += $model->fields["SHUSHOKU_KENNAI"][$i];
                
                if ($code_arr[2] === '1') {
                    $shushoku_man_sum += $model->fields["SHUSHOKU_KENNAI"][$i];
                    $total_man_sum += $model->fields["SHUSHOKU_KENNAI"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shushoku_woman_sum += $model->fields["SHUSHOKU_KENNAI"][$i];
                    $total_woman_sum += $model->fields["SHUSHOKU_KENNAI"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kennai_sum;
                $shushoku_total_sum += $shushoku_kennai_sum;
                $total_sum += $shushoku_kennai_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);
            
            //県内公務員
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '01';
            $data["SMALL_DIV"][TEXT]            = '003';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KENNAI_KOUMUIN"][$i];
                $shushoku_kennai_koumuin_sum += $model->fields["SHUSHOKU_KENNAI_KOUMUIN"][$i];
                
                if ($code_arr[2] === '1') {
                    $shushoku_man_sum += $model->fields["SHUSHOKU_KENNAI_KOUMUIN"][$i];
                    $total_man_sum += $model->fields["SHUSHOKU_KENNAI_KOUMUIN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shushoku_woman_sum += $model->fields["SHUSHOKU_KENNAI_KOUMUIN"][$i];
                    $total_woman_sum += $model->fields["SHUSHOKU_KENNAI_KOUMUIN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kennai_koumuin_sum;
                $shushoku_total_sum += $shushoku_kennai_koumuin_sum;
                $total_sum += $shushoku_kennai_koumuin_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);
            
            //県外
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '02';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KENGAI"][$i];
                $shushoku_kengai_sum += $model->fields["SHUSHOKU_KENGAI"][$i];
                if ($code_arr[2] === '1') {
                    $shushoku_man_sum += $model->fields["SHUSHOKU_KENGAI"][$i];
                    $total_man_sum += $model->fields["SHUSHOKU_KENGAI"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shushoku_woman_sum += $model->fields["SHUSHOKU_KENGAI"][$i];
                    $total_woman_sum += $model->fields["SHUSHOKU_KENGAI"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kengai_sum;
                $shushoku_total_sum += $shushoku_kengai_sum;
                $total_sum += $shushoku_kengai_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);

            //県外公務員
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '02';
            $data["SMALL_DIV"][TEXT]            = '003';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_KENGAI_KOUMUIN"][$i];
                $shushoku_kengai_koumuin_sum += $model->fields["SHUSHOKU_KENGAI_KOUMUIN"][$i];
                if ($code_arr[2] === '1') {
                    $shushoku_man_sum += $model->fields["SHUSHOKU_KENGAI_KOUMUIN"][$i];
                    $total_man_sum += $model->fields["SHUSHOKU_KENGAI_KOUMUIN"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shushoku_woman_sum += $model->fields["SHUSHOKU_KENGAI_KOUMUIN"][$i];
                    $total_woman_sum += $model->fields["SHUSHOKU_KENGAI_KOUMUIN"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_kengai_koumuin_sum;
                $shushoku_total_sum += $shushoku_kengai_koumuin_sum;
                $total_sum += $shushoku_kengai_koumuin_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);
            
            //就職その他
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '08';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_SONOTA"][$i];
                $shushoku_sonota_sum += $model->fields["SHUSHOKU_SONOTA"][$i];
                if ($code_arr[2] === '1') {
                    $shushoku_man_sum += $model->fields["SHUSHOKU_SONOTA"][$i];
                    $total_man_sum += $model->fields["SHUSHOKU_SONOTA"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shushoku_woman_sum += $model->fields["SHUSHOKU_SONOTA"][$i];
                    $total_woman_sum += $model->fields["SHUSHOKU_SONOTA"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_sonota_sum;
                $shushoku_total_sum += $shushoku_sonota_sum;
                $total_sum += $shushoku_sonota_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);

            //就職追加分
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '09';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SHUSHOKU_REMARK"][$i];
                $shushoku_remark_sum += $model->fields["SHUSHOKU_REMARK"][$i];
                if ($code_arr[2] === '1') {
                    $shushoku_man_sum += $model->fields["SHUSHOKU_REMARK"][$i];
                    $total_man_sum += $model->fields["SHUSHOKU_REMARK"][$i];
                } elseif ($code_arr[2] === '2') {
                    $shushoku_woman_sum += $model->fields["SHUSHOKU_REMARK"][$i];
                    $total_woman_sum += $model->fields["SHUSHOKU_REMARK"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $shushoku_remark_sum;
                $shushoku_total_sum += $shushoku_remark_sum;
                $total_sum += $shushoku_remark_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);

            //就職希望者合計
            $data["LARGE_DIV"][TEXT]            = '20';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER] = $shushoku_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER] = $shushoku_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]     = $shushoku_total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);

            //その他の希望者
            $data["LARGE_DIV"][TEXT]            = '30';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                $data["COUNT"][NUMBER]              = $model->fields["SONOTA_GOUKEI"][$i];
                $sonota_goukei_sum += $model->fields["SONOTA_GOUKEI"][$i];
                if ($code_arr[2] === '1') {
                    $total_man_sum += $model->fields["SONOTA_GOUKEI"][$i];
                } elseif ($code_arr[2] === '2') {
                    $total_woman_sum += $model->fields["SONOTA_GOUKEI"][$i];
                }
            } else {
                $data["COUNT"][NUMBER]              = $sonota_goukei_sum;
                $total_sum += $sonota_goukei_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);

            //総合計
            $data["LARGE_DIV"][TEXT]            = '99';
            $data["MIDDLE_DIV"][TEXT]           = '99';
            $data["SMALL_DIV"][TEXT]            = '999';
            if ($code_arr[2] !== '9') {
                if ($code_arr[2] === '1') {
                    $data["COUNT"][NUMBER] = $total_man_sum;
                } elseif ($code_arr[2] === '2') {
                    $data["COUNT"][NUMBER] = $total_woman_sum;
                }
            } else {
                $data["COUNT"][NUMBER]     = $total_sum;
            }
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
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
        $query .= "     REPORT_AFT_DISEASE_ADDITION410_DAT T1";
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
        $query .= "     AFT_DISEASE_ADDITION410_DAT ";
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
        $query .= "     AFT_DISEASE_ADDITION410_DAT ";
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
        $query  = "DELETE FROM AFT_DISEASE_ADDITION410_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db2->query($query);
        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION410_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db->query($query);
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（疾病等結果一覧）
        $query = knje410Query::getAddition1Dat($model);
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
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
            $db2->query($query);
        }
        $result->free();
        
        //報告用データ取得
        $query = knje410Query::getAddition2Dat($model);
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
            $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION410_DAT");
            $db->query($query);
            $db2->query($query);
        }
        $result->free();
        

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
        $query .= "     AFT_DISEASE_ADDITION410_DAT ";
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
        $getCount = $db->getOne(knje410Query::upCheckQuery($model, $setData, $largeDiv, $middleDiv, $smallDiv));
        
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
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION410_DAT");
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
            $query  = Query::updateSQL($data, "AFT_DISEASE_ADDITION410_DAT", $where);
            $db->query($query);
        }
        
        return;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //大学
            knje410Query::insertQuery($db, $model, $data_arr[$i], "10", "99", "004", "SHINGAKU_DAIGAKU");
        
            //短大
            knje410Query::insertQuery($db, $model, $data_arr[$i], "10", "99", "005", "SHINGAKU_TANDAI");

            //専門
            knje410Query::insertQuery($db, $model, $data_arr[$i], "10", "99", "006", "SHINGAKU_SENMON");

            //その他
            knje410Query::insertQuery($db, $model, $data_arr[$i], "10", "99", "008", "SHINGAKU_SONOTA");

            //追加文
            knje410Query::insertQuery($db, $model, $data_arr[$i], "10", "99", "009", "SHINGAKU_REMARK");

            //進学希望者合計
            knje410Query::insertQuery($db, $model, $data_arr[$i], "10", "99", "999", "SHINGAKU_GOUKEI");

            /*就職希望者*/
            //県内
            knje410Query::insertQuery($db, $model, $data_arr[$i], "20", "01", "999", "SHUSHOKU_KENNAI");
            
            //県内公務員
            knje410Query::insertQuery($db, $model, $data_arr[$i], "20", "01", "003", "SHUSHOKU_KENNAI_KOUMUIN");
            
            //県外
            knje410Query::insertQuery($db, $model, $data_arr[$i], "20", "02", "999", "SHUSHOKU_KENGAI");

            //県外公務員
            knje410Query::insertQuery($db, $model, $data_arr[$i], "20", "02", "003", "SHUSHOKU_KENGAI_KOUMUIN");
            
            //就職その他
            knje410Query::insertQuery($db, $model, $data_arr[$i], "20", "08", "999", "SHUSHOKU_SONOTA");

            //就職追加分
            knje410Query::insertQuery($db, $model, $data_arr[$i], "20", "09", "999", "SHUSHOKU_REMARK");

            //就職希望者合計
            knje410Query::insertQuery($db, $model, $data_arr[$i], "20", "99", "999", "SHUSHOKU_GOUKEI");

            //その他の希望者
            knje410Query::insertQuery($db, $model, $data_arr[$i], "30", "99", "999", "SONOTA_GOUKEI");

            //総合計
            knje410Query::insertQuery($db, $model, $data_arr[$i], "99", "99", "999", "ZENTAI_GOUKEI");

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
