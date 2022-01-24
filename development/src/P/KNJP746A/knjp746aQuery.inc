<?php

require_once('for_php7.php');

class knjp746aQuery extends Query
{
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

    /************/
    /* 処理実行 */
    /************/
    //更新
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $incomeCnt = 0;

        // 更新対象ID取得(LEVY_REQUEST_INCOME_AUTO_DAT)
        $query = knjp746aQuery::getAutoDat($model);
        $result = $db->query($query);
        while ($rowAdat = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // 更新
            knjp746aQuery::getUpdateIncome($db, $model, $rowAdat);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    // 自動設定データ取得
    public function getAutoDat($model)
    {
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
        $query .= "     COLLECT_S_CD, ";
        $query .= "     AUTO_INCOME_APPROVAL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_AUTO_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";

        return $query;
    }

    //入金日
    public function getPaidDate($db, $model)
    {
        $query = knjp746aQuery::getPaidMDatQuery($model, "DATE");
        return $query;
    }


    // 自動設定学籍データ取得
    public function getPaidMDatQuery($model, $div, $autoNo = "", $collectLcd = "", $collectMcd = "", $collectScd = "")
    {
        $query .= " SELECT ";
        if ($div == "DATE") {
            $query .= "     REPLACE(PAID.PLAN_PAID_MONEY_DATE, '-', '/') AS LABEL, ";
            $query .= "     PAID.PLAN_PAID_MONEY_DATE AS VALUE ";
        } else {
            $query .= "     PAID.SCHREGNO, ";
            $query .= "     SUM(PAID.PLAN_PAID_MONEY) AS INCOME_MONEY, ";
            $query .= "     DENSE_RANK() OVER(ORDER BY SUM(PAID.PLAN_PAID_MONEY)) AS LINE_NO ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_M_DAT PAID ";
        $query .= "     INNER JOIN LEVY_REQUEST_INCOME_AUTO_DAT AUTO ";
        $query .= "         ON AUTO.SCHOOLCD        = PAID.SCHOOLCD ";
        $query .= "         AND AUTO.SCHOOL_KIND    = PAID.SCHOOL_KIND ";
        $query .= "         AND AUTO.YEAR           = PAID.YEAR ";
        $query .= "         AND AUTO.COLLECT_L_CD   = PAID.COLLECT_L_CD ";
        $query .= "         AND AUTO.COLLECT_M_CD   = PAID.COLLECT_M_CD ";
        $query .= " WHERE ";
        $query .= "         AUTO.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND AUTO.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND AUTO.YEAR        = '{$model->year}' ";
        if ($autoNo != "") {
            $query .= "     AND AUTO.AUTO_NO        = '{$autoNo}' ";
        }
        if ($collectLcd != "") {
            $query .= "     AND AUTO.COLLECT_L_CD        = '{$collectLcd}' ";
        }
        if ($collectMcd != "") {
            $query .= "     AND AUTO.COLLECT_M_CD        = '{$collectMcd}' ";
        }
        if ($collectScd != "") {
            $query .= "     AND AUTO.COLLECT_S_CD        = '{$collectScd}' ";
        }
        $query .= "     AND NOT EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             LEVY_REQUEST_INCOME_SCHREG_DAT SCH_IN ";
        $query .= "             INNER JOIN LEVY_REQUEST_INCOME_DAT INCOME ";
        $query .= "                 ON SCH_IN.SCHOOLCD      = INCOME.SCHOOLCD ";
        $query .= "                 AND SCH_IN.SCHOOL_KIND  = INCOME.SCHOOL_KIND ";
        $query .= "                 AND SCH_IN.YEAR         = INCOME.YEAR ";
        $query .= "                 AND SCH_IN.INCOME_L_CD  = INCOME.INCOME_L_CD ";
        $query .= "                 AND SCH_IN.INCOME_M_CD  = INCOME.INCOME_M_CD ";
        $query .= "                 AND SCH_IN.REQUEST_NO   = INCOME.REQUEST_NO ";
        $query .= "         WHERE ";
        $query .= "             INCOME.SCHOOLCD         = AUTO.SCHOOLCD ";
        $query .= "             AND INCOME.SCHOOL_KIND  = AUTO.SCHOOL_KIND ";
        $query .= "             AND INCOME.YEAR         = AUTO.YEAR ";
        $query .= "             AND INCOME.INCOME_L_CD  = AUTO.INCOME_L_CD ";
        $query .= "             AND INCOME.INCOME_M_CD  = AUTO.INCOME_M_CD ";
        $query .= "             AND INCOME.COLLECT_L_CD = PAID.COLLECT_L_CD ";
        $query .= "             AND INCOME.COLLECT_M_CD = PAID.COLLECT_M_CD ";
        $query .= "             AND INCOME.INCOME_CANCEL IS NULL ";
        $query .= "             AND SCH_IN.SCHREGNO     = PAID.SCHREGNO ";
        $query .= "     ) ";
        if ($div == "MAIN") {
            $query .= "     AND PAID.PLAN_PAID_MONEY_DATE  = '{$model->paidDate}' ";
        }
        $query .= " GROUP BY ";
        if ($div == "DATE") {
            $query .= "     PAID.PLAN_PAID_MONEY_DATE ";
        } else {
            $query .= "     PAID.SCHREGNO ";
        }

        return $query;
    }

    //MAX伝票番号取得
    public function getMaxRequestNo($model)
    {
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
    public function getUpdateIncome($db, $model, $rowAdat)
    {
        // 収入伺い日、収入日をセット
        $paidDate = str_replace("/", "-", $model->paidDate);

        $schoolCd           = $rowAdat["SCHOOLCD"];
        $schoolKind         = $rowAdat["SCHOOL_KIND"];
        $year               = $rowAdat["YEAR"];
        $incomeLcd          = $rowAdat["INCOME_L_CD"];
        $incomeMcd          = $rowAdat["INCOME_M_CD"];
        $autoNo             = $rowAdat["AUTO_NO"];
        $autoName           = $rowAdat["AUTO_NAME"];
        $autoRemark         = $rowAdat["REMARK"];
        $requestReason      = $rowAdat["REQUEST_REASON"];
        $requestStaff       = $rowAdat["REQUEST_STAFF"];
        $collectLcd         = $rowAdat["COLLECT_L_CD"];
        $collectMcd         = $rowAdat["COLLECT_M_CD"];
        $collectScd         = $rowAdat["COLLECT_S_CD"];
        $autoIncomeApproval = $rowAdat["AUTO_INCOME_APPROVAL"];


        // 最大伝票番号取得
        $getMaxRequestNo = $db->getOne(knjp746aQuery::getMaxRequestNo($model));
        //対象年度の伝票番号がない場合
        if ($getMaxRequestNo == '') {
            $setRequestNo = $model->year.'A00001';
        } else {
            $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
            $setRequestNo = $model->year.'A'.$setNo;
        }

        // 更新対象者取得(LEVY_REQUEST_INCOME_AUTO_DAT)
        $schCnt = 0;
        $query = knjp746aQuery::getPaidMDatQuery($model, "MAIN", $autoNo, $collectLcd, $collectMcd, $collectScd);
        $result = $db->query($query);
        $lineNoGrpData = array();
        while ($rowSch = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // LEVY_REQUEST_INCOME_SCHREG_DAT
            $data = array();
            $data["SCHOOLCD"][TEXT]         = $schoolCd;
            $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data["YEAR"][TEXT]             = $year;
            $data["INCOME_L_CD"][TEXT]      = $incomeLcd;
            $data["INCOME_M_CD"][TEXT]      = $incomeMcd;
            $data["REQUEST_NO"][TEXT]       = $setRequestNo;
            $data["INCOME_S_CD"][TEXT]      = sprintf("%03d", $rowSch["LINE_NO"]);//LINE_NOと同一のものをいれておく
            $data["SCHREGNO"][TEXT]         = $rowSch["SCHREGNO"];
            $data["LINE_NO"][NUMBER]        = $rowSch["LINE_NO"];
            $data["INCOME_MONEY"][NUMBER]   = $rowSch["INCOME_MONEY"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_SCHREG_DAT");
            $db->query($query);

            $lineNoGrpData[$rowSch["LINE_NO"]]["SCH_CNT"]++;
            $lineNoGrpData[$rowSch["LINE_NO"]]["INCOME_MONEY"] = $rowSch["INCOME_MONEY"];

            $schCnt++;
        }

        //作成件数カウント(SCHREG単位)
        $model->totalCnt += $schCnt;

        //明細
        $incomeDatTotal = 0;
        foreach ($lineNoGrpData as $lineNo => $lineNoData) {
            // LEVY_REQUEST_INCOME_MEISAI_DAT
            $data2 = array();
            $data2["SCHOOLCD"][TEXT]             = $schoolCd;
            $data2["SCHOOL_KIND"][TEXT]          = $schoolKind;
            $data2["YEAR"][TEXT]                 = $year;
            $data2["INCOME_L_CD"][TEXT]          = $incomeLcd;
            $data2["INCOME_M_CD"][TEXT]          = $incomeMcd;
            $data2["REQUEST_NO"][TEXT]           = $setRequestNo;
            $data2["INCOME_S_CD"][TEXT]          = sprintf("%03d", $lineNo);//LINE_NOと同一のものをいれておく
            $data2["COMMODITY_NAME"][TEXT]       = $autoName;
            $data2["LINE_NO"][NUMBER]            = $lineNo;
            $data2["COMMODITY_PRICE"][NUMBER]    = $lineNoData["INCOME_MONEY"];
            $data2["COMMODITY_CNT"][NUMBER]      = $lineNoData["SCH_CNT"];
            $data2["TOTAL_PRICE"][NUMBER]        = $lineNoData["INCOME_MONEY"] * $lineNoData["SCH_CNT"];
            $data2["WARIHURI_DIV"][TEXT]         = '1'; // 1;生徒 2:生徒以外
            $data2["REMARK"][TEXT]               = $autoRemark;
                
            $data2["REGISTERCD"][TEXT]           = STAFFCD;
            $data2["UPDATED"][NUMBER]            = "sysdate()";
                
            $query = Query::insertSQL($data2, "LEVY_REQUEST_INCOME_MEISAI_DAT");
            $db->query($query);

            $incomeDatTotal += $lineNoData["INCOME_MONEY"] * $lineNoData["SCH_CNT"];
        }

        if (count($lineNoGrpData) > 0) {
            // LEVY_REQUEST_INCOME_DAT
            $data3 = array();
            $data3["SCHOOLCD"][TEXT]         = $schoolCd;
            $data3["SCHOOL_KIND"][TEXT]      = $schoolKind;
            $data3["YEAR"][TEXT]             = $year;
            $data3["INCOME_L_CD"][TEXT]      = $incomeLcd;
            $data3["INCOME_M_CD"][TEXT]      = $incomeMcd;
            $data3["REQUEST_NO"][TEXT]       = $setRequestNo;
            $data3["REQUEST_DATE"][DATE]     = $paidDate;
            $data3["REQUEST_REASON"][TEXT]   = $requestReason;
            $data3["REQUEST_STAFF"][TEXT]    = $requestStaff;
            $data3["REQUEST_GK"][NUMBER]     = $incomeDatTotal;
            $data3["COLLECT_DIV"][TEXT]      = "1";
            $data3["COLLECT_L_CD"][TEXT]     = $collectLcd;
            $data3["COLLECT_M_CD"][TEXT]     = $collectMcd;
            $data3["COLLECT_S_CD"][TEXT]     = $collectScd;
            if ($autoIncomeApproval == "1") {
                $data3["INCOME_APPROVAL"][TEXT]  = '1'; // 決済済みにする
                $data3["INCOME_DATE"][TEXT]      = $paidDate ; //収入決済日
                $data3["INCOME_NO"][TEXT]        = $setRequestNo ;
                $data3["INCOME_STAFF"][TEXT]     = $requestStaff ;
            } else {
                $data3["INCOME_APPROVAL"][TEXT]  = '';
                $data3["INCOME_DATE"][TEXT]      = '';
            }

            $data3["REGISTERCD"][TEXT]       = STAFFCD;
            $data3["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data3, "LEVY_REQUEST_INCOME_DAT");
            $db->query($query);

            $model->totalIncome += $incomeDatTotal;
        }
    }
}
