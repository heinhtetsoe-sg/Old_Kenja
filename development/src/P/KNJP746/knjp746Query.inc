<?php

require_once('for_php7.php');

class knjp746Query extends Query {
    /********/
    /* 画面 */
    /********/
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    /************/
    /* 処理実行 */
    /************/
    //更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $incomeCnt = 0;

        // 更新対象ID取得(LEVY_REQUEST_INCOME_AUTO_DAT)
        $query = knjp746Query::getAutoDat($model);
        $result = $db->query($query);
        while ($rowAdat = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // 更新
            $incomeCnt += knjp746Query::getUpdateIncome($db, $model, $rowAdat);
        }

        $db->commit();
        Query::dbCheckIn($db);

        $setMassage = "収入伺{$incomeCnt}件";

        return $setMassage;
    }

    // 自動設定データ取得
    function getAutoDat($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     YEAR, ";
        $query .= "     AUTO_NO, ";
        $query .= "     AUTO_NAME, ";
        $query .= "     INCOME_L_CD, ";
        $query .= "     INCOME_M_CD, ";
        $query .= "     REQUEST_REASON, ";
        $query .= "     REQUEST_STAFF, ";
        $query .= "     REMARK, ";
        $query .= "     COMMODITY_PRICE, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     COLLECT_S_CD ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_AUTO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";

        return $query;
    }

    // 自動設定学籍データ取得
    function getAutoSchregDat($model, $autoNo, $collectLcd, $collectMcd, $collectScd, $comPrice) {
        //入金項目M(生徒ごと)
        $query  = " WITH MONEY_SCHREG_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         SUM(VALUE(T1.PLAN_PAID_MONEY, 0)) AS PAID_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_PAID_M_DAT T1 ";
        $query .= "         INNER JOIN LEVY_REQUEST_INCOME_AUTO_SCHREG_DAT AUTO ON AUTO.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                            AND AUTO.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                            AND AUTO.YEAR        = T1.YEAR ";
        $query .= "                                                            AND AUTO.AUTO_NO     = '{$autoNo}' ";
        $query .= "                                                            AND AUTO.SCHREGNO    = T1.SCHREGNO ";
        $query .= "     WHERE  ";
        $query .= "             T1.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "         AND T1.YEAR         = '{$model->year}'  ";
        $query .= "         AND T1.COLLECT_L_CD = '{$collectLcd}' ";
        $query .= "         AND T1.COLLECT_M_CD = '{$collectMcd}' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        //使用した収入額チェック
        $query .= " ), INCOME_USE_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         L1.SCHREGNO, ";
        $query .= "         SUM(L1.INCOME_MONEY) AS INCOME_MONEY ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "         INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                                                     AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                                     AND L1.REQUEST_NO  = T1.REQUEST_NO ";
        $query .= "                                                     AND L1.YEAR        = T1.YEAR ";
        $query .= "                                                     AND L1.INCOME_L_CD = T1.INCOME_L_CD ";
        $query .= "                                                     AND L1.INCOME_M_CD = T1.INCOME_M_CD ";
        $query .= "     WHERE ";
        $query .= "             T1.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "         AND T1.YEAR         = '{$model->year}' ";
        $query .= "         AND T1.COLLECT_L_CD = '{$collectLcd}' ";
        $query .= "         AND T1.COLLECT_M_CD = '{$collectMcd}' ";
        $query .= "         AND T1.COLLECT_S_CD = '{$collectScd}' ";
        $query .= "         AND T1.INCOME_CANCEL IS NULL ";
        $query .= "     GROUP BY ";
        $query .= "         L1.SCHREGNO ";
        //入金額 - 使用する収入額 = 残額 (生徒ごと)
        $query .= " ), INCOME_ZAN_GK_SCHREG_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.PAID_MONEY - VALUE(L1.INCOME_MONEY, 0) AS ZAN_GK ";
        $query .= "     FROM ";
        $query .= "         MONEY_SCHREG_DATA T1  ";
        $query .= "         LEFT JOIN INCOME_USE_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " ) ";
        // メイン
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     ZAN_GK ";
        $query .= " FROM ";
        $query .= "     INCOME_ZAN_GK_SCHREG_DATA ";
        $query .= " WHERE ";
        $query .= "     ZAN_GK >= $comPrice ";

        return $query;
    }

    //MAX伝票番号取得
    function getMaxRequestNo($model) {
        $query  = " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";

        return $query;
    }

    // 更新
    function getUpdateIncome($db, $model, $rowAdat) {
        // 作成伝票カウント
        $incomeCnt = 0;

        // 収入伺い日、収入日をセット
        $requestDate = str_replace("/", "-", $model->requestDate);

        // 行番号セット
        $lineNo = 1;

        // ループ抜けるフラグ
        $breakFlg = false;
        $breakCnt = 0;

        $schoolCd   = $rowAdat["SCHOOLCD"];
        $schoolKind = $rowAdat["SCHOOL_KIND"];
        $year       = $rowAdat["YEAR"];
        $incomeLcd  = $rowAdat["INCOME_L_CD"];
        $incomeMcd  = $rowAdat["INCOME_M_CD"];
        $autoNo     = $rowAdat["AUTO_NO"];
        $collectLcd = $rowAdat["COLLECT_L_CD"];
        $collectMcd = $rowAdat["COLLECT_M_CD"];
        $collectScd = $rowAdat["COLLECT_S_CD"];
        $comPrice   = $rowAdat["COMMODITY_PRICE"];

        // 残額が、単価以上無くなるまでループ
        while (!$breakFlg) {
            if ($breakCnt > 99) { //念の為ループ脱出処理を入れておく
                break;
            }
            $breakCnt++;

            // 最大伝票番号取得
            $getMaxRequestNo = $db->getOne(knjp746Query::getMaxRequestNo($model));
            //対象年度の伝票番号がない場合
            if ($getMaxRequestNo == '') {
                $setRequestNo = $model->year.'A00001';
            } else {
                $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                $setRequestNo = $model->year.'A'.$setNo;
            }

            // 更新対象者取得(LEVY_REQUEST_INCOME_AUTO_DAT)
            $breakFlg = true; // 更新対象者がいない時、ループを抜ける
            $schCnt = 0;
            $query = knjp746Query::getAutoSchregDat($model, $autoNo, $collectLcd, $collectMcd, $collectScd, $comPrice);
            $result = $db->query($query);
            while ($rowSch = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // LEVY_REQUEST_INCOME_SCHREG_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]         = $schoolCd;
                $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                $data["YEAR"][TEXT]             = $year;
                $data["INCOME_L_CD"][TEXT]      = $incomeLcd;
                $data["INCOME_M_CD"][TEXT]      = $incomeMcd;
                $data["REQUEST_NO"][TEXT]       = $setRequestNo;
                $data["INCOME_S_CD"][TEXT]      = sprintf("%03d", $lineNo);//LINE_NOと同一のものをいれておく
                $data["SCHREGNO"][TEXT]         = $rowSch["SCHREGNO"];
                $data["LINE_NO"][NUMBER]        = $lineNo;
                $data["INCOME_MONEY"][NUMBER]   = $comPrice;

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_SCHREG_DAT");
                $db->query($query);

                $schCnt++;
                $breakFlg = false;
            }

            if ($schCnt > 0) {
                // LEVY_REQUEST_INCOME_MEISAI_DAT
                $data2 = array();
                $data2["SCHOOLCD"][TEXT]             = $schoolCd;
                $data2["SCHOOL_KIND"][TEXT]          = $schoolKind;
                $data2["YEAR"][TEXT]                 = $year;
                $data2["INCOME_L_CD"][TEXT]          = $incomeLcd;
                $data2["INCOME_M_CD"][TEXT]          = $incomeMcd;
                $data2["REQUEST_NO"][TEXT]           = $setRequestNo;
                $data2["INCOME_S_CD"][TEXT]          = sprintf("%03d", $lineNo);//LINE_NOと同一のものをいれておく
                $data2["COMMODITY_NAME"][TEXT]       = $rowAdat["AUTO_NAME"];
                $data2["LINE_NO"][NUMBER]            = $lineNo;
                $data2["COMMODITY_PRICE"][NUMBER]    = $comPrice;
                $data2["COMMODITY_CNT"][NUMBER]      = $schCnt;
                $data2["TOTAL_PRICE"][NUMBER]        = $comPrice * $schCnt;
                $data2["WARIHURI_DIV"][TEXT]         = '1'; // 1;生徒 2:生徒以外
                $data2["REMARK"][TEXT]               = $rowAdat["REMARK"];

                $data2["REGISTERCD"][TEXT]           = STAFFCD;
                $data2["UPDATED"][NUMBER]            = "sysdate()";

                $query = Query::insertSQL($data2, "LEVY_REQUEST_INCOME_MEISAI_DAT");
                $db->query($query);

                // LEVY_REQUEST_INCOME_DAT
                $data3 = array();
                $data3["SCHOOLCD"][TEXT]         = $schoolCd;
                $data3["SCHOOL_KIND"][TEXT]      = $schoolKind;
                $data3["YEAR"][TEXT]             = $year;
                $data3["INCOME_L_CD"][TEXT]      = $incomeLcd;
                $data3["INCOME_M_CD"][TEXT]      = $incomeMcd;
                $data3["REQUEST_NO"][TEXT]       = $setRequestNo;
                $data3["REQUEST_DATE"][DATE]     = $requestDate;
                $data3["REQUEST_REASON"][TEXT]   = $rowAdat["REQUEST_REASON"];
                $data3["REQUEST_STAFF"][TEXT]    = $rowAdat["REQUEST_STAFF"];
                $data3["REQUEST_GK"][NUMBER]     = $comPrice * $schCnt;
                $data3["COLLECT_DIV"][TEXT]      = "1";
                $data3["COLLECT_L_CD"][TEXT]     = $collectLcd;
                $data3["COLLECT_M_CD"][TEXT]     = $collectMcd;
                $data3["COLLECT_S_CD"][TEXT]     = $collectScd;
                $data3["INCOME_APPROVAL"][TEXT]  = '1'; // 決済済みにする
                $data3["INCOME_DATE"][TEXT]      = $requestDate;

                $data3["REGISTERCD"][TEXT]       = STAFFCD;
                $data3["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data3, "LEVY_REQUEST_INCOME_DAT");
                $db->query($query);

                $incomeCnt++;
            }
        }
        return $incomeCnt;
    }
}
?>