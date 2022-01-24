<?php

require_once('for_php7.php');

class knjp915Query extends Query
{
    /****************/
    /* 事前チェック */
    /****************/

    //名称マスタ取得
    public function getNameMst($model, $namecd1)
    {
        $query .= "  SELECT ";
        $query .= "      NAME1 ";
        $query .= "  FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "  WHERE ";
        $query .= "          YEAR    = '{$model->year}' ";
        $query .= "      AND NAMECD1 = '{$namecd1}' ";
        $query .= "  ORDER BY ";
        $query .= "      NAMECD2 ";

        return $query;
    }

    // 学年取得
    public function getGdat($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->lastYear}' ";

        return $query;
    }

    // 締めテーブル取得
    public function getCloseDat($model, $grade)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     LEVY_CLOSE_GRADE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '{$model->lastYear}' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //存在 -- COLLECT_M_MST
    public function getExistsCollectMMst($model, $collectLMcd)
    {
        $query  = "  SELECT ";
        $query .= "      COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "      COLLECT_M_MST ";
        $query .= "  WHERE ";
        $query .= "          SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "      AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "      AND YEAR        = '{$model->year}' ";
        $query .= "      AND COLLECT_L_CD || COLLECT_M_CD = '{$collectLMcd}' ";

        return $query;
    }

    /********/
    /* 画面 */
    /********/
    //校種取得
    public function getSchkind($model)
    {
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
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //繰越項目取得
    public function getCarryLMcd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     INCOME_L_CD || INCOME_M_CD || ";
        $query .= "     case ";
        $query .= "         when SCHREGNO = 'NOT_SCH' then '2' ";
        $query .= "         when SCHREGNO = 'HASUU'   then '3' ";
        $query .= "         else '1' ";
        $query .= "     end AS VALUE, ";
        $query .= "     INCOME_L_CD || INCOME_M_CD || ':' || LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND YEAR        = '".$model->lastYear."' ";
        $query .= "     AND value(TO_INCOME_FLG, '0') = '0' ";
        $query .= "     AND CARRY_CANCEL is null ";

        return $query;
    }

    //振替項目取得
    public function getIncomeLMcd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE, ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        if ($model->warihuriDiv == '3') { //端数の時は支出を参照
            $query .= "     AND T1.LEVY_IN_OUT_DIV = '2' ";
        } else {
            $query .= "     AND T1.LEVY_IN_OUT_DIV = '1' ";
        }
        $query .= "     AND T1.ZATU_FLG IS NULL  ";
        $query .= "     AND T1.YOBI_FLG IS NULL  ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //預り金項目取得(キャンセル処理)
    public function getIncomeLMcdCancel($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     'A' || '-' || T1.FROM_SCHOOL_KIND || '-' || MMST.LEVY_L_CD || MMST.LEVY_M_CD AS VALUE, ";
        $query .= "     MMST.LEVY_L_CD || MMST.LEVY_M_CD || ':' || MMST.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_M_MST MMST ON MMST.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                              AND MMST.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                              AND MMST.YEAR        = T1.YEAR ";
        $query .= "                              AND MMST.LEVY_L_CD   = T1.INCOME_L_CD ";
        $query .= "                              AND MMST.LEVY_M_CD   = T1.INCOME_M_CD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.COLLECT_DIV = '2' ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     'B' || '-' || T1.FROM_SCHOOL_KIND || '-' || MMST.LEVY_L_CD || MMST.LEVY_M_CD AS VALUE, ";
        $query .= "     MMST.LEVY_L_CD || MMST.LEVY_M_CD || ':' || MMST.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_OUTGO_DAT T1 ";
        $query .= "     LEFT JOIN LEVY_M_MST MMST ON MMST.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                              AND MMST.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                              AND MMST.YEAR        = T1.YEAR ";
        $query .= "                              AND MMST.LEVY_L_CD   = T1.OUTGO_L_CD ";
        $query .= "                              AND MMST.LEVY_M_CD   = T1.OUTGO_M_CD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND T1.YEAR        = '{$model->year}' ";
        $query .= "     AND T1.COLLECT_DIV = '2' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************/
    /* 処理実行 */
    /************/
    //更新
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //繰越項目
        $incomeLcd   = substr($model->carryLMcd, 0, 2);
        $incomeMcd   = substr($model->carryLMcd, 2, 2);

        //収入項目
        list($schoolKind, $incomeLMcdSaki) = explode('-', $model->incomeLMcdSaki);
        $incomeLcd_saki = substr($incomeLMcdSaki, 0, 2);
        $incomeMcd_saki = substr($incomeLMcdSaki, 2, 2);

        // 繰越項目名称取得
        $getLevyMname = $db->getOne(knjp915Query::getLevyMmst($model, $incomeLcd, $incomeMcd, $model->warihuriDiv));
        $requestReason = $setCommodityName = "{$getLevyMname}（{$model->lastYear}年度繰越）";

        $setScd = '001';

        // 最大伝票番号(収入)取得
        $getMaxRequestNo = $db->getOne(knjp915Query::getMaxRequestNo($model, "LEVY_REQUEST_INCOME_DAT", $schoolKind));

        // 最大伝票番号(支出)取得
        $getMaxRequestNoOutgo = $db->getOne(knjp915Query::getMaxRequestNo($model, "LEVY_REQUEST_OUTGO_DAT", $schoolKind));

        $cnt = 1;
        //生徒
        if ($model->warihuriDiv == '1') {
            // 対象生徒
            $schregArr = explode(',', $model->selectStudent);
            foreach ($schregArr as $schregNoCarry) {
                list($schregNo, $carryOverMoney) = explode(':', $schregNoCarry);

                //繰越テーブルに、収入伺移行フラグを立てる
                $data = array();
                $data["TO_INCOME_FLG"][TEXT]    = '1';
                $data["TO_SCHOOL_KIND"][TEXT]   = $schoolKind;
                $data["TO_INCOME_L_CD"][TEXT]   = $incomeLcd_saki;
                $data["TO_INCOME_M_CD"][TEXT]   = $incomeMcd_saki;

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $where .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
                $where .= " AND YEAR        = '{$model->lastYear}' ";
                $where .= " AND SCHREGNO    = '{$schregNo}' ";
                $where .= " AND INCOME_L_CD = '{$incomeLcd}' ";
                $where .= " AND INCOME_M_CD = '{$incomeMcd}' ";
                $query = Query::updateSQL($data, "LEVY_CARRY_OVER_DAT", $where);
                $db->query($query);

                //伝票番号セット
                $setNoIncome     = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + $cnt);
                $setIncomeRequestNo = $model->year.'A'.$setNoIncome;

                /* 移行先項目に収入伝票を次年度に作成 */
                // LEVY_REQUEST_INCOME_SCHREG_DAT--
                $data = array();
                $data["SCHOOLCD"][TEXT]         = (sprintf("%012d", SCHOOLCD));
                $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                $data["YEAR"][TEXT]             = $model->year;
                $data["INCOME_L_CD"][TEXT]      = $incomeLcd_saki;
                $data["INCOME_M_CD"][TEXT]      = $incomeMcd_saki;
                $data["REQUEST_NO"][TEXT]       = $setIncomeRequestNo;
                $data["INCOME_S_CD"][TEXT]      = $setScd;
                $data["SCHREGNO"][TEXT]         = $schregNo;
                $data["LINE_NO"][NUMBER]        = 1;
                $data["INCOME_MONEY"][NUMBER]   = $carryOverMoney;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_SCHREG_DAT");
                $db->query($query);

                // LEVY_REQUEST_INCOME_MEISAI_DAT
                $data2 = array();
                $data2["SCHOOLCD"][TEXT]            = (sprintf("%012d", SCHOOLCD));
                $data2["SCHOOL_KIND"][TEXT]         = $schoolKind;
                $data2["YEAR"][TEXT]                = $model->year;
                $data2["INCOME_L_CD"][TEXT]         = $incomeLcd_saki;
                $data2["INCOME_M_CD"][TEXT]         = $incomeMcd_saki;
                $data2["REQUEST_NO"][TEXT]          = $setIncomeRequestNo;
                $data2["INCOME_S_CD"][TEXT]         = $setScd;
                $data2["LINE_NO"][NUMBER]           = 1;
                $data2["COMMODITY_NAME"][TEXT]      = $setCommodityName;
                $data2["COMMODITY_PRICE"][NUMBER]   = $carryOverMoney;
                $data2["COMMODITY_CNT"][NUMBER]     = 1;
                $data2["TOTAL_PRICE"][NUMBER]       = $carryOverMoney;
                $data2["WARIHURI_DIV"][TEXT]        = '1'; // 1;生徒 2:生徒以外
                $data2["REGISTERCD"][TEXT]          = STAFFCD;
                $data2["UPDATED"][NUMBER]           = "sysdate()";

                $query = Query::insertSQL($data2, "LEVY_REQUEST_INCOME_MEISAI_DAT");
                $db->query($query);

                // LEVY_REQUEST_INCOME_DAT
                $data3 = array();
                $data3["SCHOOLCD"][TEXT]                = (sprintf("%012d", SCHOOLCD));
                $data3["SCHOOL_KIND"][TEXT]             = $schoolKind;
                $data3["YEAR"][TEXT]                    = $model->year;
                $data3["INCOME_L_CD"][TEXT]             = $incomeLcd_saki;
                $data3["INCOME_M_CD"][TEXT]             = $incomeMcd_saki;
                $data3["REQUEST_NO"][TEXT]              = $setIncomeRequestNo;
                $data3["REQUEST_DATE"][DATE]            = str_replace('/', '-', $model->incomeDate);
                $data3["REQUEST_REASON"][TEXT]          = $requestReason;
                $data3["REQUEST_STAFF"][TEXT]           = STAFFCD;
                $data3["REQUEST_GK"][NUMBER]            = $carryOverMoney;
                $data3["COLLECT_DIV"][TEXT]             = '2'; // 1:入金項目 2:繰越項目 3:振替処理項目
                $data3["COLLECT_WARIHURI_DIV"][TEXT]    = $model->warihuriDiv; // 1:生徒 2:生徒以外 3:端数
                $data3["COLLECT_L_CD"][TEXT]            = substr($model->collectLMcd, 0, 2);
                $data3["COLLECT_M_CD"][TEXT]            = substr($model->collectLMcd, 2, 2);
                $data3["INCOME_APPROVAL"][TEXT]         = '1'; // 決済済みにする
                $data3["INCOME_DATE"][TEXT]             = str_replace('/', '-', $model->incomeDate);
                $data3["INCOME_NO"][TEXT]               = $setIncomeRequestNo;
                $data3["INCOME_STAFF"][TEXT]            = STAFFCD;
                $data3["FROM_SCHOOL_KIND"][TEXT]        = $model->schoolKind;
                $data3["REGISTERCD"][TEXT]              = STAFFCD;
                $data3["UPDATED"][NUMBER]               = "sysdate()";

                $query = Query::insertSQL($data3, "LEVY_REQUEST_INCOME_DAT");
                $db->query($query);

                $cnt++;
            }
        } else {
            //生徒以外
            if ($model->warihuriDiv == '2') {
                $schregNo = 'NOT_SCH';

                //繰越テーブルに、収入伺移行フラグを立てる
                $data = array();
                $data["TO_INCOME_FLG"][TEXT]    = '1';
                $data["TO_SCHOOL_KIND"][TEXT]   = $schoolKind;
                $data["TO_INCOME_L_CD"][TEXT]   = $incomeLcd_saki;
                $data["TO_INCOME_M_CD"][TEXT]   = $incomeMcd_saki;

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $where .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
                $where .= " AND YEAR        = '{$model->lastYear}' ";
                $where .= " AND SCHREGNO    = '{$schregNo}' ";
                $where .= " AND INCOME_L_CD = '{$incomeLcd}' ";
                $where .= " AND INCOME_M_CD = '{$incomeMcd}' ";
                $query = Query::updateSQL($data, "LEVY_CARRY_OVER_DAT", $where);
                $db->query($query);

                //伝票番号セット
                $setNoIncome     = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                $setIncomeRequestNo = $model->year.'A'.$setNoIncome;

                //繰越金額取得
                $query = knjp915Query::getCarryOverMoney($model, $schregNo, $incomeLcd, $incomeMcd);
                $carryOverMoney = $db->getOne($query);

                /* 移行先項目に収入伝票を作成 */
                // LEVY_REQUEST_INCOME_MEISAI_DAT
                $data2 = array();
                $data2["SCHOOLCD"][TEXT]            = (sprintf("%012d", SCHOOLCD));
                $data2["SCHOOL_KIND"][TEXT]         = $schoolKind;
                $data2["YEAR"][TEXT]                = $model->year;
                $data2["INCOME_L_CD"][TEXT]         = $incomeLcd_saki;
                $data2["INCOME_M_CD"][TEXT]         = $incomeMcd_saki;
                $data2["REQUEST_NO"][TEXT]          = $setIncomeRequestNo;
                $data2["INCOME_S_CD"][TEXT]         = $setScd;
                $data2["LINE_NO"][NUMBER]           = 1;
                $data2["COMMODITY_NAME"][TEXT]      = $setCommodityName;
                $data2["COMMODITY_PRICE"][NUMBER]   = $carryOverMoney;
                $data2["COMMODITY_CNT"][NUMBER]     = 1;
                $data2["TOTAL_PRICE"][NUMBER]       = $carryOverMoney;
                $data2["WARIHURI_DIV"][TEXT]        = '2'; // 1;生徒 2:生徒以外
                $data2["REGISTERCD"][TEXT]          = STAFFCD;
                $data2["UPDATED"][NUMBER]           = "sysdate()";

                $query = Query::insertSQL($data2, "LEVY_REQUEST_INCOME_MEISAI_DAT");
                $db->query($query);

                // LEVY_REQUEST_INCOME_DAT
                $data3 = array();
                $data3["SCHOOLCD"][TEXT]                = (sprintf("%012d", SCHOOLCD));
                $data3["SCHOOL_KIND"][TEXT]             = $schoolKind;
                $data3["YEAR"][TEXT]                    = $model->year;
                $data3["INCOME_L_CD"][TEXT]             = $incomeLcd_saki;
                $data3["INCOME_M_CD"][TEXT]             = $incomeMcd_saki;
                $data3["REQUEST_NO"][TEXT]              = $setIncomeRequestNo;
                $data3["REQUEST_DATE"][DATE]            = str_replace('/', '-', $model->incomeDate);
                $data3["REQUEST_REASON"][TEXT]          = $requestReason;
                $data3["REQUEST_STAFF"][TEXT]           = STAFFCD;
                $data3["REQUEST_GK"][NUMBER]            = $carryOverMoney;
                $data3["COLLECT_DIV"][TEXT]             = '2'; // 1:入金項目 2:繰越項目 3:振替処理項目
                $data3["COLLECT_WARIHURI_DIV"][TEXT]    = $model->warihuriDiv; // 1:生徒 2:生徒以外 3:端数
                $data3["COLLECT_L_CD"][TEXT]            = substr($model->collectLMcd, 0, 2);
                $data3["COLLECT_M_CD"][TEXT]            = substr($model->collectLMcd, 2, 2);
                $data3["INCOME_APPROVAL"][TEXT]         = '1'; // 決済済みにする
                $data3["INCOME_DATE"][TEXT]             = str_replace('/', '-', $model->incomeDate);
                $data3["INCOME_NO"][TEXT]               = $setIncomeRequestNo;
                $data3["INCOME_STAFF"][TEXT]            = STAFFCD;
                $data3["FROM_SCHOOL_KIND"][TEXT]        = $model->schoolKind;
                $data3["REGISTERCD"][TEXT]              = STAFFCD;
                $data3["UPDATED"][NUMBER]               = "sysdate()";

                $query = Query::insertSQL($data3, "LEVY_REQUEST_INCOME_DAT");
                $db->query($query);

            //端数（支出伝票）
            } elseif ($model->warihuriDiv == '3') {
                $schregNo = 'HASUU';
                $setScd   = 'HAS';

                //繰越テーブルに、収入伺移行フラグを立てる
                $data = array();
                $data["TO_INCOME_FLG"][TEXT]    = '1';
                $data["TO_SCHOOL_KIND"][TEXT]   = $schoolKind;
                $data["TO_INCOME_L_CD"][TEXT]   = $incomeLcd_saki;
                $data["TO_INCOME_M_CD"][TEXT]   = $incomeMcd_saki;

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $where  = " WHERE ";
                $where .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $where .= " AND SCHOOL_KIND = '{$model->schoolKind}' ";
                $where .= " AND YEAR        = '{$model->lastYear}' ";
                $where .= " AND SCHREGNO    = '{$schregNo}' ";
                $where .= " AND INCOME_L_CD = '{$incomeLcd}' ";
                $where .= " AND INCOME_M_CD = '{$incomeMcd}' ";
                $query = Query::updateSQL($data, "LEVY_CARRY_OVER_DAT", $where);
                $db->query($query);

                //伝票番号セット
                $setNoOutgo     = sprintf("%05d", intval(substr($getMaxRequestNoOutgo, 5, 5)) + 1);
                $setOutgoRequestNo = $model->year.'B'.$setNoOutgo;

                //繰越金額取得
                $query = knjp915Query::getCarryOverMoney($model, $schregNo, $incomeLcd, $incomeMcd);
                $carryOverMoney = $db->getOne($query);

                /* 移行先項目に支出伝票を作成 */
                // LEVY_REQUEST_OUTGO_MEISAI_DAT
                $data2 = array();
                $data2["SCHOOLCD"][TEXT]            = (sprintf("%012d", SCHOOLCD));
                $data2["SCHOOL_KIND"][TEXT]         = $schoolKind;
                $data2["YEAR"][TEXT]                = $model->year;
                $data2["REQUEST_NO"][TEXT]          = $setOutgoRequestNo;
                $data2["LINE_NO"][NUMBER]           = 1;
                $data2["OUTGO_L_CD"][TEXT]          = $incomeLcd_saki;
                $data2["OUTGO_M_CD"][TEXT]          = $incomeMcd_saki;
                $data2["OUTGO_S_CD"][TEXT]          = $setScd;
                $data2["HASUU"][NUMBER]             = $carryOverMoney;
                $data2["WARIHURI_DIV"][TEXT]        = '2'; // 1;生徒 2:生徒以外
                $data2["REGISTERCD"][TEXT]          = STAFFCD;
                $data2["UPDATED"][NUMBER]           = "sysdate()";

                $query = Query::insertSQL($data2, "LEVY_REQUEST_OUTGO_MEISAI_DAT");
                $db->query($query);

                // LEVY_REQUEST_OUTGO_DAT
                $data3 = array();
                $data3["SCHOOLCD"][TEXT]                = (sprintf("%012d", SCHOOLCD));
                $data3["SCHOOL_KIND"][TEXT]             = $schoolKind;
                $data3["YEAR"][TEXT]                    = $model->year;
                $data3["OUTGO_L_CD"][TEXT]              = $incomeLcd_saki;
                $data3["OUTGO_M_CD"][TEXT]              = $incomeMcd_saki;
                $data3["REQUEST_NO"][TEXT]              = $setOutgoRequestNo;
                $data3["REQUEST_DATE"][DATE]            = str_replace('/', '-', $model->incomeDate);
                $data3["REQUEST_STAFF"][TEXT]           = STAFFCD;
                $data3["COLLECT_DIV"][TEXT]             = '2'; // 1:入金項目 2:繰越項目 3:振替処理項目
                $data3["INCOME_L_CD"][TEXT]             = $incomeLcd;
                $data3["INCOME_M_CD"][TEXT]             = $incomeMcd;
                $data3["OUTGO_APPROVAL"][TEXT]          = '1'; // 決済済みにする
                $data3["OUTGO_DATE"][TEXT]              = str_replace('/', '-', $model->incomeDate);
                $data3["FROM_SCHOOL_KIND"][TEXT]        = $model->schoolKind;
                $data3["REGISTERCD"][TEXT]              = STAFFCD;
                $data3["UPDATED"][NUMBER]               = "sysdate()";

                $query = Query::insertSQL($data3, "LEVY_REQUEST_OUTGO_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //キャンセル
    public function getCancelUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //繰越項目
        list($div, $fromSchoolKind, $incomeLMcdCancel) = explode('-', $model->incomeLMcdCancel);
        $incomeLcd   = substr($incomeLMcdCancel, 0, 2);
        $incomeMcd   = substr($incomeLMcdCancel, 2, 2);

        //繰越テーブルの、収入伺移行フラグを削除
        $data = array();
        $data["TO_INCOME_FLG"][TEXT]    = '';
        $data["TO_SCHOOL_KIND"][TEXT]   = '';
        $data["TO_INCOME_L_CD"][TEXT]   = '';
        $data["TO_INCOME_M_CD"][TEXT]   = '';

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     SCHOOLCD       = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= " AND SCHOOL_KIND    = '{$fromSchoolKind}' ";
        $where .= " AND YEAR           = '{$model->lastYear}' ";
        $where .= " AND TO_INCOME_FLG  = '1' ";
        $where .= " AND TO_INCOME_L_CD = '{$incomeLcd}' ";
        $where .= " AND TO_INCOME_M_CD = '{$incomeMcd}' ";
        $query = Query::updateSQL($data, "LEVY_CARRY_OVER_DAT", $where);
        $db->query($query);

        //収入伝票
        if ($div == 'A') {
            //収入伝票削除
            //LEVY_REQUEST_INCOME_SCHREG_DAT
            $query  = " DELETE FROM LEVY_REQUEST_INCOME_SCHREG_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "     AND YEAR        = '{$model->year}' ";
            $query .= "     AND INCOME_L_CD = '{$incomeLcd}' ";
            $query .= "     AND INCOME_M_CD = '{$incomeMcd}' ";
            $query .= "     AND REQUEST_NO  in (SELECT ";
            $query .= "                              REQUEST_NO ";
            $query .= "                          FROM ";
            $query .= "                              LEVY_REQUEST_INCOME_DAT ";
            $query .= "                          WHERE ";
            $query .= "                                  SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "                              AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "                              AND YEAR        = '{$model->year}' ";
            $query .= "                              AND INCOME_L_CD = '{$incomeLcd}' ";
            $query .= "                              AND INCOME_M_CD = '{$incomeMcd}' ";
            $query .= "                              AND COLLECT_DIV = '2' "; // 繰越から作成されたもの
            $query .= "                          ) ";
            $db->query($query);

            //LEVY_REQUEST_INCOME_MEISAI_DAT
            $query  = " DELETE FROM LEVY_REQUEST_INCOME_MEISAI_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "     AND YEAR        = '{$model->year}' ";
            $query .= "     AND INCOME_L_CD = '{$incomeLcd}' ";
            $query .= "     AND INCOME_M_CD = '{$incomeMcd}' ";
            $query .= "     AND REQUEST_NO  in (SELECT ";
            $query .= "                              REQUEST_NO ";
            $query .= "                          FROM ";
            $query .= "                              LEVY_REQUEST_INCOME_DAT ";
            $query .= "                          WHERE ";
            $query .= "                                  SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "                              AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "                              AND YEAR        = '{$model->year}' ";
            $query .= "                              AND INCOME_L_CD = '{$incomeLcd}' ";
            $query .= "                              AND INCOME_M_CD = '{$incomeMcd}' ";
            $query .= "                              AND COLLECT_DIV = '2' "; // 繰越から作成されたもの
            $query .= "                          ) ";
            $db->query($query);

            //LEVY_REQUEST_INCOME_DAT
            $query  = " DELETE FROM LEVY_REQUEST_INCOME_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "     AND YEAR        = '{$model->year}' ";
            $query .= "     AND INCOME_L_CD = '{$incomeLcd}' ";
            $query .= "     AND INCOME_M_CD = '{$incomeMcd}' ";
            $query .= "     AND COLLECT_DIV = '2' "; // 繰越から作成されたもの
            $db->query($query);

        //支出伝票
        } elseif ($div == 'B') {
            //支出伝票削除
            //LEVY_REQUEST_OUTGO_MEISAI_DAT
            $query  = " DELETE FROM LEVY_REQUEST_OUTGO_MEISAI_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "     AND YEAR        = '{$model->year}' ";
            $query .= "     AND OUTGO_L_CD  = '{$incomeLcd}' ";
            $query .= "     AND OUTGO_M_CD  = '{$incomeMcd}' ";
            $query .= "     AND REQUEST_NO  in (SELECT ";
            $query .= "                              REQUEST_NO ";
            $query .= "                          FROM ";
            $query .= "                              LEVY_REQUEST_OUTGO_DAT ";
            $query .= "                          WHERE ";
            $query .= "                                  SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "                              AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "                              AND YEAR        = '{$model->year}' ";
            $query .= "                              AND OUTGO_L_CD  = '{$incomeLcd}' ";
            $query .= "                              AND OUTGO_M_CD  = '{$incomeMcd}' ";
            $query .= "                              AND COLLECT_DIV = '2' "; // 繰越から作成されたもの
            $query .= "                          ) ";
            $db->query($query);

            //LEVY_REQUEST_OUTGO_DAT
            $query  = " DELETE FROM LEVY_REQUEST_OUTGO_DAT ";
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
            $query .= "     AND YEAR        = '{$model->year}' ";
            $query .= "     AND OUTGO_L_CD  = '{$incomeLcd}' ";
            $query .= "     AND OUTGO_M_CD  = '{$incomeMcd}' ";
            $query .= "     AND COLLECT_DIV = '2' "; // 繰越から作成されたもの
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    // 対象生徒
    public function getTergetSchreg($model)
    {
        $incomeLcd   = substr($model->carryLMcd, 0, 2);
        $incomeMcd   = substr($model->carryLMcd, 2, 2);
        $warihuriDiv = substr($model->carryLMcd, 4, 1);

        // 学籍情報
        $query  = " WITH MAX_SEME AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         max(SEMESTER) AS MAX_SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), REGD_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE || T1.HR_CLASS as GRADE_HR_CLASS, ";
        $query .= "         HDAT.HR_NAME, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         BASE.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN MAX_SEME T2 ON T2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "                               AND T2.MAX_SEMESTER = T1.SEMESTER ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = T1.YEAR ";
        $query .= "                                        AND HDAT.SEMESTER = T1.SEMESTER ";
        $query .= "                                        AND HDAT.GRADE    = T1.GRADE ";
        $query .= "                                        AND HDAT.HR_CLASS = T1.HR_CLASS ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR  = '{$model->year}' ";
        $query .= " ) ";
        // メイン
        $query .= " SELECT ";
        $query .= "     value(REGD.GRADE_HR_CLASS, '') || value(REGD.ATTENDNO, '') || '_' || CARRY.SCHREGNO || ':' || CHAR(CARRY.CARRY_OVER_MONEY) as VALUE, ";
        $query .= "     case ";
        $query .= "         when REGD.HR_NAME is null then '在籍無し　' || BASE.NAME ";
        $query .= "         else value(REGD.HR_NAME, '') || value(REGD.ATTENDNO, '') || '番　' || value(REGD.NAME, '') ";
        $query .= "     end as LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT CARRY ";
        $query .= "     LEFT JOIN REGD_DATA REGD ON REGD.SCHREGNO = CARRY.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = CARRY.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         CARRY.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND CARRY.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND CARRY.YEAR        = '{$model->lastYear}' ";
        $query .= "     AND CARRY.INCOME_L_CD = '{$incomeLcd}' ";
        $query .= "     AND CARRY.INCOME_M_CD = '{$incomeMcd}' ";
        $query .= "     AND CARRY.CARRY_CANCEL is null ";
        $query .= "     AND CARRY.TO_INCOME_FLG is null ";
        $query .= "     AND CARRY.SCHREGNO not in ('NOT_SCH', 'HASUU') ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE_HR_CLASS || REGD.ATTENDNO, ";
        $query .= "     CARRY.SCHREGNO ";

        return $query;
    }

    // MAX伝票番号取得
    public function getMaxRequestNo($model, $table, $schoolKind)
    {
        $query  = " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";

        return $query;
    }

    // 繰越金額取得
    public function getCarryOverMoney($model, $schregNo, $incomeLcd, $incomeMcd)
    {
        $query  = " SELECT ";
        $query .= "     CARRY_OVER_MONEY ";
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->lastYear}' ";
        $query .= "     AND SCHREGNO    = '{$schregNo}' ";
        $query .= "     AND INCOME_L_CD = '{$incomeLcd}' ";
        $query .= "     AND INCOME_M_CD = '{$incomeMcd}' ";

        return $query;
    }

    // 預り金項目名称取得
    public function getLevyMmst($model, $incomeLcd, $incomeMcd, $div)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     LEVY_M_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_CARRY_OVER_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->lastYear}' ";
        $query .= "     AND INCOME_L_CD = '{$incomeLcd}' ";
        $query .= "     AND INCOME_M_CD = '{$incomeMcd}' ";
        if ($div == '1') {
            $query .= "     AND SCHREGNO not in ('NOT_SCH', 'HASUU') ";
        } elseif ($div == '2') {
            $query .= "     AND SCHREGNO  = 'NOT_SCH' ";
        } elseif ($div == '3') {
            $query .= "     AND SCHREGNO  = 'HASUU' ";
        }

        return $query;
    }
}
