<?php

require_once('for_php7.php');

class knjta020_07Query extends Query {

    //名称マスタ
    function getSyoriName($namecd2) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'T042' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";
        return $query;
    }
    
    //処理状況取得
    function getShuugakuJyoukyou($shugaku_no, $shinsei_year) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SHUUGAKU_JYOUKYOU ";
        $query .= " FROM ";
        $query .= "     V_SHUUGAKU_JYOUKYOU ";
        $query .= " WHERE ";
        $query .= "     SHUUGAKU_NO = '{$shugaku_no}' ";
        $query .= " AND SHINSEI_YEAR = '{$shinsei_year}' ";
        
        return $query;
    }

    //仮停止
    function getKariteishi($shugakuNo, $shinseiYear) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     TAIYO_KEIKAKU_DAT ";
        $query .= " WHERE ";
        $query .= "     SHUUGAKU_NO = '{$shugakuNo}' ";
        $query .= "     AND SHINSEI_YEAR = '{$shinseiYear}' ";
        $query .= "     AND VALUE(KARI_TEISHI_FLG, '0') = '1' ";

        return $query;
    }

    //処理状況２
    function getShori2($shinseiYear, $kojinNo) {
        $query  = " WITH MAX_DATE AS ( ";
        $query .= " SELECT ";
        $query .= "     KOJIN_NO, ";
        $query .= "     SHINSEI_YEAR AS MAX_SHINSEI_YEAR, ";
        $query .= "     MAX(SHINSEI_DATE) AS MAX_SHINSEI_DATE ";
        $query .= " FROM ";
        $query .= "     KOJIN_IDOU_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$kojinNo}' ";
        $query .= "     AND SHINSEI_YEAR = '{$shinseiYear}' ";
        $query .= " GROUP BY ";
        $query .= "     KOJIN_NO, ";
        $query .= "     SHINSEI_YEAR ";
        $query .= " ), MAX_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.KOJIN_NO, ";
        $query .= "     T1.SHINSEI_YEAR AS MAX_SHINSEI_YEAR, ";
        $query .= "     T1.SHINSEI_DATE AS MAX_SHINSEI_DATE, ";
        $query .= "     MAX(SEQ) AS MAX_SEQ ";
        $query .= " FROM ";
        $query .= "     KOJIN_IDOU_DAT T1, ";
        $query .= "     MAX_DATE ";
        $query .= " WHERE ";
        $query .= "     T1.KOJIN_NO = MAX_DATE.KOJIN_NO ";
        $query .= "     AND T1.SHINSEI_YEAR = MAX_DATE.MAX_SHINSEI_YEAR ";
        $query .= "     AND T1.SHINSEI_DATE = MAX_DATE.MAX_SHINSEI_DATE ";
        $query .= " GROUP BY ";
        $query .= "     T1.KOJIN_NO, ";
        $query .= "     T1.SHINSEI_YEAR, ";
        $query .= "     T1.SHINSEI_DATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     N1.NAME1 ";
        $query .= " FROM ";
        $query .= "     KOJIN_IDOU_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T012' ";
        $query .= "          AND T1.IDOU_DIV = N1.NAMECD2, ";
        $query .= "     MAX_DATA ";
        $query .= " WHERE ";
        $query .= "     T1.KOJIN_NO = MAX_DATA.KOJIN_NO ";
        $query .= "     AND T1.SHINSEI_YEAR = MAX_DATA.MAX_SHINSEI_YEAR ";
        $query .= "     AND T1.SHINSEI_DATE = MAX_DATA.MAX_SHINSEI_DATE ";
        $query .= "     AND T1.SEQ = MAX_DATA.MAX_SEQ ";

        return $query;
    }

    function getShinkenCmb($model) {
        //変数
        $kojinNo = !isset($model->kojinNo) ? $model->maxKojinNo : $model->kojinNo;
        //SQL
        $query .= " SELECT ";
        $query .= "     L1.SHINKEN_CD || ':' || L1.FAMILY_NAME || '　' || L1.FIRST_NAME || '(' || L1.FAMILY_NAME_KANA || '　' || L1.FIRST_NAME_KANA || ')' AS LABEL, ";
        $query .= "     L1.SHINKEN_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINKENSHA_DAT T1 ";
        $query .= "     LEFT JOIN SHINKENSHA_HIST_DAT L1 ON T1.SHINKEN_CD = L1.SHINKEN_CD ";
        $query .= " WHERE ";
        $query .= "     T1.KOJIN_NO = '{$kojinNo}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SHINKEN_CD ";
        $query .= "   ";
        return $query;
    }

    function getSetaiCnt($model, $data) {
        //変数
        $kojinNo = !isset($model->kojinNo) ? $model->maxKojinNo : $model->kojinNo;
        //SQL
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_HIST_DAT T1, ";
        $query .= "     KOJIN_TAIYO_SETAI_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.KOJIN_NO = '{$data["KOJIN_NO"]}' ";
        $query .= "     AND T1.SHINSEI_YEAR = '{$data["SHINSEI_YEAR"]}' ";
        $query .= "     AND T1.SHIKIN_SHOUSAI_DIV = '{$data["SHIKIN_SHOUSAI_DIV"]}' ";
        $query .= "     AND T1.ISSUEDATE = '{$data["SGK_ISSUEDATE"]}' ";
        $query .= "     AND T1.KOJIN_NO = T2.KOJIN_NO ";
        $query .= "     AND T1.SHINSEI_YEAR = T2.SHINSEI_YEAR ";
        $query .= "     AND T1.SETAI_SEQ = T2.SETAI_SEQ ";

        return $query;
    }

    //審査計算書の「判定結果」等のチェック用
    function getShinsaCnt($model, $kjnRow, $flg) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_TAIYO_SHINSA_KEKKA_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->kojinNo}' ";
        $query .= "     AND SHINSEI_YEAR = '{$kjnRow["SHINSEI_YEAR"]}' ";
        $query .= "     AND SHIKIN_SHOUSAI_DIV = '{$kjnRow["SHIKIN_SHOUSAI_DIV"]}' ";
        $query .= "     AND ISSUEDATE = '{$kjnRow["SGK_ISSUEDATE"]}' ";
        if ($flg == "1") {
            if ($kjnRow["SHINSEI_YEAR"] < "2015") {
                $query .= "     AND KEKKA_DIV = '1' AND SETAI_STATUS IS NOT NULL ";
            } else {
                $query .= "     AND KEKKA_DIV = '1' AND ( SHOTOKU_WARI_GK IS NOT NULL AND SETAI_STATUS IS NULL ";
                $query .= "                               OR SETAI_STATUS IN ('1', '5') ) ";
            }
        } else if ($flg == "2") {
            $query .= "     AND KEKKA_DIV = '1' AND TAIYO_HANTEI_KEKKA = '1' ";
        } else {
            $query .= "     AND KEKKA_DIV = '2' AND NINTEI_HANTEI_KEKKA = '1' ";
        }
        return $query;
    }

    //業務コード
    function getGyoumucd() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'T013' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //元号
    function getGengou() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'T001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        return $query;
    }
    //資金区分（世帯）
    function getShikinName($div) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 AS NAME ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'T028' ";
        $query .= "     AND NAMECD2 = '{$div}' ";
        return $query;
    }
    //資金区分詳細
    function getShikinShousaiName($div) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 AS NAME ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'T030' ";
        $query .= "     AND NAMECD2 = '{$div}' ";
        return $query;
    }
    //課程
    function getKatei($schoolCd) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     L1.KATEI AS VALUE, ";
        $query .= "     L1.KATEI || ':' || L1.KATEI_NAME AS LABEL, ";
        $query .= "     L1.SHUUGAKU_MONTH_COUNT AS SMCNT ";
        $query .= " FROM ";
        $query .= "     SCHOOL_KATEI_DAT T1 ";
        $query .= "     INNER JOIN KATEI_MST L1 ON T1.KATEI = L1.KATEI ";
        if ($schoolCd) {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOLCD = '{$schoolCd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
    //貸与不成立理由
    function getTaiyoFailInfo($kojinNo, $shinseiYear, $shikinShousaiDiv, $issuedate) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.TAIYO_FAIL_REMARK, ";
        $query .= "     N1.NAME1 AS TAIYO_FAIL_DIV ";
        $query .= " FROM ";
        $query .= "     KOJIN_TAIYO_SHINSA_KEKKA_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'T023' AND N1.NAMECD2 = T1.TAIYO_FAIL_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.KOJIN_NO = '{$kojinNo}' ";
        $query .= "     AND T1.SHINSEI_YEAR = '{$shinseiYear}' ";
        $query .= "     AND T1.SHIKIN_SHOUSAI_DIV = '{$shikinShousaiDiv}' ";
        $query .= "     AND T1.ISSUEDATE = '{$issuedate}' ";
        $query .= "     AND T1.KEKKA_DIV = '1' ";
        return $query;
    }
    //個人データのチェック
    function checkKojinInfo($model) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_HIST_DAT T1, ";
        $query .= "     V_KOJIN_HIST_DAT V1 ";
        $query .= " WHERE ";
        $query .= "     T1.KOJIN_NO = '{$model->kojinNo}' ";
        $query .= " AND T1.ISSUEDATE = (SELECT ";
        $query .= "                         MAX(M1.ISSUEDATE) ";
        $query .= "                     FROM ";
        $query .= "                         KOJIN_HIST_DAT M1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.KOJIN_NO =M1.KOJIN_NO ";
        if ($model->sanshouYear) {
            $query .= "                     AND FISCALYEAR(M1.ISSUEDATE) <= '{$model->sanshouYear}' ";
        }
        if ($model->shinseiYear) {
            $query .= "                     AND FISCALYEAR(M1.ISSUEDATE) <= '{$model->shinseiYear}' ";
        }
        $query .= "                     ) ";
        $query .= " AND T1.KOJIN_NO = V1.KOJIN_NO ";
        return $query;
    }
    //個人データ
    function getKojinInfo($model) {
        $query  = "";
        $query .= " WITH T_KOJIN AS ( ";
        $query .= " SELECT ";
        if ($model->kojin_count > 0) {
            $query .= "     T1.*, ";
        } else {
            $query .= " DISTINCT V1.*, ";
        }
        $query .= "     V1.ISSUEDATE AS V_ISSUEDATE, ";
        $query .= "     V1.REMARK AS V_REMARK ";
        $query .= " FROM ";
        $query .= "     KOJIN_HIST_DAT T1, ";
        $query .= "     V_KOJIN_HIST_DAT V1 ";
        $query .= " WHERE ";
        $query .= "     T1.KOJIN_NO = '{$model->kojinNo}' ";
        if ($model->kojin_count > 0) {
            $query .= " AND T1.ISSUEDATE = (SELECT ";
            $query .= "                         MAX(M1.ISSUEDATE) ";
            $query .= "                     FROM ";
            $query .= "                         KOJIN_HIST_DAT M1 ";
            $query .= "                     WHERE ";
            $query .= "                         T1.KOJIN_NO =M1.KOJIN_NO ";
            if ($model->sanshouYear) {
                $query .= "                     AND FISCALYEAR(M1.ISSUEDATE) <= '{$model->sanshouYear}' ";
            }
            if ($model->shinseiYear) {
                $query .= "                     AND FISCALYEAR(M1.ISSUEDATE) <= '{$model->shinseiYear}' ";
            }
            $query .= "                     ) ";
        }
        $query .= " AND T1.KOJIN_NO = V1.KOJIN_NO ";
        $query .= "     ) ";
        $query .= " , MAX_SHINSEI AS ( ";
        $query .= "     SELECT ";
        $query .= "         KOJIN_NO, ";
        $query .= "         SHIKIN_SHOUSAI_DIV, ";
        $query .= "         MAX(SHINSEI_YEAR || CAST(ISSUEDATE AS VARCHAR(10))) AS MAX_KEY ";
        $query .= "     FROM ";
        $query .= "         KOJIN_SHINSEI_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         KOJIN_NO = '{$model->kojinNo}' ";
        if ($model->sanshouYear) {
            $query .= "     AND SHINSEI_YEAR = '{$model->sanshouYear}' ";
        }
        if ($model->shinseiYear) {
            $query .= "     AND SHINSEI_YEAR = '{$model->shinseiYear}' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         KOJIN_NO, ";
        $query .= "         SHIKIN_SHOUSAI_DIV ";
        $query .= "     ) ";
        $query .= " , T_SHINSEI AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         KOJIN_SHINSEI_HIST_DAT T1, ";
        $query .= "         MAX_SHINSEI T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.KOJIN_NO = T2.KOJIN_NO ";
        $query .= "         AND T1.SHIKIN_SHOUSAI_DIV = T2.SHIKIN_SHOUSAI_DIV ";
        $query .= "         AND T1.SHINSEI_YEAR || CAST(T1.ISSUEDATE AS VARCHAR(10)) = T2.MAX_KEY ";
        $query .= "     ) ";
        $query .= " , T_YOYAKU AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         KOJIN_TAIYOYOYAKU_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         KOJIN_NO = '{$model->kojinNo}' ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.KOJIN_NO, ";
        $query .= "     T1.ISSUEDATE, ";//更新用
        $query .= "     T1.FAMILY_NAME, ";
        $query .= "     T1.FIRST_NAME, ";
        $query .= "     T1.FAMILY_NAME_KANA, ";
        $query .= "     T1.FIRST_NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.KIKON_FLG, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     T1.CITYCD, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2, ";
        $query .= "     T1.TELNO1, ";
        $query .= "     T1.TELNO2, ";
        $query .= "     T1.TSUUGAKU_DIV, ";
        //02:修直
        $query .= "     T2.NENREI, ";
        $query .= "     T2.YOYAKU_KIBOU_GK, ";
        $query .= "     T2.S_YOYAKU_KIBOU_YM, ";
        $query .= "     T2.E_YOYAKU_KIBOU_YM, ";
        $query .= "     T2.SHINKEN1_CD, ";
        $query .= "     T2.SHINKEN2_CD, ";
        $query .= "     T2.SETAI_SEQ AS SETAI_SEQ_SHOW, ";
        if (!strlen($model->sendShoriDiv)) {
            $query .= "     T2.SHORI_JYOUKYOU AS SHORI_JYOUKYOU1, ";
            $query .= "     T2.SHINSEI_KANRYOU_FLG, ";
            $query .= "     T2.KETTEI_FLG, ";
            $query .= "     T2.ISSUEDATE AS SGK_ISSUEDATE, ";//更新用
            $query .= "     T2.SHINSEI_YEAR, ";
            $query .= "     T2.UKE_YEAR, ";
            $query .= "     T2.UKE_NO, ";
            $query .= "     CASE WHEN T2.UKE_EDABAN = '001' THEN NULL ELSE T2.UKE_EDABAN END AS UKE_EDABAN, ";//'001' → ブランク表示
            $query .= "     T2.SHINSEI_DATE, ";
            $query .= "     T2.SHIKIN_SHOUSAI_DIV, ";
            $query .= "     T2.SHUUGAKU_NO, ";
            $query .= "     T2.H_SCHOOL_CD, ";
            $query .= "     T2.KATEI, ";
            $query .= "     T2.GRADE, ";
            $query .= "     T2.H_GRAD_YM, ";
            $query .= "     T2.S_TAIYO_YM, ";
            $query .= "     T2.E_TAIYO_YM, ";
            $query .= "     T2.HEIKYUU_SHOUGAKU_STATUS1, ";
            $query .= "     T2.HEIKYUU_SHOUGAKU_STATUS2, ";
            $query .= "     T2.HEIKYUU_SHOUGAKU_REMARK2, ";
            $query .= "     T2.HEIKYUU_SHOUGAKU_STATUS3, ";
            $query .= "     T2.HEIKYUU_SHOUGAKU_REMARK3, ";
            $query .= "     T2.HEIKYUU_SHOUGAKU_GYOUMUCD2, ";
            $query .= "     T2.HEIKYUU_SHOUGAKU_GYOUMUCD3, ";
            $query .= "     T2.RENTAI_CD, ";
            $query .= "     CASE WHEN T2.SHINSEI_CANCEL_FLG = '1' THEN 1 ";
            $query .= "          END AS SEND_UN_UPD, ";
            $query .= "     T2.SHITAKU_CANCEL_CHOKU_FLG, ";//TODO:どの区分(02:修直,03:支直)のレコードを参照するのか？
        }
        //03:支直
        $stk = "STK_";
        $query .= "     T3.SHINKEN1_CD AS {$stk}SHINKEN1_CD, ";
        $query .= "     T3.SHINKEN2_CD AS {$stk}SHINKEN2_CD, ";
        if (!strlen($model->sendShoriDiv)) {
            $query .= "     T3.SHORI_JYOUKYOU AS SHORI_JYOUKYOU2, ";
            $query .= "     T3.SHINSEI_KANRYOU_FLG AS {$stk}SHINSEI_KANRYOU_FLG, ";
            $query .= "     T3.KETTEI_FLG AS {$stk}KETTEI_FLG, ";
            $query .= "     T3.ISSUEDATE AS {$stk}ISSUEDATE, ";//更新用
            $query .= "     T3.SHINSEI_YEAR AS {$stk}SHINSEI_YEAR, ";
            $query .= "     T3.UKE_YEAR AS {$stk}UKE_YEAR, ";
            $query .= "     T3.UKE_NO AS {$stk}UKE_NO, ";
            $query .= "     CASE WHEN T3.UKE_EDABAN = '001' THEN NULL ELSE T3.UKE_EDABAN END AS {$stk}UKE_EDABAN, ";//'001' → ブランク表示
            $query .= "     T3.SHINSEI_DATE AS {$stk}SHINSEI_DATE, ";
            $query .= "     T3.SHIKIN_SHOUSAI_DIV AS {$stk}SHIKIN_SHOUSAI_DIV, ";
            $query .= "     T3.SHUUGAKU_NO AS {$stk}SHUUGAKU_NO, ";
            $query .= "     T3.H_SCHOOL_CD AS {$stk}H_SCHOOL_CD, ";
            $query .= "     T3.KATEI AS {$stk}KATEI, ";
            $query .= "     T3.GRADE AS {$stk}GRADE, ";
            $query .= "     T3.SHITAKUKIN_TAIYO_DIV AS {$stk}SHITAKUKIN_TAIYO_DIV, ";
            $query .= "     T3.HEIKYUU_SHITAKU_STATUS1 AS {$stk}HEIKYUU_SHITAKU_STATUS1, ";
            $query .= "     T3.HEIKYUU_SHITAKU_STATUS2 AS {$stk}HEIKYUU_SHITAKU_STATUS2, ";
            $query .= "     T3.HEIKYUU_SHITAKU_REMARK2 AS {$stk}HEIKYUU_SHITAKU_REMARK2, ";
            $query .= "     T3.HEIKYUU_SHITAKU_STATUS3 AS {$stk}HEIKYUU_SHITAKU_STATUS3, ";
            $query .= "     T3.HEIKYUU_SHITAKU_REMARK3 AS {$stk}HEIKYUU_SHITAKU_REMARK3, ";
            $query .= "     T3.HEIKYUU_SHITAKU_GYOUMUCD2 AS {$stk}HEIKYUU_SHITAKU_GYOUMUCD2, ";
            $query .= "     T3.HEIKYUU_SHITAKU_GYOUMUCD3 AS {$stk}HEIKYUU_SHITAKU_GYOUMUCD3, ";
            $query .= "     T3.RENTAI_CD AS {$stk}RENTAI_CD, ";
            $query .= "     CASE WHEN T3.SHINSEI_CANCEL_FLG = '1' THEN 1 ";
            $query .= "          END AS {$stk}SEND_UN_UPD, ";
        }
        //$query .= "     T1.REMARK ";
        $query .= "     T1.V_ISSUEDATE, ";
        $query .= "     T1.V_REMARK AS REMARK ";
        $query .= " FROM ";
        $query .= "     T_KOJIN T1 ";
        if (!strlen($model->sendShoriDiv)) {
            //申請履歴テーブルを参照
            $query .= "     LEFT JOIN T_SHINSEI T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.SHIKIN_SHOUSAI_DIV = '02' ";
            $query .= "     LEFT JOIN T_SHINSEI T3 ON T3.KOJIN_NO = T1.KOJIN_NO AND T3.SHIKIN_SHOUSAI_DIV IN ('03','05') ";
        } else {
            //予約テーブルを参照
            $query .= "     LEFT JOIN T_YOYAKU T2 ON T2.KOJIN_NO = T1.KOJIN_NO ";
            $query .= "     LEFT JOIN T_YOYAKU T3 ON T3.KOJIN_NO = T1.KOJIN_NO ";
        }
        return $query;
    }
    //処理状況(4:貸与中、5:貸与完、6:貸与終了)
    function getKeikaku($shuugakuNo, $shinseiYear, $kojinNo) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MIN(VALUE(SHISHUTSU_GK,-1)) AS MIN_SHISHUTSU_GK, ";
        $query .= "     MAX(YEAR || '-' || MONTH) AS MAX_YM ";
        $query .= " FROM ";
        $query .= "     TAIYO_KEIKAKU_DAT ";
        $query .= " WHERE ";
        $query .= "     SHUUGAKU_NO = '{$shuugakuNo}' ";
        $query .= "     AND SHINSEI_YEAR = '{$shinseiYear}' ";
        $query .= "     AND KOJIN_NO = '{$kojinNo}' ";
        return $query;
    }
    //申請データ
    function getShinseiInfo($model, $ssDiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_KOJIN_SHINSEI_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->kojinNo}' ";
        $query .= "     AND SHIKIN_SHOUSAI_DIV = '{$ssDiv}' ";
        return $query;
    }
    //予約データ
    function getYoyakuInfo($kojinNo) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SHUUGAKU_NO ";
        $query .= " FROM ";
        $query .= "     KOJIN_TAIYOYOYAKU_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$kojinNo}' ";
        return $query;
    }
    //学校情報を取得
    function getSchoolInfo($schoolcd, $check) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SCHOOL_DISTCD AS RITSUCD, ";
        $query .= "     T1.SCHOOL_DIV, ";
        $query .= "     L4.SCHOOL_DIV_NAME, ";
        $query .= "     L3.NAME1 AS RITSU ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD1 = 'T003' ";
        $query .= "                 AND L3.NAMECD2 = T1.SCHOOL_DISTCD ";
        $query .= " LEFT JOIN SCHOOL_DIV_MST L4 ON L4.SCHOOL_DIV = T1.SCHOOL_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD = '{$schoolcd}' ";
        if ($check != "") {
            $query .= " AND  T1.SCHOOL_TYPE = '4' ";
        }
        return $query;
    }
    //課程をチェック
    function getSchoolKateiCnt($schoolcd, $katei) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHOOL_KATEI_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD = '{$schoolcd}' ";
        $query .= "     AND T1.KATEI = '{$katei}' ";
        return $query;
    }

    //開始日付
    function getExistKojinHistDat($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(ISSUEDATE) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->kojinNo}' ";
        $query .= "     AND ISSUEDATE = '{$model->rireki["ISSUEDATE"]}' ";
        return $query;
    }
    //重複チェック
    function getExistKojinShinseiHistDat($model, $stk) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(ISSUEDATE) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->kojinNo}' ";
        $query .= "     AND SHINSEI_YEAR = '{$model->field[$stk."SHINSEI_YEAR"]}' ";
        $query .= "     AND SHIKIN_SHOUSAI_DIV = '{$model->field[$stk."SHIKIN_SHOUSAI_DIV"]}' ";
        $query .= "     AND ISSUEDATE = '{$model->rireki["ISSUEDATE"]}' ";
        return $query;
    }
    //MAX個人番号＋１を取得
    function getMaxKojinNo() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     VALUE(MAX(INT(KOJIN_NO)), 1000000) + 1 AS KOJIN_NO ";
        $query .= " FROM ";
        $query .= "     KOJIN_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO LIKE '1%' ";
        return $query;
    }
    //更新(メイン)
    function update($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjta020_07Query::getKojinInfo($model);
        $kojinData = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //個人履歴データ(Upd or Ins)
        knjta020_07Query::updKojinHistDat($model, $db, $kojinData);

        //世帯状況データ追加(世帯状況が1件もない時) 世帯状況の資金処理区分 1:修直 2:修直＋支直 3:修直＋支利 4:修利
        $setaiSeq = knjta020_07Query::insKojinTaiyoSetaiDat($model, $db, "2", $kojinData);

        //申請履歴データ(Upd or Ins)(02:修直)
        knjta020_07Query::updKojinShinseiHistDat($model, $db, $model->field["SHIKIN_SHOUSAI_DIV"], $kojinData);
        knjta020_07Query::updSetaiSeqJyoukyou($model, $db, $setaiSeq, $model->field["SHIKIN_SHOUSAI_DIV"], $kojinData);

        //申請履歴データ(Upd or Ins)(03:支直)
        if (!strlen($model->field["SHITAKU_CANCEL_CHOKU_FLG"])) {
            knjta020_07Query::updKojinShinseiHistDat($model, $db, $model->field["STK_SHIKIN_SHOUSAI_DIV"], $kojinData);
            knjta020_07Query::updSetaiSeqJyoukyou($model, $db, $setaiSeq, $model->field["STK_SHIKIN_SHOUSAI_DIV"], $kojinData);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
    //個人履歴データ更新
    function updKojinHistDat($model, $db, $kojinData) {
        //項目
        $data = array();
        $data["FAMILY_NAME"][TEXT]      = $model->field["FAMILY_NAME"];
        $data["FIRST_NAME"][TEXT]       = $model->field["FIRST_NAME"];
        $data["FAMILY_NAME_KANA"][TEXT] = mb_convert_kana($model->field["FAMILY_NAME_KANA"], "akh");
        $data["FIRST_NAME_KANA"][TEXT]  = mb_convert_kana($model->field["FIRST_NAME_KANA"], "akh");
        $data["BIRTHDAY"][TEXT]         = $model->field["BIRTHDAY"];
        $data["KIKON_FLG"][TEXT]        = $model->field["KIKON_FLG"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["CITYCD"][TEXT]           = $model->field["CITYCD"];
        $data["ADDR1"][TEXT]            = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]            = $model->field["ADDR2"];
        $data["TELNO1"][TEXT]           = $model->field["TELNO1"];
        $data["TELNO2"][TEXT]           = $model->field["TELNO2"];
        $data["TSUUGAKU_DIV"][TEXT]     = $model->field["TSUUGAKU_DIV"];
        //$data["REMARK"][TEXT]           = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        //レコード
        if (!$kojinData["ISSUEDATE"]) {
            $data["KOJIN_NO"][TEXT]         = !isset($model->kojinNo) ? $model->maxKojinNo : $model->kojinNo;
            $data["ISSUEDATE"][TEXT]        = CTRL_DATE;
            $data["REMARK"][TEXT]           = $model->field["REMARK"];
            //追加
            $query = Query::insertSQL($data, "KOJIN_HIST_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     KOJIN_NO = '{$model->kojinNo}' ";
            $where .= "     AND ISSUEDATE = '{$kojinData["ISSUEDATE"]}' ";
            //更新
            $query = Query::updateSQL($data, "KOJIN_HIST_DAT", $where);
            $db->query($query);
            
            //備考のみ最新にする
            $data2 = array();
            $data2["REMARK"][TEXT]           = $model->field["REMARK"];
            $where  = " WHERE ";
            $where .= "     KOJIN_NO = '{$model->kojinNo}' ";
            $where .= "     AND ISSUEDATE = '{$kojinData["V_ISSUEDATE"]}' ";
            //更新
            $query = Query::updateSQL($data2, "KOJIN_HIST_DAT", $where);
        }
        $db->query($query);
    }
    //申請履歴データ更新
    function updKojinShinseiHistDat($model, $db, $div, $kojinData) {
        $stk = ($div == "02") ? "" : "STK_";
        //変数
        $kojinNo = !isset($model->kojinNo) ? $model->maxKojinNo : $model->kojinNo;
        $ukeEdaban = $model->field[$stk."UKE_EDABAN"] ? $model->field[$stk."UKE_EDABAN"] : "001";
        //項目
        $data = array();
        $data["UKE_YEAR"][TEXT]             = $model->field[$stk."UKE_YEAR"];
        $data["UKE_NO"][TEXT]               = $model->field[$stk."UKE_NO"];
        $data["UKE_EDABAN"][TEXT]           = $ukeEdaban;
        $data["SHINSEI_DATE"][TEXT]         = $model->field[$stk."SHINSEI_DATE"];
        $data["NENREI"][NUMBER]             = $model->field["NENREI"];
        if ($div == "02") {
            $data["SHITAKU_CANCEL_CHOKU_FLG"][TEXT]  = $model->field["SHITAKU_CANCEL_CHOKU_FLG"];
        }
        $data["SHINSEI_KANRYOU_FLG"][TEXT]  = $model->field[$stk."SHINSEI_KANRYOU_FLG"];
        $data["SHINSEI_DIV"][TEXT]          = "3";  //3:新規申請
        $data["H_SCHOOL_CD"][TEXT]          = $model->field[$stk."H_SCHOOL_CD"];
        $data["KATEI"][TEXT]                = $model->field[$stk."KATEI"];
        $data["GRADE"][TEXT]                = $model->field[$stk."GRADE"];
        if ($div == "02") {
            $data["H_GRAD_YM"][TEXT]            = $model->field["H_GRAD_YM"];
            $data["YOYAKU_KIBOU_GK"][NUMBER]    = $model->field["YOYAKU_KIBOU_GK"];
            $data["S_YOYAKU_KIBOU_YM"][TEXT]    = $model->field["S_YOYAKU_KIBOU_YM"];
            $data["E_YOYAKU_KIBOU_YM"][TEXT]    = $model->field["E_YOYAKU_KIBOU_YM"];
            $data["S_TAIYO_YM"][TEXT]           = $model->field["S_TAIYO_YM"];
            $data["E_TAIYO_YM"][TEXT]           = $model->field["E_TAIYO_YM"];
        }
        if ($div != "02") {
            $data["SHITAKUKIN_TAIYO_DIV"][TEXT] = $model->field[$stk."SHITAKUKIN_TAIYO_DIV"];
            //支直の貸与希望額は、支度金貸与区分（1:50,000、2:250,000）を参照しセットする
            if ($model->field[$stk."SHITAKUKIN_TAIYO_DIV"] == "1") {
                $data["YOYAKU_KIBOU_GK"][NUMBER]    = "50000";
            } else if ($model->field[$stk."SHITAKUKIN_TAIYO_DIV"] == "2") {
                $data["YOYAKU_KIBOU_GK"][NUMBER]    = "250000";
            }
            //支直の貸与希望期間（FROMとTO）は、修直の貸与希望期間（FROM）をコピーする
            $data["S_YOYAKU_KIBOU_YM"][TEXT]    = $model->field["S_YOYAKU_KIBOU_YM"];
            $data["E_YOYAKU_KIBOU_YM"][TEXT]    = $model->field["S_YOYAKU_KIBOU_YM"];
            //支直の本年度貸与期間（FROMとTO）は、修直の本年度貸与期間（FROM）をコピーする
            $data["S_TAIYO_YM"][TEXT]           = $model->field["S_TAIYO_YM"];
            $data["E_TAIYO_YM"][TEXT]           = $model->field["S_TAIYO_YM"];

            //修学のデータ
            $issuedateSetai = $kojinData["SGK_ISSUEDATE"];
            $checkDataSetai = strlen($model->sendShoriDiv) || !$issuedateSetai ? CTRL_DATE : $issuedateSetai;
            $query = knjta020_07Query::getShinseiCnt($kojinNo, $model->field["SHINSEI_YEAR"], "02", $checkDataSetai);
            $shinseiCntSetai = $db->getOne($query);

            if ($shinseiCntSetai == 0) {
                $checkDataSetai = CTRL_DATE;
            }
            $query  = " SELECT ";
            $query .= "     SETAI_SEQ ";
            $query .= " FROM ";
            $query .= "     KOJIN_SHINSEI_HIST_DAT ";
            $query .= " WHERE ";
            $query .= "     KOJIN_NO = '{$kojinNo}' ";
            $query .= "     AND SHINSEI_YEAR = '{$model->field["SHINSEI_YEAR"]}' ";
            $query .= "     AND SHIKIN_SHOUSAI_DIV = '02' ";
            $query .= "     AND ISSUEDATE = '{$checkDataSetai}' ";
            $setSetaiSeq = $db->getOne($query);
            $data["SETAI_SEQ"][NUMBER]   = $setSetaiSeq["SETAI_SEQ"];
        }
        if ($div == "02") {
            $data["HEIKYUU_SHOUGAKU_STATUS1"][TEXT] = $model->field["HEIKYUU_SHOUGAKU_STATUS1"];
            $data["HEIKYUU_SHOUGAKU_STATUS2"][TEXT] = $model->field["HEIKYUU_SHOUGAKU_STATUS2"];
            $data["HEIKYUU_SHOUGAKU_REMARK2"][TEXT] = $model->field["HEIKYUU_SHOUGAKU_REMARK2"];
            $data["HEIKYUU_SHOUGAKU_STATUS3"][TEXT] = $model->field["HEIKYUU_SHOUGAKU_STATUS3"];
            $data["HEIKYUU_SHOUGAKU_REMARK3"][TEXT] = $model->field["HEIKYUU_SHOUGAKU_REMARK3"];
            $data["HEIKYUU_SHOUGAKU_GYOUMUCD2"][TEXT] = $model->field["HEIKYUU_SHOUGAKU_GYOUMUCD2"];
            $data["HEIKYUU_SHOUGAKU_GYOUMUCD3"][TEXT] = $model->field["HEIKYUU_SHOUGAKU_GYOUMUCD3"];
        }
        if ($div != "02") {
            $data["HEIKYUU_SHITAKU_STATUS1"][TEXT] = $model->field[$stk."HEIKYUU_SHITAKU_STATUS1"];
            $data["HEIKYUU_SHITAKU_STATUS2"][TEXT] = $model->field[$stk."HEIKYUU_SHITAKU_STATUS2"];
            $data["HEIKYUU_SHITAKU_REMARK2"][TEXT] = $model->field[$stk."HEIKYUU_SHITAKU_REMARK2"];
            $data["HEIKYUU_SHITAKU_STATUS3"][TEXT] = $model->field[$stk."HEIKYUU_SHITAKU_STATUS3"];
            $data["HEIKYUU_SHITAKU_REMARK3"][TEXT] = $model->field[$stk."HEIKYUU_SHITAKU_REMARK3"];
            $data["HEIKYUU_SHITAKU_GYOUMUCD2"][TEXT] = $model->field[$stk."HEIKYUU_SHITAKU_GYOUMUCD2"];
            $data["HEIKYUU_SHITAKU_GYOUMUCD3"][TEXT] = $model->field[$stk."HEIKYUU_SHITAKU_GYOUMUCD3"];
        }
        $data["RENTAI_CD"][TEXT]            = $model->field[$stk."RENTAI_CD"];
        $data["SHINKEN1_CD"][TEXT]          = $model->field[$stk."SHINKEN1_CD"];
        $data["SHINKEN2_CD"][TEXT]          = $model->field[$stk."SHINKEN2_CD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $issuedate = ($div == "02") ? $kojinData["SGK_ISSUEDATE"] : $kojinData["STK_ISSUEDATE"];
        $checkData = strlen($model->sendShoriDiv) || !$issuedate ? CTRL_DATE : $issuedate;
        $query = knjta020_07Query::getShinseiCnt($kojinNo, $model->field[$stk."SHINSEI_YEAR"], $div, $checkData);
        $shinseiCnt = $db->getOne($query);
        if ($shinseiCnt == 0) {
            $data["KOJIN_NO"][TEXT]             = $kojinNo;
            $data["SHINSEI_YEAR"][TEXT]         = $model->field[$stk."SHINSEI_YEAR"];
            $data["SHIKIN_SHOUSAI_DIV"][TEXT]   = $div; //02:修直 03:支直
            $data["ISSUEDATE"][TEXT]            = CTRL_DATE;
            $data["KEIZOKU_KAISUU"][TEXT]       = "1";
            //追加
            $query = Query::insertSQL($data, "KOJIN_SHINSEI_HIST_DAT");
        } else {
            $issuedate = ($div == "02") ? $model->rireki["SGK_ISSUEDATE"] : $model->rireki["STK_ISSUEDATE"];
            $where  = " WHERE ";
            $where .= "     KOJIN_NO = '{$kojinNo}' ";
            $where .= "     AND SHINSEI_YEAR = '{$model->field[$stk."SHINSEI_YEAR"]}' ";
            $where .= "     AND SHIKIN_SHOUSAI_DIV = '{$div}' "; //02:修直 03:支直
            $where .= "     AND ISSUEDATE = '{$checkData}' ";
            //更新
            $query = Query::updateSQL($data, "KOJIN_SHINSEI_HIST_DAT", $where);
        }
        $db->query($query);
    }

    function getShinseiCnt($kojin, $year, $shousaiDiv, $issuedate) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$kojin}' ";
        $query .= "     AND SHINSEI_YEAR = '{$year}' ";
        $query .= "     AND SHIKIN_SHOUSAI_DIV = '{$shousaiDiv}' ";
        $query .= "     AND ISSUEDATE = '{$issuedate}' ";

        return $query;
    }

    //世帯状況データ追加
    function insKojinTaiyoSetaiDat($model, $db, $shikinDiv, $kojinData) {
        //変数
        $kojinNo = !isset($model->kojinNo) ? $model->maxKojinNo : $model->kojinNo;
        //SQL
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_TAIYO_SETAI_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$kojinNo}' ";
        $query .= "     AND SHINSEI_YEAR = '{$model->field["SHINSEI_YEAR"]}' ";
        $cnt = $db->getOne($query);
        //世帯状況が1件もない時は、1件レコード追加
        if ($cnt == 0) {
            //項目
            $data = array();
            $data["KOJIN_NO"][TEXT]         = $kojinNo;
            $data["SHINSEI_YEAR"][TEXT]     = $model->field["SHINSEI_YEAR"];
            $data["SETAI_SEQ"][NUMBER]      = "1";
            $data["SEQ"][NUMBER]            = "1";
            $data["FAMILY_NAME"][TEXT]      = $model->field["FAMILY_NAME"];
            $data["FIRST_NAME"][TEXT]       = $model->field["FIRST_NAME"];
            $data["FAMILY_NAME_KANA"][TEXT] = mb_convert_kana($model->field["FAMILY_NAME_KANA"], "akh");
            $data["FIRST_NAME_KANA"][TEXT]  = mb_convert_kana($model->field["FIRST_NAME_KANA"], "akh");
            $data["TSUZUKIGARA_CD"][TEXT]   = "01";
            $data["NENREI"][NUMBER]         = $model->field["NENREI"];
            $data["SHIKIN_DIV"][TEXT]       = $shikinDiv;
            //追加
            $query = Query::insertSQL($data, "KOJIN_TAIYO_SETAI_DAT");
            $db->query($query);
            return "1";
        } else {
            return;
        }
    }

    //世帯SEQ、処理状況更新
    function updSetaiSeqJyoukyou($model, $db, $setaiSeq, $div, $kojinData) {
        $stk = ($div == "02") ? "" : "STK_";

        //変数
        $kojinNo = !isset($model->kojinNo) ? $model->maxKojinNo : $model->kojinNo;
        $issuedate = ($div == "02") ? $kojinData["SGK_ISSUEDATE"] : $kojinData["STK_ISSUEDATE"];
        $issuedate = strlen($model->sendShoriDiv) || !$issuedate ? CTRL_DATE : $issuedate;

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$kojinNo}' ";
        $query .= "     AND SHINSEI_YEAR = '{$model->field[$stk."SHINSEI_YEAR"]}' ";
        $query .= "     AND SHIKIN_SHOUSAI_DIV = '{$div}' ";
        $query .= "     AND ISSUEDATE = '{$issuedate}' ";

        $shinsei = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query  = " SELECT ";
        $query .= "     MAX(S_STOP_YOTEI_YM) AS S_STOP_YOTEI_YM ";
        $query .= " FROM ";
        $query .= "     KOJIN_IDOU_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.KOJIN_NO = '{$shinsei["KOJIN_NO"]}' ";
        $query .= "     AND T1.IDOU_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'T012' AND NAMESPARE1 = '4') ";
        $query .= "     AND T1.SHINSEI_YEAR = '{$shinsei["SHINSEI_YEAR"]}' ";
        $query .= "     AND T1.SHUUGAKU_NO = '{$shinsei["SHUUGAKU_NO"]}' ";
        $query .= " GROUP BY ";
        $query .= "     SHUUGAKU_NO ";

        $idouYm = $db->getOne($query);

        $query  = " SELECT ";
        $query .= "     SHUUGAKU_NO, ";
        $query .= "     MAX(YEAR || '-' || MONTH) AS YM, ";
        $query .= "     SUM(CASE WHEN SHISHUTSU_GK > 0 THEN 1 ELSE 0 END) AS GK_ARI, ";
        $query .= "     SUM(CASE WHEN SHISHUTSU_GK IS NULL THEN 1 ELSE 0 END) AS GK_NULL ";
        $query .= " FROM ";
        $query .= "     TAIYO_KEIKAKU_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SHUUGAKU_NO = '{$shinsei["SHUUGAKU_NO"]}' ";
        $query .= "     AND SHINSEI_YEAR = '{$model->field[$stk."SHINSEI_YEAR"]}' ";
        $query .= "     AND VALUE(KARI_TEISHI_FLG, '0') != '1' ";
        $query .= "     AND VALUE(TEISHI_FLG, '0') != '1' ";
        $query .= "     AND (VALUE(SHIHARAI_PLAN_GK, 0) != 0 OR SHISHUTSU_GK IS NOT NULL) ";
        $query .= " GROUP BY ";
        $query .= "     SHUUGAKU_NO ";

        $keikaku = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $setJyouKyou = knjta020_07Query::getSyoriJyouKyou($shinsei, $keikaku, $div, $idouYm);
        //項目
        $data = array();
        if ($setaiSeq > 0) {
            $data["SETAI_SEQ"][NUMBER] = $setaiSeq;
        }
        if ($model->setSetaiSeq > 0) {
            $data["SETAI_SEQ"][NUMBER] = $model->setSetaiSeq;
        }
        $data["SHORI_JYOUKYOU"][TEXT]   = $setJyouKyou;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     KOJIN_NO = '{$kojinNo}' ";
        $where .= "     AND SHINSEI_YEAR = '{$model->field[$stk."SHINSEI_YEAR"]}' ";
        $where .= "     AND SHIKIN_SHOUSAI_DIV = '{$div}' "; //02:修直
        $where .= "     AND ISSUEDATE = '{$issuedate}' ";
        //更新
        $query = Query::updateSQL($data, "KOJIN_SHINSEI_HIST_DAT", $where);
        $db->query($query);
    }

    //処理状況取得
    function getSyoriJyouKyou($shinsei, $keikaku, $div, $idouYm) {

        $retJyouKyou = "1";
        if (($idouYm) OR 
            ($shinsei["SHINSEI_KANRYOU_FLG"] == "1" &&
            $shinsei["KETTEI_FLG"] == "1" &&
            $keikaku["GK_NULL"] == "0" &&
            $shinsei["E_YOYAKU_KIBOU_YM"] == $keikaku["YM"])
        ) {
            $retJyouKyou = "6";
        } else if ($shinsei["SHINSEI_KANRYOU_FLG"] == "1" &&
            $shinsei["KETTEI_FLG"] == "1" &&
            $keikaku["GK_NULL"] == "0"
        ) {
            $retJyouKyou = "5";
        } else if ($shinsei["SHINSEI_KANRYOU_FLG"] == "1" &&
            $shinsei["KETTEI_FLG"] == "1" &&
            $keikaku["GK_ARI"] > 0
        ) {
            $retJyouKyou = "4";
        } else if ($shinsei["SHINSEI_KANRYOU_FLG"] == "1" &&
            $shinsei["KETTEI_FLG"] == "1"
        ) {
            $retJyouKyou = "3";
        } else if ($shinsei["SHINSEI_KANRYOU_FLG"] == "1"
        ) {
            $retJyouKyou = "2";
        }
        return $retJyouKyou;
    }

    //削除(メイン)
    function delete($model, $type1, $type2, $shikinShousai) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjta020_07Query::getKojinInfo($model);
        $kojinData = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query = knjta020_07Query::getShinseiAllCnt($model, $kojinData);
        $allCnt = $db->getOne($query);

        if ($allCnt == 1) {
            $query = knjta020_07Query::deleteShinsei($model, $kojinData, $type1, $type2, $shikinShousai, "ALL");
            $db->query($query);

            $query = knjta020_07Query::deleteKekka($model, $kojinData, $type1, $type2, $shikinShousai, "ALL");
            $db->query($query);

            $query = knjta020_07Query::deleteSetai($model, $kojinData, $type1, "ALL");
            $db->query($query);

            $query = knjta020_07Query::deleteKojin($model, $kojinData, "ALL");
            $db->query($query);

            $query = knjta020_07Query::deleteKojinShinken($model, $kojinData, "ALL");
            $db->query($query);
            
            $query = knjta020_07Query::deleteKojinKouza($model);
            $db->query($query);
        } else {
            $query = knjta020_07Query::getShinseiYearCnt($model, $kojinData, $type1);
            $yearCnt = $db->getOne($query);

            $query = knjta020_07Query::deleteShinsei($model, $kojinData, $type1, $type2, $shikinShousai);
            $db->query($query);

            $query = knjta020_07Query::deleteKekka($model, $kojinData, $type1, $type2, $shikinShousai);
            $db->query($query);

            if ($yearCnt == 1) {
                $query = knjta020_07Query::deleteSetai($model, $kojinData, $type1);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function getShinseiAllCnt($model, $kojinData) {

        $query  = " WITH MAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_TAIYOYOYAKU_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";
        //給付追加
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_KYUHU_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";
        $query .= "     AND SEQ = 1 ";  //給付データのカウントなので、1回目を固定で指定
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUM(CNT) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN ";

        return $query;
    }

    function getShinseiYearCnt($model, $kojinData, $type1) {

        $query  = " WITH MAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_TAIYOYOYAKU_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";
        $query .= "     AND SHINSEI_YEAR = '{$kojinData[$type1."SHINSEI_YEAR"]}' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";
        $query .= "     AND SHINSEI_YEAR = '{$kojinData[$type1."SHINSEI_YEAR"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUM(CNT) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN ";

        return $query;
    }

    //申請データ削除
    function deleteShinsei($model, $kojinData, $type1, $type2, $shikinShousai, $div = "") {

        $query  = " DELETE FROM ";
        $query .= "     KOJIN_SHINSEI_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";
        if ($div != "ALL") {
            $query .= "     AND SHINSEI_YEAR = '{$kojinData[$type1."SHINSEI_YEAR"]}' ";
            $query .= "     AND SHIKIN_SHOUSAI_DIV = '{$shikinShousai}' ";
            $query .= "     AND ISSUEDATE = '{$kojinData[$type2."ISSUEDATE"]}' ";
        }

        return $query;
    }

    //審査結果削除
    function deleteKekka($model, $kojinData, $type1, $type2, $shikinShousai, $div = "") {

        $query  = " DELETE FROM ";
        $query .= "     KOJIN_TAIYO_SHINSA_KEKKA_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";
        if ($div != "ALL") {
            $query .= "     AND SHINSEI_YEAR = '{$kojinData[$type1."SHINSEI_YEAR"]}' ";
            $query .= "     AND SHIKIN_SHOUSAI_DIV = '{$shikinShousai}' ";
            $query .= "     AND ISSUEDATE = '{$kojinData[$type2."ISSUEDATE"]}' ";
        }

        return $query;
    }

    //世帯削除
    function deleteSetai($model, $kojinData, $type1, $div = "") {

        $query  = " DELETE FROM ";
        $query .= "     KOJIN_TAIYO_SETAI_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";
        if ($div != "ALL") {
            $query .= "     AND SHINSEI_YEAR = '{$kojinData[$type1."SHINSEI_YEAR"]}' ";
        }

        return $query;
    }

    //個人履歴削除
    function deleteKojin($model, $kojinData, $div = "") {

        $query  = " DELETE FROM ";
        $query .= "     KOJIN_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";

        return $query;
    }

    //個人親権削除
    function deleteKojinShinken($model, $kojinData, $div = "") {

        $query  = " DELETE FROM ";
        $query .= "     KOJIN_SHINKENSHA_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";

        return $query;
    }

/************/
/* 給付処理 */
/************/
    //直近の給付情報を取得
    function getKyuhuZennenDat($model, $Year) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_KYUHU_DAT T1";
        $query .= " WHERE ";
        $query .= "     T1.KOJIN_NO = '{$model->kojinNo}' ";
        if ($Year) {
            $query .= " AND INT(T1.SHINSEI_YEAR) < {$Year} ";
        }
        $query .= " AND T1.SHINSEI_YEAR IN (SELECT ";
        $query .= "                             MAX(M1.SHINSEI_YEAR) AS MAX_YEAR ";
        $query .= "                         FROM ";
        $query .= "                             KOJIN_SHINSEI_KYUHU_DAT M1 ";
        $query .= "                         WHERE ";
        $query .= "                             M1.KOJIN_NO = T1.KOJIN_NO ";
        $query .= "                         AND M1.SHUUGAKU_NO IS NOT NULL ";
        $query .= "                         AND M1.CANCEL_FLG IS NULL ";
        $query .= "                         AND M1.KETTEI_DATE IS NOT NULL  ";
        $query .= "                         AND M1.KETTEI_FLG = '1'  ";
        $query .= "                         ) ";

        $query .= " ORDER BY ";
        $query .= "     T1.SHINSEI_YEAR DESC ";
        $query .= "   , T1.SEQ DESC ";

        return $query;
    }

    //減額申請情報取得
    function getShinseiGengaku($shuugaku_no, $model) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_GENGAKU_DAT ";
        $query .= " WHERE ";
        $query .= "     SHUUGAKU_NO = '{$shuugaku_no}' ";
        $query .= " AND SHINSEI_YEAR = '{$model->shinseiYear}' ";
        
        return $query;
    }

    //担当課情報取得
    function getSectioncd() {
        $query  = " SELECT ";
        $query .= "     T1.SECTIONCD ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1 ";
        $query .= "     LEFT JOIN SECTION_MST L1 ON L1.SECTIONCD = T1.SECTIONCD ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".STAFFCD."' ";
        
        return $query;
    }
    
    //給付の公私区分を取得
    function getKyuhuSchoolDistcd($model, $seq, $setSeq) {
        $shinseiYear = $model->field2["KYUHU_SHINSEI_YEAR".$setSeq];
        $query  = "  SELECT ";
        $query .= "      L1.SCHOOL_DISTCD ";
        $query .= "  FROM ";
        $query .= "      KOJIN_SHINSEI_KYUHU_DAT T1 ";
        $query .= "      LEFT JOIN SCHOOL_DAT L1 ON L1.SCHOOLCD = T1.H_SCHOOL_CD ";
        $query .= "  WHERE ";
        $query .= "      KOJIN_NO = '{$model->kojinNo}' ";
        $query .= "  AND SHINSEI_YEAR = '{$shinseiYear}' ";
        $query .= "  AND T1.SEQ = {$seq} ";

        return $query;
    }

    //給付情報有無チェック
    function getKyuhuShinseiDiv($model) {
    
        $query  = " SELECT ";
        $query .= "     MAX(SHINSEI_DIV) AS SHINSEI_DIV ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_KYUHU_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->kojinNo}' ";
        $query .= " AND SHINSEI_YEAR = '{$model->shinseiYear}' ";

        return $query;
    }

    //給付情報取得
    function getKyuhuInfo($model, $flg, $seq = "") {
       $setFieldName = '';
        if ($seq) {
            $setSeq = $seq == 1 ? '' : '_'.$seq;
        }
        $shinseiYear = $model->field2["KYUHU_SHINSEI_YEAR".$setSeq];

        $query  = " SELECT ";
        $query .= "     SEQ AS KYUHU_SEQ, ";
        $query .= "     SHINSEI_DIV AS KYUHU_SHINSEI_DIV, ";
        $query .= "     KAKEI_KYUHEN_FLG, ";
        $query .= "     KAKEI_KYUHEN_DATE, ";
        $query .= "     SHINSEI_YEAR AS KYUHU_SHINSEI_YEAR, ";
        $query .= "     SHINSEI_DATE AS KYUHU_SHINSEI_DATE, ";
        $query .= "     UKE_YEAR AS KYUHU_UKE_YEAR, ";
        $query .= "     UKE_NO AS KYUHU_UKE_NO, ";
        $query .= "     CASE WHEN UKE_EDABAN = '001' THEN NULL ELSE UKE_EDABAN END AS KYUHU_UKE_EDABAN, ";
        $query .= "     SHUUGAKU_NO AS KYUHU_SHUUGAKU_NO, ";
        $query .= "     SHORI_JYOUKYOU AS SHORI_JYOUKYOU3, ";
        $query .= "     HOGOSHA_CD, ";
        $query .= "     HOGOSHA2_CD, ";
        $query .= "     KYUHU_KAISUU, ";
        $query .= "     H_SCHOOL_CD AS KYUHU_H_SCHOOL_CD, ";
        $query .= "     KATEI AS KYUHU_KATEI, ";
        $query .= "     KATEI_DIV, ";
        $query .= "     GRADE AS KYUHU_GRADE, ";
        $query .= "     HR_CLASS AS KYUHU_HR_CLASS, ";
        $query .= "     ATTENDNO AS KYUHU_ATTENDNO, ";
        $query .= "     SHOTOKUWARI_DIV, ";
        $query .= "     SHOTOKUWARI_GK, ";
        $query .= "     SHOTOKUWARI_GK_CHECK_FLG, ";
        $query .= "     KYOUDAI_BIRTHDAY, ";
        $query .= "     KYOUDAI_TSUZUKIGARA_CD, ";
        $query .= "     KYOUDAI_FAMILY_NAME, ";
        $query .= "     KYOUDAI_FIRST_NAME, ";
        $query .= "     KYOUDAI_FAMILY_NAME_KANA, ";
        $query .= "     KYOUDAI_FIRST_NAME_KANA, ";
        $query .= "     HEIKYUU_SHOUGAKU_FLG1, ";
        $query .= "     HEIKYUU_SHOUGAKU_FLG2, ";
        $query .= "     KANRYOU_FLG, ";
        $query .= "     CANCEL_FLG, ";
        $query .= "     KETTEI_DATE AS KYUHU_KETTEI_DATE , ";
        $query .= "     KETTEI_FLG AS KYUHU_KETTEI_FLG, ";
        $query .= "     REMARK AS KYUHU_REMARK, ";
        $query .= "     KYUHU_YOTEI_GK ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_KYUHU_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->kojinNo}' ";
        if ($model->sendKyuhuDiv === '7' || $model->sendKyuhuDiv === '8') {
            if (!$flg) {
                $query .= " AND SHINSEI_YEAR = '{$model->shinseiYear}' ";
            } else {
                $query .= " AND SHINSEI_YEAR = '{$shinseiYear}' ";
            }
            if ($seq) {
                $query .= "     AND SEQ = {$seq} ";
            }
        } else {
            $query .= " ORDER BY ";
            $query .= "     SHINSEI_YEAR DESC ";
            $query .= "     ,SEQ ASC ";
        }

        return $query;
    }

    //所得割区分名称取得
    function getShotokuwariDivName() {
        $query  = " SELECT ";
        $query .= "     NAME2 AS SHOTOKUWARI_DIV_NAME ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'T049' ";
        $query .= " AND INT(NAMECD2) < 4 ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //処理状況取得(給付は名称マスタのみ)
    function getKyuhuShoriJyouKyou($cd) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'T042' ";
        $query .= " AND NAMECD2 = '{$cd}' ";

        return $query;
    }
    
    //保護者住所の県名取得
    function getHogoshaPref($hogoshaCd) {
        $query  = " SELECT ";
        $query .= "     L1.PREF ";
        $query .= " FROM ";
        $query .= "     SHINKENSHA_HIST_DAT T1 ";
        $query .= "     LEFT JOIN ZIPCD_MST L1 ON L1.NEW_ZIPCD = T1.ZIPCD ";
        $query .= " WHERE ";
        $query .= "     SHINKEN_CD = '{$hogoshaCd}' ";

        return $query;
    }
    
    //続柄取得
    function getNameMst($cd, $model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$cd}' ";
        $query .= " AND NAMESPARE2 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程情報を取得
    function getKateiInfo($getfield, $seq) {
        $setSeq = $seq == 1 ? '' : '_'.$seq;

        $query  = "";
        $query .= " SELECT ";
        $query .= "     L1.*, ";
        $query .= "     L2.SCHOOL_DISTCD AS KOUSHI_DIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_KATEI_DAT T1 ";
        $query .= "     INNER JOIN KATEI_MST L1 ON T1.KATEI = L1.KATEI ";
        $query .= "     INNER JOIN SCHOOL_DAT L2 ON T1.SCHOOLCD = L2.SCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD = '{$getfield["KYUHU_H_SCHOOL_CD".$setSeq]}' ";
        $query .= " AND L1.KATEI = '{$getfield["KYUHU_KATEI".$setSeq]}' ";
        
        return $query;
    }

    //課程区分名称を取得
    function getKateiDivName($kyuhu_katei) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.KATEI_DIV || ':' || L1.NAME1 AS KATEI_DIV_NAME ";
        $query .= " FROM ";
        $query .= "     KATEI_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'T004' ";
        $query .= "                          AND L1.NAMECD2 = T1.KATEI_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.KATEI = '{$kyuhu_katei}' ";
        
        return $query;
    }

    //貸与限度額を取得
    function getTaiyoGendoGkDat($get_tsuugakudiv, $getfield, $seq, $kateidiv, $koushidiv) {
        $setSeq = $seq == 1 ? '' : '_'.$seq;
        $query  = "";
        $query .= " SELECT ";
        $query .= "     GENDO_GK ";
        $query .= " FROM ";
        $query .= "     TAIYO_GENDO_GK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '{$getfield["KYUHU_SHINSEI_YEAR".$setSeq]}' ";
        //所得割区分をその他併給によりセット
        if ($getfield["SHOTOKUWARI_DIV".$setSeq] === '2') {
            if ($getfield["HEIKYUU_SHOUGAKU_FLG1".$setSeq] === '1') {
                $query .= " AND SHOTOKUWARI_DIV = '4' ";
            } else {
                $query .= " AND SHOTOKUWARI_DIV = '{$getfield["SHOTOKUWARI_DIV".$setSeq]}' ";
            }
        } else if ($getfield["SHOTOKUWARI_DIV".$setSeq] === '3') {
            if ($getfield["HEIKYUU_SHOUGAKU_FLG1".$setSeq] === '1') {
                $query .= " AND SHOTOKUWARI_DIV = '5' ";
            } else {
                $query .= " AND SHOTOKUWARI_DIV = '{$getfield["SHOTOKUWARI_DIV".$setSeq]}' ";
            }
        } else {
            $query .= " AND SHOTOKUWARI_DIV = '{$getfield["SHOTOKUWARI_DIV".$setSeq]}' ";
        }
        $query .= " AND KOUSHI_DIV      = '{$koushidiv}' ";
        $query .= " AND KATEI_DIV       = '{$kateidiv}' ";
        $query .= " AND TSUUGAKU_DIV    = '{$get_tsuugakudiv}' ";
        return $query;
    }

    //給付金（マスタ）を取得
    function getMasterKyuhuGkDat($getfield, $seq, $kateidiv, $koushidiv) {
        $setSeq = $seq == 1 ? '' : '_'.$seq;
        $query  = "";
        $query .= " SELECT ";
        $query .= "     KYUHU_GK ";
        $query .= " FROM ";
        $query .= "     KYUHU_GK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '{$getfield["KYUHU_SHINSEI_YEAR".$setSeq]}' ";
        $query .= " AND SHOTOKUWARI_DIV = '{$getfield["SHOTOKUWARI_DIV".$setSeq]}' ";
        $query .= " AND KOUSHI_DIV      = '{$koushidiv}' ";
        $query .= " AND KATEI_DIV       = '{$kateidiv}' ";
        return $query;
    }

    //申請年度の給付金一覧を取得
    function getKyuhuMasterGkList($shinseiYear, $schoolcd, $katei) {
        $query  = "";
        $query .= " WITH GET_KOUSHI_DIV AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_DISTCD AS KOUSHI_DIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD1 = 'T003' ";
        $query .= "                 AND L3.NAMECD2 = T1.SCHOOL_DISTCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD = '{$schoolcd}' ";
        $query .= " )";
        $query .= "  SELECT ";
        $query .= "      T1.SHOTOKUWARI_DIV AS GET_SHOTOKUWARI_DIV, ";
        $query .= "      T1.KYUHU_GK AS GET_KYUHU_GK ";
        $query .= "  FROM ";
        $query .= "      KYUHU_GK_DAT T1 ";
        $query .= "     ,GET_KOUSHI_DIV T2 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '{$shinseiYear}' ";
        $query .= "  AND T1.KOUSHI_DIV = T2.KOUSHI_DIV ";
        $query .= "  AND T1.KATEI_DIV = (SELECT ";
        $query .= "                          K1.KATEI_DIV ";
        $query .= "                      FROM ";
        $query .= "                          KATEI_MST K1 ";
        $query .= "                          LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'T004' ";
        $query .= "                                               AND L1.NAMECD2 = K1.KATEI_DIV ";
        $query .= "                      WHERE ";
        $query .= "                          K1.KATEI = '{$katei}' ";
        $query .= "                      ) ";
        $query .= "  ORDER BY ";
        $query .= "      T1.SHOTOKUWARI_DIV ";

        return $query;
    }

    //申請年度の貸与限度額一覧を取得
    function getTaiyoMasterGkList($shinseiYear, $schoolcd, $katei) {
        $query  = "";
        $query .= " WITH GET_KOUSHI_DIV AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_DISTCD AS KOUSHI_DIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD1 = 'T003' ";
        $query .= "                 AND L3.NAMECD2 = T1.SCHOOL_DISTCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD = '{$schoolcd}' ";
        $query .= " )";
        $query .= "  SELECT ";
        $query .= "      T1.TSUUGAKU_DIV AS GET_TSUUGAKU_DIV, ";
        $query .= "      T1.SHOTOKUWARI_DIV AS GET_SHOTOKUWARI_DIV, ";
        $query .= "      T1.GENDO_GK AS GET_GENDO_GK ";        
        $query .= "  FROM ";
        $query .= "      TAIYO_GENDO_GK_DAT T1 ";
        $query .= "     ,GET_KOUSHI_DIV T2 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '{$shinseiYear}' ";
        $query .= "  AND T1.SHIKIN_SHUBETSU = '1' ";
        $query .= "  AND T1.KOUSHI_DIV = T2.KOUSHI_DIV ";
        $query .= "  AND T1.KATEI_DIV = (SELECT ";
        $query .= "                          K1.KATEI_DIV ";
        $query .= "                      FROM ";
        $query .= "                          KATEI_MST K1 ";
        $query .= "                          LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'T004' ";
        $query .= "                                               AND L1.NAMECD2 = K1.KATEI_DIV ";
        $query .= "                      WHERE ";
        $query .= "                          K1.KATEI = '{$katei}' ";
        $query .= "                      ) ";
        $query .= "  ORDER BY ";
        $query .= "      T1.TSUUGAKU_DIV, ";
        $query .= "      T1.SHOTOKUWARI_DIV ";

        return $query;
    }

    //給付金（支払計画）を取得
    function getKeikakuKyuhuGkDat($getShinseiRow, $seq) {
        $query .= " SELECT ";
        $query .= "     SUM(SHISHUTSU_YOTEI_GK) AS SUM_SHISHUTSU_YOTEI_GK ";
        $query .= " FROM ";
        $query .= "     KYUHU_KEIKAKU_DAT ";
        $query .= " WHERE ";
        $query .= "     SHUUGAKU_NO = '{$getShinseiRow["KYUHU_SHUUGAKU_NO"]}' ";
        $query .= " AND SHINSEI_YEAR = '{$getShinseiRow["KYUHU_SHINSEI_YEAR"]}' ";
        $query .= " AND SEQ = {$seq} ";
        $query .= " GROUP BY ";
        $query .= "     SHUUGAKU_NO, ";
        $query .= "     SHINSEI_YEAR ";
        return $query;
    }
        
    //給付回数の最大値を取得
    function getKyuhuMax($kyuhu_katei) {
        $query  = " SELECT ";
        $query .= "     CEILING((1.0 * SHUUGAKU_MONTH_COUNT) / 12 ) AS KYUHU_MAX ";
        $query .= " FROM ";
        $query .= "     KATEI_MST ";
        $query .= " WHERE ";
        $query .= "     KATEI = '{$kyuhu_katei}' ";
        return $query;
    }

    //給付申請データの給付回数の一覧取得(申請年度以外)
    function getKyuhuKaisuRow($model, $seq) {
        $setSeq = $seq == 1 ? '' : '_'.$seq;

        $query .= " SELECT ";
        $query .= "     KYUHU_KAISUU ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_KYUHU_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->kojinNo}' ";
        $query .= " AND SHINSEI_YEAR <> '{$model->field2["KYUHU_SHINSEI_YEAR".$setSeq]}' ";
        return $query;
    }

    //保護者口座登録確認
    function getHogoshaKouzaCount($kojinNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     KOJIN_KOUZA_BANK_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$kojinNo}' ";
        $query .= " AND TAISHOUSHA_DIV = '4' ";
        $query .= " AND KOUZA_DIV = '2' ";
        return $query;
    }

    //給付金計画データ取得
    function getKyuhuKeikaku($getShinseiRow, $model) {
        $query  = " SELECT ";
        $query .= "     SHUUGAKU_NO, ";
        $query .= "     SUM(CASE WHEN SHISHUTSU_GK > 0 THEN 1 ELSE 0 END) AS GK_ARI, ";
        $query .= "     SUM(CASE WHEN SHISHUTSU_GK IS NULL THEN 1 ELSE 0 END) AS GK_NULL ";
        $query .= " FROM ";
        $query .= "     KYUHU_KEIKAKU_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SHUUGAKU_NO = '{$getShinseiRow["KYUHU_SHUUGAKU_NO"]}' ";
        $query .= "     AND SHINSEI_YEAR = '{$model->field2["KYUHU_SHINSEI_YEAR"]}' ";
        $query .= "     AND VALUE(KARI_TEISHI_FLG, '0') != '1' ";
        $query .= "     AND VALUE(TEISHI_FLG, '0') != '1' ";
        $query .= "     AND (VALUE(SHIHARAI_PLAN_GK, 0) != 0 OR SHISHUTSU_GK IS NOT NULL) ";
        $query .= " GROUP BY ";
        $query .= "     SHUUGAKU_NO ";
        return $query;
    }

    //貸与計画の未払いの最小年月と支出予定額
    function getShishutsuYoteiGk($model) {
        $query  = " SELECT ";
        //$query .= "     T1.YEAR || T1.MONTH AS MIN_YEAR_MONTH, ";
        $query .= "     T1.SHISHUTSU_YOTEI_GK ";
        $query .= " FROM ";
        $query .= "     TAIYO_KEIKAKU_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SHUUGAKU_NO = '{$model->field["SHUUGAKU_NO"]}' ";
        $query .= " AND T1.SHINSEI_YEAR = '{$model->field2["KYUHU_SHINSEI_YEAR"]}' ";
        $query .= " AND T1.SHISHUTSU_GK IS NULL ";
        $query .= " AND VALUE(T1.KARI_TEISHI_FLG, '0') != '1' ";
        $query .= " AND VALUE(T1.TEISHI_FLG, '0') != '1' ";
        $query .= " AND T1.YEAR || T1.MONTH = ( SELECT ";
        $query .= "                                 MIN(M1.YEAR || M1.MONTH) AS MIN_YEAR_MONTH ";
        $query .= "                             FROM ";
        $query .= "                                 TAIYO_KEIKAKU_DAT M1 ";
        $query .= "                             WHERE ";
        $query .= "                                 T1.SHUUGAKU_NO = M1.SHUUGAKU_NO ";
        $query .= "                             AND T1.SHINSEI_YEAR = M1.SHINSEI_YEAR ";
        $query .= "                             AND M1.SHISHUTSU_GK IS NULL ";
        $query .= "                             AND VALUE(M1.KARI_TEISHI_FLG, '0') != '1' ";
        $query .= "                             AND VALUE(M1.TEISHI_FLG, '0') != '1' ";
        $query .= "                             GROUP BY ";
        $query .= "                                 M1.SHUUGAKU_NO, ";
        $query .= "                                 M1.SHINSEI_YEAR ";
        $query .= "                             )  ";
        return $query;
    }

    //減額申請データ確認
    function getGengakuInfo($model) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KOJIN_SHINSEI_GENGAKU_DAT ";
        $query .= " WHERE ";
        $query .= "     SHUUGAKU_NO = '{$model->field["SHUUGAKU_NO"]}' ";
        $query .= " AND SHINSEI_YEAR = '{$model->field2["KYUHU_SHINSEI_YEAR"]}' ";
        return $query;
    }

    //処理状況取得(給付)
    function getKyuhuSyoriJyouKyou($getShinseiRow, $getKeikaku, $model, $seq) {
        $setSeq = $seq == 1 ? '' : '_'.$seq;

        $setJyouKyou = "1";
        //給付終了
        if ($model->field2["KANRYOU_FLG".$setSeq] == "1" &&
            $getShinseiRow["KYUHU_KETTEI_FLG"] == "1" &&
            $getKeikaku["GK_NULL"] == "0" &&
            $getKeikaku["GK_ARI"] > "0" &&
            $model->field2["KYUHU_KAISUU".$setSeq] == $model->kyuhuMax[$seq]
        ) {
            $setJyouKyou = "9";
        //給付完了
        } else if ($model->field2["KANRYOU_FLG".$setSeq] == "1" &&
            $getShinseiRow["KYUHU_KETTEI_FLG"] == "1" &&
            $getKeikaku["GK_NULL"] == "0" &&
            $getKeikaku["GK_ARI"] > "0" &&
            $model->field2["KYUHU_KAISUU".$setSeq] < $model->kyuhuMax[$seq]
        ) {
            $setJyouKyou = "8";
        //給付中
        } else if ($model->field2["KANRYOU_FLG".$setSeq] == "1" &&
            $getShinseiRow["KYUHU_KETTEI_FLG"] == "1" &&
            $getKeikaku["GK_NULL"] != "0" &&
            $getKeikaku["GK_ARI"] > "0"
        ) {
            $setJyouKyou = "7";
        //決定
        } else if ($model->field2["KANRYOU_FLG".$setSeq] == "1" &&
            $getShinseiRow["KYUHU_KETTEI_FLG"] == "1"
        ) {
            $setJyouKyou = "3";
        //入力中
        } else if ($model->field2["KANRYOU_FLG".$setSeq] == "1") {
            $setJyouKyou = "2";
        }
        return $setJyouKyou;
    }

    //更新(給付金データ)
    function updateKyuhu($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $getKyuhuGk = "";

        for ($seq = 1; $seq <= $model->updKyuhuCnt; $seq++) {
            $setSeq = $seq == 1 ? '' : '_'.$seq;
            $shinseiYear = $model->field2["KYUHU_SHINSEI_YEAR".$setSeq];
            //給付申請データ確認
            $query = knjta020_07Query::getKyuhuInfo($model, "update", $seq);
            $getShinseiRow[$seq] = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //課程情報を取得
            $query = knjta020_07Query::getKateiInfo($model->field2, $seq);
            $getKateiRow[$seq] = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$getKateiRow[$seq]["KATEI_DIV"]) {
                $model->setWarning("MSG203", "更新対象の高校の課程情報に不備があります。\\n課程マスタにて、全･定･通区分を確認してください。");

                $db->commit();
                Query::dbCheckIn($db);

                return false;
            //給付金額を取得
            } else if ($getKateiRow[$seq]["KATEI_DIV"] != "" && $getShinseiRow[$seq]["KYUHU_SHUUGAKU_NO"] != "" && $getShinseiRow[$seq]["KYUHU_KETTEI_DATE"] != "" && $getShinseiRow[$seq]["KYUHU_KETTEI_FLG"] != ""){
                $query = knjta020_07Query::getMasterKyuhuGkDat($model->field2, $seq, $getKateiRow[$seq]["KATEI_DIV"], $getKateiRow[$seq]["KOUSHI_DIV"]);
                $getKyuhuGk = $db->getOne($query);
                if (!$getKyuhuGk) {
                    $model->setWarning("MSG203", "更新対象の給付金申請データに対応した奨学給付金が\\n奨学給付金マスタに登録されていません。");

                    $db->commit();
                    Query::dbCheckIn($db);

                    return false;
                }
            }

            //給付計画情報取得
            $query = knjta020_07Query::getKyuhuKeikaku($getShinseiRow[$seq], $model);
            $getKeikaku = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //処理状況取得
            $setKyuhuJyouKyou = knjta020_07Query::getKyuhuSyoriJyouKyou($getShinseiRow[$seq], $getKeikaku, $model, $seq);

            //給付申請データの更新
            $kyuhuUkeEdaban = $model->field2["KYUHU_UKE_EDABAN".$setSeq] ? $model->field2["KYUHU_UKE_EDABAN".$setSeq] : "001";
            $data = array();
            $data["SHINSEI_DATE"][TEXT]             = $model->field2["KYUHU_SHINSEI_DATE".$setSeq];
            $data["KAKEI_KYUHEN_FLG"][TEXT]         = $model->field2["KAKEI_KYUHEN_FLG".$setSeq];
            $data["KAKEI_KYUHEN_DATE"][TEXT]        = $model->field2["KAKEI_KYUHEN_DATE".$setSeq];
            $data["UKE_YEAR"][TEXT]                 = $model->field2["KYUHU_UKE_YEAR".$setSeq];
            $data["UKE_NO"][TEXT]                   = $model->field2["KYUHU_UKE_NO".$setSeq];
            $data["UKE_EDABAN"][TEXT]               = $kyuhuUkeEdaban;
            $data["SHINSEI_DIV"][TEXT]              = ($this->sendKyuhuDiv != "" ? $this->sendKyuhuDiv : $model->maxKyuhuDiv);
            $data["SHORI_JYOUKYOU"][TEXT]           = $setKyuhuJyouKyou;
            $data["HOGOSHA_CD"][TEXT]               = $model->field2["HOGOSHA_CD".$setSeq];
            $data["HOGOSHA2_CD"][TEXT]              = $model->field2["HOGOSHA2_CD".$setSeq];
            $data["KYUHU_KAISUU"][NUMBER]           = $model->field2["KYUHU_KAISUU".$setSeq];
            $data["SHUUGAKU_NO"][TEXT]              = $model->field2["KYUHU_SHUUGAKU_NO".$setSeq];//画面の給付番号を引き継ぐ
            $data["H_SCHOOL_CD"][TEXT]              = $model->field2["KYUHU_H_SCHOOL_CD".$setSeq];
            $data["KOUSHI_DIV"][TEXT]               = $model->field2["KYUHU_RITSUCD".$setSeq];
            $data["KATEI"][TEXT]                    = $model->field2["KYUHU_KATEI".$setSeq];
            $data["KATEI_DIV"][TEXT]                = $getKateiRow["KATEI_DIV"];
            $data["GRADE"][TEXT]                    = $model->field2["KYUHU_GRADE".$setSeq];
            $data["HR_CLASS"][TEXT]                 = $model->field2["KYUHU_HR_CLASS".$setSeq];
            $data["ATTENDNO"][TEXT]                 = $model->field2["KYUHU_ATTENDNO".$setSeq];
            $data["SHOTOKUWARI_DIV"][TEXT]          = $model->field2["SHOTOKUWARI_DIV".$setSeq];
            $data["SHOTOKUWARI_GK"][NUMBER]         = ($model->field2["SHOTOKUWARI_GK".$setSeq] == "") ? "0" : $model->field2["SHOTOKUWARI_GK".$setSeq];
            $data["SHOTOKUWARI_GK_CHECK_FLG"][TEXT] = $model->field2["SHOTOKUWARI_GK_CHECK_FLG".$setSeq];
            if ($model->field2["SHOTOKUWARI_DIV".$setSeq] == "3") {
                $data["KYOUDAI_BIRTHDAY"][TEXT]         = $model->field2["KYOUDAI_BIRTHDAY".$setSeq];
                $data["KYOUDAI_TSUZUKIGARA_CD"][TEXT]   = $model->field2["KYOUDAI_TSUZUKIGARA_CD".$setSeq];
                $data["KYOUDAI_FAMILY_NAME"][TEXT]      = $model->field2["KYOUDAI_FAMILY_NAME".$setSeq];
                $data["KYOUDAI_FIRST_NAME"][TEXT]       = $model->field2["KYOUDAI_FIRST_NAME".$setSeq];
                $data["KYOUDAI_FAMILY_NAME_KANA"][TEXT] = $model->field2["KYOUDAI_FAMILY_NAME_KANA".$setSeq];
                $data["KYOUDAI_FIRST_NAME_KANA"][TEXT]  = $model->field2["KYOUDAI_FIRST_NAME_KANA".$setSeq];
            } else {
                $data["KYOUDAI_BIRTHDAY"][TEXT]         = "";
                $data["KYOUDAI_TSUZUKIGARA_CD"][TEXT]   = "";
                $data["KYOUDAI_FAMILY_NAME"][TEXT]      = "";
                $data["KYOUDAI_FIRST_NAME"][TEXT]       = "";
                $data["KYOUDAI_FAMILY_NAME_KANA"][TEXT] = "";
                $data["KYOUDAI_FIRST_NAME_KANA"][TEXT]  = "";
            }
            $data["HEIKYUU_SHOUGAKU_FLG1"][TEXT]    = $model->field2["HEIKYUU_SHOUGAKU_FLG1".$setSeq];
            $data["HEIKYUU_SHOUGAKU_FLG2"][TEXT]    = $model->field2["HEIKYUU_SHOUGAKU_FLG2".$setSeq];
            $data["KANRYOU_FLG"][TEXT]              = $model->field2["KANRYOU_FLG".$setSeq];
            $data["REMARK"][TEXT]                   = $model->field2["KYUHU_REMARK".$setSeq];
            $data["KYUHU_YOTEI_GK"][NUMBER]         = $model->field2["KYUHU_YOTEI_GK".$setSeq];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][NUMBER]                = "sysdate()";
            if (!is_array($getShinseiRow[$seq])) {
                $data["KOJIN_NO"][TEXT]                 = $model->kojinNo;
                $data["SHINSEI_YEAR"][TEXT]             = $shinseiYear;
                $data["SEQ"][NUMBER]                    = $seq;
                //新規
                $query = Query::insertSQL($data, "KOJIN_SHINSEI_KYUHU_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     KOJIN_NO = '{$model->kojinNo}' ";
                $where .= "     AND SHINSEI_YEAR = '{$shinseiYear}' ";
                $where .= "     AND SEQ = {$seq} ";
                //更新
                $query = Query::updateSQL($data, "KOJIN_SHINSEI_KYUHU_DAT", $where);
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除(給付メイン)
    function deleteKyuhuMain($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjta020_07Query::getKyuhuInfo($model, "");
        $kojinDataKyuhu = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query = knjta020_07Query::getShinseiAllCnt($model, $kojinDataKyuhu);
        $allCnt = $db->getOne($query);

        if ($allCnt == 1) {
            $query = knjta020_07Query::deleteKyuhu($model, $kojinDataKyuhu, "ALL");
            $db->query($query);

            $query = knjta020_07Query::deleteGengaku($model, $kojinDataKyuhu, "ALL");
            $db->query($query);

            $query = knjta020_07Query::deleteKojin($model, $kojinDataKyuhu, "ALL");
            $db->query($query);

            $query = knjta020_07Query::deleteKojinShinken($model, $kojinDataKyuhu, "ALL");
            $db->query($query);
            
            $query = knjta020_07Query::deleteKojinKouza($model);
            $db->query($query);
        } else {
            $query = knjta020_07Query::deleteKyuhu($model, $kojinDataKyuhu);
            $db->query($query);
            if ($model->field["SHUUGAKU_NO"]) {
                $query = knjta020_07Query::deleteGengaku($model, $kojinDataKyuhu);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除(給付金データ)
    function deleteKyuhu($model, $kojinData, $div = "") {

        $query  = " DELETE FROM ";
        $query .= "     KOJIN_SHINSEI_KYUHU_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";
        if ($div != "ALL") {
            $query .= "     AND SHINSEI_YEAR = '{$kojinData["KYUHU_SHINSEI_YEAR"]}' ";
        }
        return $query;
    }
    
    //削除(減額申請データ)
    function deleteGengaku($model, $kojinData, $div = "") {

        $query  = " DELETE FROM ";
        $query .= "     KOJIN_SHINSEI_GENGAKU_DAT ";
        $query .= " WHERE ";
        $query .= "     SHUUGAKU_NO = '{$model->field["SHUUGAKU_NO"]}' ";
        $query .= " AND KOJIN_NO    = '{$model->field["KOJIN_NO"]}' ";
        if ($div != "ALL") {
            $query .= "     AND SHINSEI_YEAR = '{$kojinData["KYUHU_SHINSEI_YEAR"]}' ";
        }
        return $query;
    }

    //口座情報削除
    function deleteKojinKouza($model, $div = "") {

        $query  = " DELETE FROM ";
        $query .= "     KOJIN_KOUZA_BANK_DAT ";
        $query .= " WHERE ";
        $query .= "     KOJIN_NO = '{$model->field["KOJIN_NO"]}' ";

        return $query;
    }

}
?>