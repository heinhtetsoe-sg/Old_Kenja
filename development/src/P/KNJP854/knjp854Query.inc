<?php

require_once('for_php7.php');

class knjp854Query extends Query
{
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($namecd1 == 'P004') {
            $query .= "     NAME1 AS LABEL ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd1 == 'P004') {
            $query .= "     AND NAMECD2 <> '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタP012取得(グループコード採番時の番号帯)
    public function getNameMstP012($model)
    {
        $query  = "  SELECT ";
        $query .= "      NAME2, "; //from
        $query .= "      NAME3 ";  //to
        $query .= "  FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR           = '{$model->year}' ";
        $query .= "      AND NAMECD1    = 'P012' ";
        $query .= "      AND NAMESPARE1 = 'KNJP854' ";

        return $query;
    }

    //生徒氏名取得
    public function getSchregNameQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    //生徒校種取得
    public function getSchregSchKindQuery($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "          ON T2.YEAR  = T1.YEAR ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.YEAR = '{$model->year}' ";

        return $query;
    }

    public function selectMainQuery($model, $div = "")
    {
        $query .= " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "          PLAN_M.*, ";
        $query .= "          CASE ";
        $query .= "              WHEN VALUE(PLAN_M.PAID_MONEY, 0) > 0 ";
        $query .= "                  THEN VALUE(PLAN_M.PLAN_MONEY, 0) ";
        $query .= "              ELSE ";
        $query .= "                  VALUE(PLAN_M.PLAN_MONEY, 0) - ";
        $query .= "                  VALUE(CASE WHEN MMST.REDUCTION_DIV = '1' AND REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0) - ";
        $query .= "                  VALUE(CASE WHEN MMST.REDUCTION_DIV = '1' AND REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0) - ";
        $query .= "                  VALUE(CASE WHEN REDUC_D.OFFSET_FLG = '1' THEN REDUC_D.DECISION_MONEY ELSE 0 END, 0) - ";
        $query .= "                  VALUE(BURDEN.BURDEN_CHARGE, 0) - ";
        $query .= "                  VALUE(CASE WHEN SCHOOL_P.OFFSET_FLG = '1' THEN SCHOOL_P.DECISION_MONEY ELSE 0 END, 0) ";
        $query .= "          END AS REDUCED_PLAN_MONEY, ";
        $query .= "          CASE ";
        $query .= "              WHEN VALUE(PLAN_M.PAID_MONEY, 0) > 0 ";
        $query .= "                  THEN 0 ";
        $query .= "              ELSE ";
        $query .= "                  VALUE(CASE WHEN MMST.REDUCTION_DIV = '1' AND REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0) + ";
        $query .= "                  VALUE(CASE WHEN MMST.REDUCTION_DIV = '1' AND REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0) + ";
        $query .= "                  VALUE(CASE WHEN REDUC_D.OFFSET_FLG = '1' THEN REDUC_D.DECISION_MONEY ELSE 0 END, 0) + ";
        $query .= "                  VALUE(BURDEN.BURDEN_CHARGE, 0) + ";
        $query .= "                  VALUE(CASE WHEN SCHOOL_P.OFFSET_FLG = '1' THEN SCHOOL_P.DECISION_MONEY ELSE 0 END, 0) ";
        $query .= "          END AS REDUCE_MONEY, ";
        $query .= "          SL_D.COLLECT_GRP_CD, ";
        $query .= "          LIMIT_D.PAID_LIMIT_MONTH, ";
        $query .= "          LIMIT_D.PAID_LIMIT_DATE ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_M_DAT PLAN_M ";
        $query .= "         LEFT JOIN COLLECT_SLIP_DAT SL_D ";
        $query .= "             ON SL_D.SCHOOLCD     = PLAN_M.SCHOOLCD ";
        $query .= "             AND SL_D.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ";
        $query .= "             AND SL_D.YEAR        = PLAN_M.YEAR ";
        $query .= "             AND SL_D.SLIP_NO     = PLAN_M.SLIP_NO ";
        $query .= "             AND SL_D.CANCEL_DATE IS NULL ";
        $query .= "         LEFT JOIN COLLECT_M_MST MMST ";
        $query .= "             ON MMST.SCHOOLCD        = PLAN_M.SCHOOLCD ";
        $query .= "             AND MMST.SCHOOL_KIND    = PLAN_M.SCHOOL_KIND ";
        $query .= "             AND MMST.YEAR           = PLAN_M.YEAR ";
        $query .= "             AND MMST.COLLECT_L_CD   = PLAN_M.COLLECT_L_CD ";
        $query .= "             AND MMST.COLLECT_M_CD   = PLAN_M.COLLECT_M_CD ";
        $query .= "         LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_D ";
        $query .= "             ON PLAN_M.SCHOOLCD      = LIMIT_D.SCHOOLCD ";
        $query .= "             AND PLAN_M.SCHOOL_KIND  = LIMIT_D.SCHOOL_KIND ";
        $query .= "             AND PLAN_M.YEAR         = LIMIT_D.YEAR ";
        $query .= "             AND PLAN_M.SCHREGNO     = LIMIT_D.SCHREGNO ";
        $query .= "             AND PLAN_M.SLIP_NO      = LIMIT_D.SLIP_NO ";
        $query .= "             AND PLAN_M.PLAN_YEAR    = LIMIT_D.PLAN_YEAR ";
        $query .= "             AND PLAN_M.PLAN_MONTH   = LIMIT_D.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_C ";
        $query .= "             ON REDUC_C.SCHOOLCD     = PLAN_M.SCHOOLCD ";
        $query .= "             AND REDUC_C.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ";
        $query .= "             AND REDUC_C.YEAR        = PLAN_M.YEAR ";
        $query .= "             AND REDUC_C.SCHREGNO    = PLAN_M.SCHREGNO ";
        $query .= "             AND REDUC_C.SLIP_NO     = PLAN_M.SLIP_NO ";
        $query .= "             AND REDUC_C.PLAN_YEAR   = PLAN_M.PLAN_YEAR ";
        $query .= "             AND REDUC_C.PLAN_MONTH  = PLAN_M.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_PLAN_DAT REDUC_D ";
        $query .= "             ON REDUC_D.SCHOOLCD     = PLAN_M.SCHOOLCD ";
        $query .= "             AND REDUC_D.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ";
        $query .= "             AND REDUC_D.YEAR        = PLAN_M.YEAR ";
        $query .= "             AND REDUC_D.REDUCTION_TARGET = MMST.GAKUNOKIN_DIV ";
        $query .= "             AND REDUC_D.SCHREGNO    = PLAN_M.SCHREGNO ";
        $query .= "             AND REDUC_D.SLIP_NO     = PLAN_M.SLIP_NO ";
        $query .= "             AND REDUC_D.PLAN_YEAR   = PLAN_M.PLAN_YEAR ";
        $query .= "             AND REDUC_D.PLAN_MONTH  = PLAN_M.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN ";
        $query .= "             ON BURDEN.SCHOOLCD     = PLAN_M.SCHOOLCD ";
        $query .= "             AND BURDEN.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ";
        $query .= "             AND BURDEN.YEAR        = PLAN_M.YEAR ";
        $query .= "             AND BURDEN.REDUCTION_TARGET =  MMST.GAKUNOKIN_DIV ";
        $query .= "             AND BURDEN.SCHREGNO    = PLAN_M.SCHREGNO ";
        $query .= "             AND BURDEN.SLIP_NO     = PLAN_M.SLIP_NO ";
        $query .= "             AND BURDEN.PLAN_YEAR   = PLAN_M.PLAN_YEAR ";
        $query .= "             AND BURDEN.PLAN_MONTH  = PLAN_M.PLAN_MONTH ";
        $query .= "         LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_P ";
        $query .= "             ON SCHOOL_P.SCHOOLCD     = PLAN_M.SCHOOLCD ";
        $query .= "             AND SCHOOL_P.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ";
        $query .= "             AND SCHOOL_P.YEAR        = PLAN_M.YEAR ";
        $query .= "             AND SCHOOL_P.REDUCTION_TARGET =  MMST.GAKUNOKIN_DIV ";
        $query .= "             AND SCHOOL_P.SCHREGNO    = PLAN_M.SCHREGNO ";
        $query .= "             AND SCHOOL_P.SLIP_NO     = PLAN_M.SLIP_NO ";
        $query .= "             AND SCHOOL_P.PLAN_YEAR   = PLAN_M.PLAN_YEAR ";
        $query .= "             AND SCHOOL_P.PLAN_MONTH  = PLAN_M.PLAN_MONTH ";
        $query .= "     WHERE ";
        $query .= "         PLAN_M.SCHOOLCD        = '{$model->schoolCd}' ";
        $query .= "         AND PLAN_M.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "         AND PLAN_M.YEAR        = '{$model->year}' ";
        $query .= "         AND PLAN_M.SCHREGNO    = '{$model->schregno}' ";
        $query .= "         AND value(PLAN_M.PLAN_MONEY, 0) - value(PLAN_M.PAID_MONEY, 0) > 0 ";
        $query .= " ) ";

        if ($div == "MONTH") {
            //納期限月コンボ用
            $query .= " SELECT DISTINCT ";
            $query .= "      MAIN.PLAN_MONTH AS LABEL, ";
            $query .= "      MAIN.PLAN_YEAR || '-' || MAIN.PLAN_MONTH AS VALUE ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= " WHERE ";
            $query .= "     REDUCED_PLAN_MONEY > 0 ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } elseif ($div == "SLIP") {
            //伝票番号コンボ用
            $query .= " SELECT DISTINCT ";
            $query .= "     SLIP_NO || '：' || VALUE(GRP_M.COLLECT_GRP_NAME, '') AS LABEL, ";
            $query .= "     SLIP_NO AS VALUE ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= "     LEFT JOIN COLLECT_GRP_MST GRP_M  ON MAIN.SCHOOLCD        = GRP_M.SCHOOLCD ";
            $query .= "                                     AND MAIN.SCHOOL_KIND     = GRP_M.SCHOOL_KIND ";
            $query .= "                                     AND MAIN.YEAR            = GRP_M.YEAR ";
            $query .= "                                     AND MAIN.COLLECT_GRP_CD  = GRP_M.COLLECT_GRP_CD ";
            $query .= " WHERE ";
            $query .= "     MAIN.REDUCED_PLAN_MONEY > 0 ";
            $query .= "     AND MAIN.PLAN_YEAR || '-' || MAIN.PLAN_MONTH = '{$model->field["PLAN_YEAR_MONTH"]}' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            //明細行用
            $query .= " SELECT ";
            $query .= "     MAIN.COLLECT_L_CD, ";
            $query .= "     MAIN.COLLECT_M_CD, ";
            $query .= "     MAIN.PLAN_YEAR, ";
            $query .= "     MAIN.PLAN_MONTH, ";
            $query .= "     MMST2.COLLECT_M_NAME, ";
            $query .= "     MAIN.PLAN_MONEY, ";
            $query .= "     MAIN.REDUCED_PLAN_MONEY, ";
            $query .= "     MAIN.REDUCE_MONEY ";
            $query .= " FROM ";
            $query .= "     MAIN ";
            $query .= "     LEFT JOIN COLLECT_M_MST MMST2 ";
            $query .= "          ON MMST2.SCHOOLCD = MAIN.SCHOOLCD ";
            $query .= "         AND MMST2.SCHOOL_KIND = MAIN.SCHOOL_KIND ";
            $query .= "         AND MMST2.YEAR = MAIN.YEAR ";
            $query .= "         AND MMST2.COLLECT_L_CD = MAIN.COLLECT_L_CD ";
            $query .= "         AND MMST2.COLLECT_M_CD = MAIN.COLLECT_M_CD ";
            $query .= " WHERE ";
            $query .= "     MAIN.PLAN_YEAR || '-' || MAIN.PLAN_MONTH = '{$model->field["PLAN_YEAR_MONTH"]}' ";
            $query .= "     AND MAIN.SLIP_NO = '{$model->field["SLIP_NO"]}' ";
            $query .= " ORDER BY ";
            $query .= "     MAIN.PAID_LIMIT_DATE, ";
            $query .= "     MAIN.COLLECT_L_CD, ";
            $query .= "     MAIN.COLLECT_M_CD ";
        }

        return $query;
    }

    //一行取得(COLLECT_SLIP_DAT)
    public function getSlipDatRowQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "      * ";
        $query .= "  FROM ";
        $query .= "      COLLECT_SLIP_DAT ";
        $query .= "  WHERE ";
        $query .= "       SCHOOLCD     = '{$model->schoolCd}' ";
        $query .= "   AND SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "   AND YEAR         = '{$model->year}' ";
        $query .= "   AND SLIP_NO      = '{$model->field["SLIP_NO"]}' ";

        return $query;
    }

    //更新
    public function getUpdateQuery($db, $model)
    {
        list($planYear, $planMonth) = explode("-", $model->field["PLAN_YEAR_MONTH"]);

        //入金完了処理 (徴収予定額を一部入金額と同額に変更し、元伝票は入金完了とする)
        $updFlg = false;
        foreach ($model->field2 as $rowNo => $rowData) {
            list($lCd, $mCd) = explode("-", $rowData["ROW_KEY"]);

            //元伝票の入金処理
            knjp854Query::updateMotoSlipQuery($db, $model, $lCd, $mCd, $planYear, $planMonth, $rowData["PAID_MONEY"] + $rowData["REDUCE_MONEY"]);

            $updFlg = true;
        }

        //徴収予定額が0円の項目も支払い済みにする
        foreach ($model->field3 as $rowNo => $noDispRowData) {
            list($lCd, $mCd) = explode("-", $noDispRowData["NO_DISP_ROW_KEY"]);

            //元伝票の入金処理
            knjp854Query::updateMotoSlipQuery($db, $model, $lCd, $mCd, $planYear, $planMonth, $noDispRowData["NO_DISP_PLAN_MONEY"]);
        }

        //元伝票への更新処理が行なわれなかった場合は以降の処理を行わない。
        if (!$updFlg) {
            return false;
        }

        //COLLECT_SLIP_PLAN_PAID_DAT
        $maxSeq = $db->getOne(knjp854Query::getMaxSeqPaidDat($model, $planYear, $planMonth));
        $data = array();
        $data["SCHOOLCD"][TEXT]             = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SLIP_NO"][TEXT]              = $model->field["SLIP_NO"];
        $data["PLAN_YEAR"][TEXT]            = $planYear;
        $data["PLAN_MONTH"][TEXT]           = $planMonth;
        $data["SEQ"][TEXT]                  = $maxSeq + 1;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["PLAN_PAID_MONEY_DATE"][DATE] = $model->field["PLAN_PAID_MONEY_DATE"];
        $data["PLAN_PAID_MONEY"][NUMBER]    = $model->totalRow["PAID_MONEY_TOTAL"];
        $data["PLAN_PAID_MONEY_DIV"][TEXT]  = $model->field["PLAN_PAID_MONEY_DIV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_DAT");
        $db->query($query);

        //元伝票情報を取得(下の新規伝票作成処理で使用)
        $query = knjp854Query::getSlipDatRowQuery($model);
        $slipDatRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //MAX伝票番号取得
        $slipNoArray = array();
        $query       = knjp854Query::getMaxSlipNo($model);
        $maxSlipNo   = $db->getOne($query);
        $nextSlipNo  = ($maxSlipNo != "") ? $maxSlipNo + 1 : $model->year.$model->schregno."001";

        //新規伝票作成処理 (一部入金によって消込が完了しなかった場合には新規伝票に振分け)
        $updFlg2 = false;
        foreach ($model->field2 as $rowNo => $rowData) {
            if ($rowData["BALANCE"] > 0) {
                list($lCd, $mCd) = explode("-", $rowData["ROW_KEY"]);

                //COLLECT_SLIP_M_DAT
                $data = array();
                $data["SCHOOLCD"][TEXT]         = $model->schoolCd;
                $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
                $data["YEAR"][TEXT]             = $model->year;
                $data["SLIP_NO"][TEXT]          = $nextSlipNo;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["COLLECT_L_CD"][TEXT]     = $lCd;
                $data["COLLECT_M_CD"][TEXT]     = $mCd;
                $data["COLLECT_MONEY"][NUMBER]  = $rowData["BALANCE"];
                $data["COLLECT_CNT"][NUMBER]    = 1;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";
                $query = Query::insertSQL($data, "COLLECT_SLIP_M_DAT");
                $db->query($query);

                //COLLECT_SLIP_PLAN_M_DAT
                for ($i = 4; $i <= 15; $i++) {
                    $updYear  = ($i > 12) ? $model->year + 1 : $model->year;
                    $updMonth = sprintf("%02d", ($i > 12) ? ($i - 12) : $i);

                    $updPaidMoney = ($planYear.$planMonth == $updYear.$updMonth) ? $rowData["BALANCE"] : 0; //※指定月以外は0円で計画金をセット

                    knjp854Query::insertPlanMDat($db, $model, $nextSlipNo, $lCd, $mCd, $updYear, $updMonth, $updPaidMoney);
                }

                $updFlg2 = true;
            }
        }

        if ($updFlg2) {
            //COLLECT_SLIP_DAT
            $data = array();
            $data["SCHOOLCD"][TEXT]           = $model->schoolCd;
            $data["SCHOOL_KIND"][TEXT]        = $model->schoolKind;
            $data["YEAR"][TEXT]               = $model->year;
            $data["SLIP_NO"][TEXT]            = $nextSlipNo;
            $data["SCHREGNO"][TEXT]           = $model->schregno;
            $data["SLIP_STAFFCD"][TEXT]       = $slipDatRow["SLIP_STAFFCD"];
            $data["PAY_DIV"][TEXT]            = $slipDatRow["PAY_DIV"];
            $data["SLIP_DATE"][DATE]          = $slipDatRow["SLIP_DATE"];
            $data["COLLECT_GRP_CD"][TEXT]     = $model->nextGrpCd;
            $data["COLLECT_PATTERN_CD"][TEXT] = $slipDatRow["COLLECT_PATTERN_CD"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_SLIP_DAT");
            $db->query($query);

            //入金グループ関連の登録
            knjp854Query::getUpdateCollectGrpQuery($db, $model);

            //納期限
            knjp854Query::updateLimitDateQuery($db, $model, $nextSlipNo, $slipDatRow["COLLECT_PATTERN_CD"]);
        }

        return $updFlg;
    }

    //入金グループ関連登録
    public function getUpdateCollectGrpQuery($db, $model)
    {
        //1.COLLECT_GRP_MST
        $query = knjp854Query::insertGrpMstQuery($model);
        $db->query($query);

        foreach ($model->field2 as $rowNo => $rowData) {
            //新規伝票に移動する項目を入金グループに追加
            if ($rowData["BALANCE"] > 0) {
                list($lCd, $mCd) = explode("-", $rowData["ROW_KEY"]);

                //2.COLLECT_GRP_DAT
                $query = knjp854Query::insertGrpDatQuery($model, $lCd, $mCd);
                $db->query($query);

                //3.COLLECT_MONTH_GRP_DAT
                $query = knjp854Query::insertMonthGrpDatQuery($model, $lCd, $mCd);
                $db->query($query);
            }
        }
    }

    //グループコードの最大値を取得
    public function getMaxCollectGrpCdQuery($model)
    {
        $query  = "  SELECT ";
        $query .= "      MAX(COLLECT_GRP_CD) ";
        $query .= "  FROM ";
        $query .= "      COLLECT_GRP_MST ";
        $query .= "  WHERE ";
        $query .= "          SCHOOLCD    = '{$model->schoolCd}' ";
        $query .= "      AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "      AND YEAR        = '{$model->year}' ";
        $query .= "      AND COLLECT_GRP_CD BETWEEN '{$model->grpCdFromNo}' AND '{$model->grpCdToNo}' ";

        return $query;
    }

    //MAX伝票番号
    public function getMaxSlipNo($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SLIP_NO) AS SLIP_NO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD       = '{$model->schoolCd}' AND ";
        $query .= "     SCHOOL_KIND    = '{$model->schoolKind}' AND ";
        $query .= "     YEAR           = '{$model->year}' AND ";
        $query .= "     SCHREGNO       = '{$model->schregno}' ";

        return $query;
    }

    //maxSEQ取得(COLLECT_SLIP_PLAN_PAID_M_DAT)
    public function getMaxSeqMdat($model, $lCd, $mCd, $planYear, $planMonth)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_M_DAT ";
        $query .= " WHERE ";
        $query .= "       SCHOOLCD     = '{$model->schoolCd}' ";
        $query .= "   AND SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "   AND YEAR         = '{$model->year}' ";
        $query .= "   AND SCHREGNO     = '{$model->schregno}' ";
        $query .= "   AND SLIP_NO      = '{$model->field["SLIP_NO"]}' ";
        $query .= "   AND COLLECT_L_CD = '{$lCd}' ";
        $query .= "   AND COLLECT_M_CD = '{$mCd}' ";
        $query .= "   AND PLAN_YEAR    = '{$planYear}' ";
        $query .= "   AND PLAN_MONTH   = '{$planMonth}' ";

        return $query;
    }

    //maxSEQ取得(COLLECT_SLIP_PLAN_PAID_DAT)
    public function getMaxSeqPaidDat($model, $planYear, $planMonth)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= " WHERE ";
        $query .= "       SCHOOLCD     = '{$model->schoolCd}' ";
        $query .= "   AND SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "   AND YEAR         = '{$model->year}' ";
        $query .= "   AND SCHREGNO     = '{$model->schregno}' ";
        $query .= "   AND SLIP_NO      = '{$model->field["SLIP_NO"]}' ";
        $query .= "   AND PLAN_YEAR    = '{$planYear}' ";
        $query .= "   AND PLAN_MONTH   = '{$planMonth}' ";

        return $query;
    }


    //追加 -- COLLECT_GRP_MST
    public function insertGrpMstQuery($model)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["COLLECT_GRP_CD"][TEXT]   = $model->nextGrpCd;
        $data["COLLECT_GRP_NAME"][TEXT] = $model->nextGrpCd;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_GRP_MST");
        return $query;
    }

    //追加 -- COLLECT_GRP_DAT
    public function insertGrpDatQuery($model, $lCd, $mCd)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["COLLECT_GRP_CD"][TEXT]   = $model->nextGrpCd;
        $data["COLLECT_L_CD"][TEXT]     = $lCd;
        $data["COLLECT_M_CD"][TEXT]     = $mCd;
        $data["COLLECT_S_CD"][TEXT]     = "00";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_GRP_DAT");
        return $query;
    }

    //追加 -- COLLECT_MONTH_GRP_DAT
    public function insertMonthGrpDatQuery($model, $lCd, $mCd)
    {
        list($year, $month) = explode("-", $model->field["PLAN_YEAR_MONTH"]);
        $monthVal = intval($month);

        $data = array();
        $data["SCHOOLCD"][TEXT]                 = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]              = $model->schoolKind;
        $data["YEAR"][TEXT]                     = $model->year;
        $data["COLLECT_GRP_CD"][TEXT]           = $model->nextGrpCd;
        $data["COLLECT_L_CD"][TEXT]             = $lCd;
        $data["COLLECT_M_CD"][TEXT]             = $mCd;
        $data["MONTH_CNT"][NUMBER]              = 1;
        $data["COLLECT_MONTH_".$monthVal][TEXT] = "1";
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_MONTH_GRP_DAT");
        return $query;
    }

    //追加 -- COLLECT_SLIP_PLAN_LIMITDATE_DAT
    public function updateLimitDateQuery($db, $model, $nextSlipNo, $patternCd)
    {
        //指定徴収月
        list($selectedYear, $selectedMonth) = explode("-", $model->field["PLAN_YEAR_MONTH"]);

        //元伝票の納期限情報取得
        $limitRow = $db->getRow(knjp854Query::getLimitDateDat($model), DB_FETCHMODE_ASSOC);

        //パターン情報取得
        $patternInfo = $db->getRow(knjp854Query::getCollectPattern($model, $patternCd), DB_FETCHMODE_ASSOC);
        //入金パターン引落月
        $directDebitDate = $patternInfo["DIRECT_DEBIT_DATE"];

        //銀行休業日取得
        $holidayArray = array();
        $result = $db->query(knjp854Query::getBankHolidayDat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $holidayArray[] = $row["EXECUTEDATE"];
        }
        $result->free();

        for ($i = 1; $i <= 12; $i++) {
            $paid_limit_date = $paid_limit_month = "";
            $targetYMFlg = false;

            $updYear  = ($i <= 3) ? $model->year + 1 : $model->year;
            $updMonth = sprintf("%02d", $i);

            //指定した年月だった場合フラグを立てる
            if ($selectedYear.$selectedMonth == $updYear.$updMonth) {
                $targetYMFlg = true;
            }

            $data = array();
            $data["SCHOOLCD"][TEXT]              = $model->schoolCd;
            $data["SCHOOL_KIND"][TEXT]           = $model->schoolKind;
            $data["YEAR"][TEXT]                  = $model->year;
            $data["SLIP_NO"][TEXT]               = $nextSlipNo;
            $data["SCHREGNO"][TEXT]              = $model->schregno;
            $data["PLAN_YEAR"][TEXT]             = $updYear;
            $data["PLAN_MONTH"][TEXT]            = $updMonth;
            $data["PAID_LIMIT_MONTH"][TEXT]      = ($targetYMFlg) ? $limitRow["PAID_LIMIT_MONTH"] : "";
            $data["PAID_LIMIT_DATE"][TEXT]       = ($targetYMFlg) ? $limitRow["PAID_LIMIT_DATE"]  : "";
            $data["PAID_LIMIT_MONTH_CALC"][TEXT] = ($targetYMFlg) ? $limitRow["PAID_LIMIT_MONTH_CALC"] : "";
            $data["PAID_LIMIT_DATE_CALC"][TEXT]  = ($targetYMFlg) ? $limitRow["PAID_LIMIT_DATE_CALC"]  : "";
            $data["REGISTERCD"][TEXT]            = STAFFCD;
            $data["UPDATED"][NUMBER]             = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_LIMITDATE_DAT");
            $db->query($query);
        }
    }

    //銀行休業日取得
    public function getBankHolidayDat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BANK_HOLIDAY_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '{$model->schoolCd}' AND ";
        $query .= "     SCHOOL_KIND = '{$model->schoolKind}' ";

        return $query;
    }

    //入金パターン取得
    public function getCollectPattern($model, $collect_pattern_cd = "")
    {
        $query  = " SELECT ";
        if ($collect_pattern_cd) {
            $query .= "     * ";
        } else {
            $query .= "     COLLECT_PATTERN_CD AS VALUE, ";
            $query .= "     COLLECT_PATTERN_CD || ' ' || COLLECT_PATTERN_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '{$model->schoolCd}' AND ";
        $query .= "     SCHOOL_KIND = '{$model->schoolKind}' AND ";
        $query .= "     YEAR        = '{$model->year}' ";
        if ($collect_pattern_cd) {
            $query .= " AND COLLECT_PATTERN_CD = '{$collect_pattern_cd}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //納期限情報取得
    public function getLimitDateDat($model)
    {
        $query .= "   SELECT ";
        $query .= "       * ";
        $query .= "   FROM ";
        $query .= "       COLLECT_SLIP_PLAN_LIMITDATE_DAT ";
        $query .= "   WHERE ";
        $query .= "       SCHOOLCD     = '{$model->schoolCd}' ";
        $query .= "   AND SCHOOL_KIND  = '{$model->schoolKind}' ";
        $query .= "   AND YEAR         = '{$model->year}' ";
        $query .= "   AND SCHREGNO     = '{$model->schregno}' ";
        $query .= "   AND SLIP_NO      = '{$model->field["SLIP_NO"]}' ";
        $query .= "   AND PLAN_YEAR || '-' ||  PLAN_MONTH = '{$model->field["PLAN_YEAR_MONTH"]}' ";

        return $query;
    }

    //元伝票の入金処理
    public function updateMotoSlipQuery($db, $model, $lCd, $mCd, $planYear, $planMonth, $paidMoney)
    {
        //更新 -- COLLECT_SLIP_PLAN_M_DAT
        knjp854Query::updatePlanMDat($db, $model, $lCd, $mCd, $planYear, $planMonth, $paidMoney);

        //追加 -- COLLECT_SLIP_PLAN_PAID_M_DAT
        knjp854Query::insertPaidMDat($db, $model, $lCd, $mCd, $planYear, $planMonth, $paidMoney);
    }

    //更新 -- COLLECT_SLIP_PLAN_M_DAT
    public function updatePlanMDat($db, $model, $lCd, $mCd, $planYear, $planMonth, $paidMoney)
    {
        list($paidYear, $paidMonth, $paidDay) = explode("-", $model->field["PLAN_PAID_MONEY_DATE"]);

        $data = array();
        $data["PLAN_MONEY"][NUMBER]     = $paidMoney;
        $data["PAID_MONEY"][NUMBER]     = $paidMoney;
        $data["PAID_YEARMONTH"][TEXT]   = $paidYear.$paidMonth;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "       SCHOOLCD     = '{$model->schoolCd}' ";
        $where .= "   AND SCHOOL_KIND  = '{$model->schoolKind}' ";
        $where .= "   AND YEAR         = '{$model->year}' ";
        $where .= "   AND SCHREGNO     = '{$model->schregno}' ";
        $where .= "   AND SLIP_NO      = '{$model->field["SLIP_NO"]}' ";
        $where .= "   AND COLLECT_L_CD = '{$lCd}' ";
        $where .= "   AND COLLECT_M_CD = '{$mCd}' ";
        $where .= "   AND PLAN_YEAR    = '{$planYear}' ";
        $where .= "   AND PLAN_MONTH   = '{$planMonth}' ";

        $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
        $db->query($query);
    }

    //追加 -- COLLECT_SLIP_PLAN_M_DAT
    public function insertPlanMDat($db, $model, $nextSlipNo, $lCd, $mCd, $planYear, $planMonth, $paidMoney)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $model->year;
        $data["SLIP_NO"][TEXT]          = $nextSlipNo;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["COLLECT_L_CD"][TEXT]     = $lCd;
        $data["COLLECT_M_CD"][TEXT]     = $mCd;
        $data["PLAN_YEAR"][TEXT]        = $planYear;
        $data["PLAN_MONTH"][TEXT]       = $planMonth;
        $data["PLAN_MONEY"][NUMBER]     = $paidMoney;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_M_DAT");
        $db->query($query);
    }

    //追加 -- COLLECT_SLIP_PLAN_PAID_M_DAT
    public function insertPaidMDat($db, $model, $lCd, $mCd, $planYear, $planMonth, $paidMoney)
    {
        $maxSeq = $db->getOne(knjp854Query::getMaxSeqMdat($model, $lCd, $mCd, $planYear, $planMonth));

        $data = array();
        $data["SCHOOLCD"][TEXT]             = $model->schoolCd;
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = $model->year;
        $data["SLIP_NO"][TEXT]              = $model->field["SLIP_NO"];
        $data["COLLECT_L_CD"][TEXT]         = $lCd;
        $data["COLLECT_M_CD"][TEXT]         = $mCd;
        $data["PLAN_YEAR"][TEXT]            = $planYear;
        $data["PLAN_MONTH"][TEXT]           = $planMonth;
        $data["SEQ"][TEXT]                  = $maxSeq + 1;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["PLAN_PAID_MONEY_DATE"][DATE] = $model->field["PLAN_PAID_MONEY_DATE"];
        $data["PLAN_PAID_MONEY"][NUMBER]    = $paidMoney;
        $data["PLAN_PAID_MONEY_DIV"][TEXT]  = $model->field["PLAN_PAID_MONEY_DIV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_M_DAT");
        $db->query($query);
    }
}
