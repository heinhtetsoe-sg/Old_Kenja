<?php

require_once('for_php7.php');

class knjh213query extends Query {
    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //部活動取得
    function getClub($model) {
        $query  = " WITH SCH_DOMI_HIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         DOMI_ENTDAY ";
        $query .= "     FROM ";
        $query .= "         SCHREG_DOMITORY_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         '".CTRL_YEAR."' BETWEEN FISCALYEAR(DOMI_ENTDAY) AND FISCALYEAR(value(DOMI_OUTDAY, '9999-03-31')) ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     CLUBM.CLUBCD AS VALUE, ";
        $query .= "     CLUBM.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT HIST ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                    AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                                    AND REGD.SCHREGNO = HIST.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "                                     AND GDAT.GRADE = REGD.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                                 AND GDAT.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                                 AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN CLUB_MST CLUBM ON HIST.CLUBCD      = CLUBM.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                          AND HIST.SCHOOLCD    = CLUBM.SCHOOLCD ";
            $query .= "                          AND HIST.SCHOOL_KIND = CLUBM.SCHOOL_KIND ";
        }
        $query .= "     INNER JOIN CLUB_YDAT CLUBY ON CLUBY.YEAR        = REGD.YEAR ";
        $query .= "                               AND CLUBM.CLUBCD      = CLUBY.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                           AND CLUBM.SCHOOLCD    = CLUBY.SCHOOLCD ";
            $query .= "                           AND CLUBM.SCHOOL_KIND = CLUBY.SCHOOL_KIND ";
        }
        $query .= "     INNER JOIN SCH_DOMI_HIST DOMI ON DOMI.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "         HIST.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND HIST.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "     AND GDAT.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         HIST.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
            $query .= "     AND HIST.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "     AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND '".CTRL_DATE."' BETWEEN HIST.SDATE AND value(HIST.EDATE, '9999-03-31') ";
        if ($model->field["GRADE_HR_CLASS"] != '') {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    function selectQuery($model) {
        $query  = " WITH SCH_DOMI_HIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         DOMI_CD, ";
        $query .= "         DOMI_ENTDAY, ";
        $query .= "         DOMI_OUTDAY ";
        $query .= "     FROM ";
        $query .= "         SCHREG_DOMITORY_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         '".CTRL_YEAR."' BETWEEN FISCALYEAR(DOMI_ENTDAY) AND FISCALYEAR(value(DOMI_OUTDAY, '9999-03-31')) ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     HR_NAME, ";
        $query .= "     BASE.NAME_SHOW AS NAME, ";
        $query .= "     DOMI.DOMI_CD, ";
        $query .= "     DOMI.DOMI_ENTDAY, ";
        $query .= "     DOMI.DOMI_OUTDAY, ";
        foreach ($model->monthArray as $key => $month) {
            $query .= "     D_{$month}.TEMPORARY_OUT_FLG AS FLG{$month}, ";
        }
        $query .= "     '' as dd ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        if ($model->Properties["useClubCombo"] == "1" && $model->field["CLUB"] != '') {
            $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT CLUB ON CLUB.SCHREGNO    = REGD.SCHREGNO ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "                                     AND CLUB.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $query .= "                                     AND CLUB.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                                     AND CLUB.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
                $query .= "                                     AND CLUB.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "                                         AND CLUB.CLUBCD      = '{$model->field["CLUB"]}' ";
            $query .= "                                         AND '".CTRL_DATE."' BETWEEN CLUB.SDATE AND value(CLUB.EDATE, '9999-03-31') ";
        }
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "                                    AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "                                    AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "                                    AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     INNER JOIN SCH_DOMI_HIST DOMI ON DOMI.SCHREGNO = REGD.SCHREGNO ";
        foreach ($model->monthArray as $key => $month) {
            $query .= "     LEFT JOIN SCHREG_DOMITORY_TEMPORARY_OUT_DAT D_{$month} ";
            $query .= "         ON D_{$month}.YEAR     = REGD.YEAR ";
            $query .= "        AND D_{$month}.MONTH    = '{$month}' ";
            $query .= "        AND D_{$month}.SCHREGNO = REGD.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["GRADE_HR_CLASS"] != '') {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["GRADE_HR_CLASS"] == '' && $model->field["CLUB"] == '') {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '00000' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND GDAT.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     DOMI.DOMI_ENTDAY ";

        return $query;
    }

    //更新処理
    function updateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //校納金使用プロパティが立っている時
        if ($model->Properties["useCollectMoneySchool"] == '1') {

            //入金額のまるめ変換用
            $round_digit = array("1" => "1", "2" => "10", "3" => "100");

            $henkanArr  = array();
            $colM       = array();
            $monthGData = array();

            //寮コードからLMコード変換用
            $query = knjh213Query::getLMcdDomi($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $henkanArr[$row["DOMI_CD"]] = array('COLLECT_L_CD'  => $row['COLLECT_L_CD'],
                                                    'COLLECT_M_CD'  => $row['COLLECT_M_CD'],
                                                    'MONTH_MONEY'   => $row['MONTH_MONEY'],
                                                    'DAY_MONEY'     => $row['DAY_MONEY']
                                                    );

                //項目[COLLECT_M_MST]データ取得
                $result2 = $db->query(knjh213Query::getCollectM($model, $row['COLLECT_L_CD'], $row['COLLECT_M_CD']));
                while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $lmcd = $row2["COLLECT_L_CD"].":".$row2["COLLECT_M_CD"];
                    $colM[$lmcd]["COLLECT_M_MONEY"] = $row2["COLLECT_M_MONEY"];
                    $colM[$lmcd]["DIVIDE_PROCESS"]  = $row2["DIVIDE_PROCESS"];
                    $colM[$lmcd]["ROUND_DIGIT"]     = $row2["ROUND_DIGIT"];
                }

                //入金計画取得
                $result3 = $db->query(knjh213Query::getCollectMonthGrpDat($model, $row['COLLECT_L_CD'], $row['COLLECT_M_CD']));
                while ($row3 = $result3->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $startFlg = false;
                    $cGcdKey = $row3["COLLECT_GRP_CD"].":".$row3["COLLECT_L_CD"].":".$row3["COLLECT_M_CD"];
                    $monthGData[$cGcdKey] = $row3;
                    foreach ($model->monthArray as $key => $month) {
                        $intMonth = intval($month);
                        if ($row3["COLLECT_MONTH_".$intMonth] == '1') {
                            if (!$startFlg) $monthGData[$cGcdKey]["PLAN_START_MONTH"] = $month; // 入金計画の最初の月をセット
                            $monthGData[$cGcdKey]["PLAN_FIN_MONTH"] = $month; // 入金計画の最終の月をセット
                            $startFlg = true;
                        }
                    }
                }
            }

        }

        //追加-更新　生徒毎
        $schArr = explode(",", $model->sch_list);
        foreach ($schArr as $key => $schregNo) {
            //削除
            $query  = " DELETE FROM ";
            $query .= "     SCHREG_DOMITORY_TEMPORARY_OUT_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SCHREGNO = '{$schregNo}' ";
            $db->query($query);

            $hasuuFlg  = false;
            $counter   = 0;
            $money_sum = 0;

            foreach ($model->monthArray as $key => $month) {
                $setFlg = $model->data["FLG".$month][$schregNo];
                if (strlen($setFlg)) {
                    $data = array();
                    $data["YEAR"][TEXT]                 = CTRL_YEAR;
                    $data["MONTH"][TEXT]                = $month;
                    $data["SCHREGNO"][TEXT]             = $schregNo;
                    $data["TEMPORARY_OUT_FLG"][TEXT]    = $setFlg;
                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][FUNC]              = "sysdate()";

                    $db->query(Query::insertSQL($data, "SCHREG_DOMITORY_TEMPORARY_OUT_DAT"));
                }

                //校納金使用プロパティが立っている時
                if ($model->Properties["useCollectMoneySchool"] == '1') {
                    //グレーアウトは更新しない
                    if ($model->setData[$schregNo][$month]) {
                        $setDomiCd = $model->setData[$schregNo][$month]["DOMI_CD"];
                        //寮コードからLコード、Mコードに変換
                        $setLcd    = $henkanArr[$setDomiCd]['COLLECT_L_CD'];
                        $setMcd    = $henkanArr[$setDomiCd]['COLLECT_M_CD'];
                        $setLMcd   = $setLcd.':'.$setMcd;
                        //項目マスタの年額をセット
                        $total_money = $colM[$setLMcd]["COLLECT_M_MONEY"];
                        //寮マスタの一時退寮費をセット（nullは0をセット）
                        $temporarilyMoney = ($henkanArr[$setDomiCd]['MONTH_MONEY'] != '') ? $henkanArr[$setDomiCd]['MONTH_MONEY']: 0;

                        //COLLECT_SLIP_PLAN_M_DATから伝票情報取得
                        $query = knjh213Query::getCollectSlipPlanMDat($model, $schregNo, $setLcd, $setMcd, $month);
                        $result = $db->query($query);
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $intMonth = intval($month);
                            $setGLMkey = $row["COLLECT_GRP_CD"].':'.$setLMcd;

                            //項目マスタの入金計画月にフラグが無いときは、更新しない
                            if ($monthGData[$setGLMkey]["COLLECT_MONTH_".$intMonth] != "1") continue;

                            //端数、入金額のまるめ処理用
                            $setMonthCntG = $monthGData[$setGLMkey]["MONTH_CNT"];
                            if ($setMonthCntG) {
                                if ($colM[$setLMcd]["DIVIDE_PROCESS"] == "3") {
                                    if (!$hasuuFlg) $tukiwari = number_format((int)$total_money / (int)$setMonthCntG, 4, '.', '');
                                    if (!$hasuuFlg) list ($int, $decimal) = explode(".", $tukiwari);
                                    if (!$hasuuFlg) $decimal = "0.".$decimal;
                                } else {
                                    $amari = (int)$total_money % ((int)$setMonthCntG * (int)$round_digit[$colM[$setLMcd]["ROUND_DIGIT"]]);
                                    $tukiwari = ((int)$total_money - $amari) / (int)$setMonthCntG;
                                }
                            }
                            if (!$hasuuFlg) $hasuu = $decimal;

                            $planStratMonth = $monthGData[$setGLMkey]["PLAN_START_MONTH"];
                            $defaultMoney = "";
                            if ($colM[$setLMcd]["DIVIDE_PROCESS"] == "1") { // 初月
                                $defaultMoney = ($month == $planStratMonth) ? ($tukiwari + $amari) : $tukiwari;
                            } else if ($colM[$setLMcd]["DIVIDE_PROCESS"] == "2") { // 最終月
                                $defaultMoney = $tukiwari;
                            } else if ($colM[$setLMcd]["DIVIDE_PROCESS"] == "3") { // 1円未満を繰越、整数分を加算
                                $defaultMoney = (int)$int + floor($hasuu);
                            }

                            //チェック月は、一時退寮費をセット or 一時退寮費が指定無しは0円にセット
                            $setPlanMoney = (strlen($setFlg)) ? $temporarilyMoney: $defaultMoney;

                            //COLLECT_SLIP_PLAN_M_DAT
                            $data = array();
                            $data["PLAN_MONEY"][NUMBER]     = $setPlanMoney;
                            $data["REGISTERCD"][TEXT]       = STAFFCD;
                            $data["UPDATED"][NUMBER]        = "sysdate()";

                            $planYear = ($month == "01" || $month == "02" || $month == "03") ? CTRL_YEAR + 1: CTRL_YEAR;
                            $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                            $where .= "   AND SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
                            $where .= "   AND YEAR         = '".CTRL_YEAR."' ";
                            $where .= "   AND SCHREGNO     = '{$schregNo}' ";
                            $where .= "   AND SLIP_NO      = '{$row["SLIP_NO"]}' ";
                            $where .= "   AND COLLECT_L_CD = '{$setLcd}' ";
                            $where .= "   AND COLLECT_M_CD = '{$setMcd}' ";
                            $where .= "   AND PLAN_YEAR    = '{$row["PLAN_YEAR"]}' ";
                            $where .= "   AND PLAN_MONTH   = '{$month}' ";

                            $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                            $db->query($query);

                            $setGLMkey2 = $setGLMkey;
                            $slipNo     = $row["SLIP_NO"];
                            $plan_year  = $row["PLAN_YEAR"];
                            $plan_month = $month;

                            $money_sum += (int)$setPlanMoney;
                            $hasuu     -= floor($hasuu);
                            $hasuu     += (int)$decimal;

                            $hasuuFlg = true;
                            $counter++;
                        }
                        //入金計画の最終月と画面の最終月が一致しているかに使用
                        $pFm = $monthGData[$setGLMkey2]["PLAN_FIN_MONTH"];
                        $planFinMonth = ($pFm < 04)   ? (int)$pFm   + 12: $pFm;
                        $finMonth     = ($month < 04) ? (int)$month + 12: $month;
                    }
                }
            }

            //校納金使用プロパティが立っている時
            if ($model->Properties["useCollectMoneySchool"] == '1') {
                //項目マスタの分割端数が「2:最終月」or「3:1円未満を繰越、整数分を加算」の時、
                if (($colM[$setLMcd]["DIVIDE_PROCESS"] == "2" || 
                    ($colM[$setLMcd]["DIVIDE_PROCESS"] == "3" && $total_money != $money_sum)) && 
                    ($counter > 0) && $planFinMonth <= $finMonth) {

                    $plan_money = ($colM[$setLMcd]["DIVIDE_PROCESS"] == "2") ? ($tukiwari + (int)$amari) : ($hasuu + (int)$defaultMoney);

                    //更新 -- COLLECT_SLIP_PLAN_M_DAT
                    $data = array();
                    $data["PLAN_MONEY"][NUMBER]     = $plan_money;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $where  = " WHERE SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                    $where .= "   AND SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
                    $where .= "   AND YEAR         = '".CTRL_YEAR."' ";
                    $where .= "   AND SCHREGNO     = '{$schregNo}' ";
                    $where .= "   AND SLIP_NO      = '".$slipNo."' ";
                    $where .= "   AND COLLECT_L_CD = '".$setLcd."' ";
                    $where .= "   AND COLLECT_M_CD = '".$setMcd."' ";
                    $where .= "   AND PLAN_YEAR    = '".$plan_year."' ";
                    $where .= "   AND PLAN_MONTH   = '".$plan_month."' ";

                    $query = Query::updateSQL($data, "COLLECT_SLIP_PLAN_M_DAT", $where);
                    $db->query($query);
                }

                //更新 -- COLLECT_SLIP_M_DAT
                if ($counter > 0) {
                    $query = knjh213Query::getCollectSlipPlanMDatTotal($model, $schregNo, $slipNo, $setLcd, $setMcd);
                    $sumSetColectMoney = $db->getOne($query);

                    $data = array();
                    $data["COLLECT_MONEY"][NUMBER]  = $sumSetColectMoney;
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $where  = " WHERE ";
                    $where .= "         YEAR         = '".CTRL_YEAR."' ";
                    $where .= "     AND SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
                    $where .= "     AND SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
                    $where .= "     AND SLIP_NO      = '".$slipNo."' ";
                    $where .= "     AND COLLECT_L_CD = '".$setLcd."' ";
                    $where .= "     AND COLLECT_M_CD = '".$setMcd."' ";
                    $query = Query::updateSQL($data, "COLLECT_SLIP_M_DAT", $where);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //寮コードからLMコード取得
    function getLMcdDomi($model) {
        $query  = " SELECT ";
        $query .= "     DOMI_CD, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD, ";
        $query .= "     MONTH_MONEY, ";
        $query .= "     DAY_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_DOMITORY_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";

        return $query;
    }

    //入金項目一覧取得
    function getCollectM($model, $lCd, $mCd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND COLLECT_L_CD = '{$lCd}' ";
        $query .= "     AND COLLECT_M_CD = '{$mCd}' ";

        return $query;
    }

    //入金計画取得
    function getCollectMonthGrpDat($model, $lCd, $mCd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_MONTH_GRP_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD        = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND COLLECT_L_CD = '{$lCd}' ";
        $query .= "     AND COLLECT_M_CD = '{$mCd}' ";

        return $query;
    }

    //COLLECT_SLIP_PLAN_M_DAT取得
    function getCollectSlipPlanMDat($model, $schregNo, $setLcd, $setMcd, $month) {
        $query  = " SELECT ";
        $query .= "     PLAN.*, ";
        $query .= "     SLIP.COLLECT_GRP_CD ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT PLAN ";
        $query .= "     LEFT JOIN COLLECT_SLIP_DAT SLIP ON PLAN.SCHOOLCD    = SLIP.SCHOOLCD ";
        $query .= "                                    AND PLAN.SCHOOL_KIND = SLIP.SCHOOL_KIND ";
        $query .= "                                    AND PLAN.YEAR        = SLIP.YEAR ";
        $query .= "                                    AND PLAN.SLIP_NO     = SLIP.SLIP_NO ";
        $query .= " WHERE ";
        $query .= "         PLAN.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND PLAN.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND PLAN.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND PLAN.SCHREGNO     = '".$schregNo."' ";
        $query .= "     AND PLAN.COLLECT_L_CD = '{$setLcd}' ";
        $query .= "     AND PLAN.COLLECT_M_CD = '{$setMcd}' ";
        $query .= "     AND PLAN.PLAN_MONTH   = '{$month}' ";
        $query .= "     AND SLIP.CANCEL_DATE  IS NULL ";

        return $query;
    }

    //COLLECT_SLIP_PLAN_M_DATの計画合計取得
    function getCollectSlipPlanMDatTotal($model, $schregNo, $slipNo, $setLcd, $setMcd) {
        $query  = " SELECT ";
        $query .= "     sum(PLAN.PLAN_MONEY) as PLAN_MONEY_TOTAL ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT PLAN ";
        $query .= "     LEFT JOIN COLLECT_SLIP_DAT SLIP ON PLAN.SCHOOLCD    = SLIP.SCHOOLCD ";
        $query .= "                                    AND PLAN.SCHOOL_KIND = SLIP.SCHOOL_KIND ";
        $query .= "                                    AND PLAN.YEAR        = SLIP.YEAR ";
        $query .= "                                    AND PLAN.SLIP_NO     = SLIP.SLIP_NO ";
        $query .= " WHERE ";
        $query .= "         PLAN.SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND PLAN.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND PLAN.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND PLAN.SCHREGNO     = '".$schregNo."' ";
        $query .= "     AND PLAN.SLIP_NO      = '{$slipNo}' ";
        $query .= "     AND PLAN.COLLECT_L_CD = '{$setLcd}' ";
        $query .= "     AND PLAN.COLLECT_M_CD = '{$setMcd}' ";
        $query .= "     AND SLIP.CANCEL_DATE  IS NULL ";
        $query .= " GROUP BY ";
        $query .= "     PLAN.SLIP_NO, ";
        $query .= "     PLAN.COLLECT_L_CD, ";
        $query .= "     PLAN.COLLECT_M_CD ";

        return $query;
    }
}
?>
