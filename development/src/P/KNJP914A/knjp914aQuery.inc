<?php

require_once('for_php7.php');

class knjp914aQuery extends Query
{

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

    //振替項目取得
    public function getIncomeLMcd($model, $selectLMcd='')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     I_SCH.INCOME_L_CD || I_SCH.INCOME_M_CD AS VALUE, ";
        $query .= "     I_SCH.INCOME_L_CD || I_SCH.INCOME_M_CD || ':' || value(M_MST.LEVY_M_NAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_REQUEST_INCOME_SCHREG_DAT I_SCH ";
        $query .= "     LEFT JOIN LEVY_REQUEST_INCOME_DAT I_DAT ON I_DAT.SCHOOLCD    = I_SCH.SCHOOLCD ";
        $query .= "                                            AND I_DAT.SCHOOL_KIND = I_SCH.SCHOOL_KIND ";
        $query .= "                                            AND I_DAT.YEAR        = I_SCH.YEAR ";
        $query .= "                                            AND I_DAT.INCOME_L_CD = I_SCH.INCOME_L_CD ";
        $query .= "                                            AND I_DAT.INCOME_M_CD = I_SCH.INCOME_M_CD ";
        $query .= "                                            AND I_DAT.REQUEST_NO  = I_SCH.REQUEST_NO ";
        $query .= "     LEFT JOIN LEVY_M_MST M_MST ON M_MST.SCHOOLCD    = I_SCH.SCHOOLCD ";
        $query .= "                               AND M_MST.SCHOOL_KIND = I_SCH.SCHOOL_KIND ";
        $query .= "                               AND M_MST.YEAR        = I_SCH.YEAR ";
        $query .= "                               AND M_MST.LEVY_L_CD   = I_SCH.INCOME_L_CD ";
        $query .= "                               AND M_MST.LEVY_M_CD   = I_SCH.INCOME_M_CD ";
        $query .= " WHERE ";
        $query .= "         I_SCH.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND I_SCH.SCHOOL_KIND     = '{$model->schoolKind}' ";
        $query .= "     AND I_SCH.YEAR            = '{$model->year}' ";
        $query .= "     AND M_MST.LEVY_IN_OUT_DIV = '1' "; //収入項目
        $query .= "     AND I_DAT.INCOME_APPROVAL = '1' ";
        $query .= "     AND I_DAT.INCOME_CANCEL   is null ";
        $query .= "     AND I_DAT.INCOME_L_CD not in ('99') ";
        if ($selectLMcd != '') {
            $query .= "     AND I_DAT.INCOME_L_CD || I_SCH.INCOME_M_CD <> '{$selectLMcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //支出科目
    public function getLevyMDiv($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD AS VALUE, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || ':' || T1.LEVY_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_M_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND     = '{$model->schoolKind}' ";
        $query .= "     AND T1.YEAR            = '{$model->year}' ";
        $query .= "     AND T1.ZATU_FLG IS NULL  ";
        $query .= "     AND T1.YOBI_FLG IS NULL  ";
        $query .= "     AND T1.KURIKOSI_FLG IS NULL  ";
        $query .= "     AND T1.LEVY_IN_OUT_DIV = '2' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //支出細目
    public function getLevySDiv($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.LEVY_S_NAME AS NAME, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD || ':' || T1.LEVY_S_NAME AS LABEL, ";
        $query .= "     T1.LEVY_L_CD || T1.LEVY_M_CD || T1.LEVY_S_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_S_MST T1 ";
        $query .= "     INNER JOIN LEVY_M_MST L1 ON L1.SCHOOLCD    = T1.SCHOOLCD ";
        $query .= "                             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.YEAR        = T1.YEAR ";
        $query .= "                             AND L1.LEVY_L_CD   = T1.LEVY_L_CD ";
        $query .= "                             AND L1.LEVY_M_CD   = T1.LEVY_M_CD ";
        $query .= " WHERE ";
        $query .= "          T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "      AND T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "      AND T1.YEAR        = '{$model->year}' ";
        $query .= "      AND T1.LEVY_L_CD || T1.LEVY_M_CD = '{$model->outgoLMcdMoto}' ";
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

        $motoCd = $model->incomeLMcdMoto;
        $sakiCd = $model->incomeLMcdSaki;

        // 振替元項目の科目、項目コード
        $incomeLcd_moto = substr($motoCd, 0, 2);
        $incomeMcd_moto = substr($motoCd, 2, 2);

        //振替元項目に支出伝票を作成する際にセットするコードを取得
        $setOutLCd = substr($model->outgoLMScdMoto, 0, 2);
        $setOutMCd = substr($model->outgoLMScdMoto, 2, 2);
        $setOutSCd = substr($model->outgoLMScdMoto, 4, 3);

        // 振替先項目の科目、項目コード
        $incomeLcd_saki = substr($sakiCd, 0, 2);
        $incomeMcd_saki = substr($sakiCd, 2, 2);

        // 最大伝票番号(収入)取得
        $getMaxRequestNoIn = $db->getOne(knjp914aQuery::getMaxRequestNo($model, "LEVY_REQUEST_INCOME_DAT"));
        $setNoIncome = sprintf("%05d", intval(substr($getMaxRequestNoIn, 5, 5)) + 1);
        $setIncomeRequestNo = $model->year.'A'.$setNoIncome;

        // 最大伝票番号(支出)取得
        $getMaxRequestNoOut = $db->getOne(knjp914aQuery::getMaxRequestNo($model, "LEVY_REQUEST_OUTGO_DAT"));
        $setNoOutgo = sprintf("%05d", intval(substr($getMaxRequestNoOut, 5, 5)) + 1);
        $setOutgoRequestNo = $model->year.'B'.$setNoOutgo;

        // 対象生徒
        $schCnt = 0;
        $schregArr = explode(',', $model->selectStudent);
        foreach ($schregArr as $schregNo) {

            // 振替元項目に支出伝票を作成 --LEVY_REQUEST_OUTGO_SCHREG_DAT--
            $data = array();
            $data["SCHOOLCD"][TEXT]         = (sprintf("%012d", SCHOOLCD));
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
            $data["YEAR"][TEXT]             = $model->year;
            $data["REQUEST_NO"][TEXT]       = $setOutgoRequestNo;
            $data["SCHREGNO"][TEXT]         = $schregNo;
            $data["LINE_NO"][NUMBER]        = 1;
            $data["OUTGO_L_CD"][TEXT]       = $setOutLCd;
            $data["OUTGO_M_CD"][TEXT]       = $setOutMCd;
            $data["OUTGO_S_CD"][TEXT]       = $setOutSCd;
            $data["OUTGO_MONEY"][NUMBER]    = $model->setMoney;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "LEVY_REQUEST_OUTGO_SCHREG_DAT");
            $db->query($query);

            // 振替先項目に収入伝票を作成 --LEVY_REQUEST_INCOME_SCHREG_DAT--
            $data = array();
            $data["SCHOOLCD"][TEXT]         = (sprintf("%012d", SCHOOLCD));
            $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
            $data["YEAR"][TEXT]             = $model->year;
            $data["INCOME_L_CD"][TEXT]      = $incomeLcd_saki;
            $data["INCOME_M_CD"][TEXT]      = $incomeMcd_saki;
            $data["REQUEST_NO"][TEXT]       = $setIncomeRequestNo;
            $data["INCOME_S_CD"][TEXT]      = $setOutSCd;
            $data["SCHREGNO"][TEXT]         = $schregNo;
            $data["LINE_NO"][NUMBER]        = 1;
            $data["INCOME_MONEY"][NUMBER]   = $model->setMoney;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "LEVY_REQUEST_INCOME_SCHREG_DAT");
            $db->query($query);

            $schCnt++;
        }

        // 振替元項目に支出伝票、振替先項目に収入伝票を作成
        if ($schCnt > 0) {
            // LEVY_REQUEST_OUTGO_MEISAI_DAT
            $data2 = array();
            $data2["SCHOOLCD"][TEXT]              = (sprintf("%012d", SCHOOLCD));
            $data2["SCHOOL_KIND"][TEXT]           = $model->schoolKind;
            $data2["YEAR"][TEXT]                  = $model->year;
            $data2["REQUEST_NO"][TEXT]            = $setOutgoRequestNo;
            $data2["LINE_NO"][NUMBER]             = 1;
            $data2["OUTGO_L_CD"][TEXT]            = $setOutLCd;
            $data2["OUTGO_M_CD"][TEXT]            = $setOutMCd;
            $data2["OUTGO_S_CD"][TEXT]            = $setOutSCd;
            $data2["COMMODITY_PRICE"][NUMBER]     = $model->setMoney;
            $data2["COMMODITY_CNT"][NUMBER]       = $schCnt;
            $data2["TOTAL_PRICE_ZEINUKI"][NUMBER] = $model->setMoney * $schCnt;
            $data2["TOTAL_TAX"][NUMBER]           = 0;
            $data2["TOTAL_PRICE"][NUMBER]         = $model->setMoney * $schCnt;
            $data2["SCH_PRICE"][NUMBER]           = $model->setMoney;
            $data2["SCH_CNT"][NUMBER]             = $schCnt;
            $data2["HASUU"][NUMBER]               = 0;
            $data2["WARIHURI_DIV"][TEXT]          = '1'; // 1;生徒 2:生徒以外
            $data2["REGISTERCD"][TEXT]            = STAFFCD;
            $data2["UPDATED"][NUMBER]             = "sysdate()";

            $query = Query::insertSQL($data2, "LEVY_REQUEST_OUTGO_MEISAI_DAT");
            $db->query($query);

            // LEVY_REQUEST_OUTGO_DAT
            $data3 = array();
            $data3["SCHOOLCD"][TEXT]            = (sprintf("%012d", SCHOOLCD));
            $data3["SCHOOL_KIND"][TEXT]         = $model->schoolKind;
            $data3["YEAR"][TEXT]                = $model->year;
            $data3["OUTGO_L_CD"][TEXT]          = $setOutLCd;
            $data3["OUTGO_M_CD"][TEXT]          = $setOutMCd;
            $data3["REQUEST_NO"][TEXT]          = $setOutgoRequestNo;
            $data3["REQUEST_DATE"][DATE]        = CTRL_DATE;
            $data3["REQUEST_REASON"][TEXT]      = $model->requestReason;
            $data3["REQUEST_STAFF"][TEXT]       = STAFFCD;
            $data3["INCOME_L_CD"][TEXT]         = $incomeLcd_moto;
            $data3["INCOME_M_CD"][TEXT]         = $incomeMcd_moto;
            $data3["REQUEST_GK"][NUMBER]        = $model->setMoney * $schCnt;
            $data3["OUTGO_APPROVAL"][TEXT]      = '1'; // 決済済みにする
            $data3["OUTGO_DATE"][TEXT]          = CTRL_DATE;
            $data3["REGISTERCD"][TEXT]          = STAFFCD;
            $data3["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data3, "LEVY_REQUEST_OUTGO_DAT");
            $db->query($query);

            // LEVY_REQUEST_INCOME_MEISAI_DAT
            $data2 = array();
            $data2["SCHOOLCD"][TEXT]            = (sprintf("%012d", SCHOOLCD));
            $data2["SCHOOL_KIND"][TEXT]         = $model->schoolKind;
            $data2["YEAR"][TEXT]                = $model->year;
            $data2["INCOME_L_CD"][TEXT]         = $incomeLcd_saki;
            $data2["INCOME_M_CD"][TEXT]         = $incomeMcd_saki;
            $data2["REQUEST_NO"][TEXT]          = $setIncomeRequestNo;
            $data2["INCOME_S_CD"][TEXT]         = $setOutSCd;
            $data2["LINE_NO"][NUMBER]           = 1;
            $data2["COMMODITY_NAME"][TEXT]      = $model->requestReason;
            $data2["COMMODITY_PRICE"][NUMBER]   = $model->setMoney;
            $data2["COMMODITY_CNT"][NUMBER]     = $schCnt;
            $data2["TOTAL_PRICE"][NUMBER]       = $model->setMoney * $schCnt;
            $data2["WARIHURI_DIV"][TEXT]        = '1'; // 1;生徒 2:生徒以外
            $data2["REGISTERCD"][TEXT]          = STAFFCD;
            $data2["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data2, "LEVY_REQUEST_INCOME_MEISAI_DAT");
            $db->query($query);

            // LEVY_REQUEST_INCOME_DAT
            $data3 = array();
            $data3["SCHOOLCD"][TEXT]            = (sprintf("%012d", SCHOOLCD));
            $data3["SCHOOL_KIND"][TEXT]         = $model->schoolKind;
            $data3["YEAR"][TEXT]                = $model->year;
            $data3["INCOME_L_CD"][TEXT]         = $incomeLcd_saki;
            $data3["INCOME_M_CD"][TEXT]         = $incomeMcd_saki;
            $data3["REQUEST_NO"][TEXT]          = $setIncomeRequestNo;
            $data3["REQUEST_DATE"][DATE]        = CTRL_DATE;
            $data3["REQUEST_REASON"][TEXT]      = $model->requestReason;
            $data3["REQUEST_STAFF"][TEXT]       = STAFFCD;
            $data3["REQUEST_GK"][NUMBER]        = $model->setMoney * $schCnt;
            $data3["COLLECT_DIV"][TEXT]         = '3';
            $data3["COLLECT_L_CD"][TEXT]        = substr($model->collectLMcd, 0, 2);
            $data3["COLLECT_M_CD"][TEXT]        = substr($model->collectLMcd, 2, 2);
            $data3["COLLECT_S_CD"][TEXT]        = "000";
            $data3["INCOME_APPROVAL"][TEXT]     = '1'; // 決済済みにする
            $data3["INCOME_DATE"][TEXT]         = CTRL_DATE;
            $data3["INCOME_NO"][TEXT]           = $setIncomeRequestNo;
            $data3["INCOME_STAFF"][TEXT]        = STAFFCD;
            $data3["REGISTERCD"][TEXT]          = STAFFCD;
            $data3["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data3, "LEVY_REQUEST_INCOME_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    // 対象生徒
    public function getTergetSchreg($model, $list='')
    {
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
        if ($model->taigaku == '1') {
            $query .= " AND value(BASE.GRD_DIV, '') NOT IN ('2', '3', '6', '7') ";
        }
        // 収入伝票（元）
        $query .= " ), INCOME_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         ISCH.SCHREGNO, ";
        $query .= "         sum(value(ISCH.INCOME_MONEY, 0)) AS TOTAL_INCOME_MONEY ";
        $query .= "     FROM ";
        $query .= "         LEVY_REQUEST_INCOME_SCHREG_DAT ISCH ";
        $query .= "         LEFT JOIN LEVY_REQUEST_INCOME_DAT INCO ON INCO.SCHOOLCD    = ISCH.SCHOOLCD ";
        $query .= "                                               AND INCO.SCHOOL_KIND = ISCH.SCHOOL_KIND ";
        $query .= "                                               AND INCO.YEAR        = ISCH.YEAR ";
        $query .= "                                               AND INCO.INCOME_L_CD = ISCH.INCOME_L_CD ";
        $query .= "                                               AND INCO.INCOME_M_CD = ISCH.INCOME_M_CD ";
        $query .= "                                               AND INCO.REQUEST_NO  = ISCH.REQUEST_NO ";
        $query .= "     WHERE ";
        $query .= "             ISCH.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND ISCH.SCHOOL_KIND     = '{$model->schoolKind}' ";
        $query .= "         AND ISCH.YEAR            = '{$model->year}' ";
        $query .= "         AND ISCH.INCOME_L_CD || ISCH.INCOME_M_CD = '{$model->incomeLMcdMoto}' ";
        $query .= "         AND INCO.INCOME_APPROVAL = '1' ";
        $query .= "         AND INCO.INCOME_CANCEL   is null ";
        $query .= "     GROUP BY ";
        $query .= "         ISCH.SCHREGNO ";
        $query .= " ) ";
        // メイン
        $query .= " SELECT ";
        if ($list != '') {
            $query .= "     REGD.GRADE_HR_CLASS || REGD.ATTENDNO || '-' || INCOME.SCHREGNO as VALUE, ";
            $query .= "     REGD.HR_NAME || REGD.ATTENDNO || '番　' || REGD.NAME as LABEL, ";
            $query .= "     INCOME.SCHREGNO ";
        } else {
            $query .= "     INCOME.SCHREGNO ";
        }
        $query .= " FROM ";
        $query .= "     INCOME_DATA INCOME ";
        $query .= "     INNER JOIN REGD_DATA REGD ON REGD.SCHREGNO = INCOME.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     INCOME.TOTAL_INCOME_MONEY >= {$model->setMoney} ";
        $query .= " ORDER BY ";
        if ($list != '') {
            $query .= "     REGD.GRADE_HR_CLASS || REGD.ATTENDNO ";
        } else {
            $query .= "     INCOME.SCHREGNO ";
        }

        return $query;
    }

    // MAX伝票番号取得
    public function getMaxRequestNo($model, $table)
    {
        $query  = " SELECT ";
        $query .= "     MAX(REQUEST_NO) AS NO ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND YEAR        = '{$model->year}' ";

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
}
