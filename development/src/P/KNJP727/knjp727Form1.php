<?php

require_once('for_php7.php');

class knjp727Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("", "POST", "knjp727index.php", "", "");
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["WindowWidth"] = $model->windowWidth - 36;
        $arg["titleWindowWidth"] = $model->windowWidth - 477;
        $arg["valWindowWidth"] = $model->windowWidth - 460;
        $arg["valWindowHeight"] = $model->windowHeight - 200;
        $arg["tcolWindowHeight"] = $model->windowHeight - 217;
        $arg["setWidth"] = 2000;
        $resizeFlg = $model->cmd == "cmdStart" ? true : false;

        //radio
        $opt = array(1, 2);
        $model->search["STD_DIV"] = ($model->search["STD_DIV"] == "") ? "2" : $model->search["STD_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"STD_DIV{$val}\" onClick=\"btn_submit('chgStdDiv')\"");
        }
        $radioArray = knjCreateRadio($objForm, "STD_DIV", $model->search["STD_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //対象年度
        $model->exeYear = CTRL_YEAR;
        if ($model->search["STD_DIV"] == "1") {
            $model->exeYear = CTRL_YEAR + 1;
        }
        $arg["CTRL_YEAR"] = $model->exeYear;
        $arg["THIS_YEAR"] = $model->exeYear."年";
        $arg["LAST_YEAR"] = ($model->exeYear - 1)."年";

        //学年
        $model->schoolKindAry = array();
        $result = $db->query(knjp727Query::selectQueryGrade($model));
        $opt1 = $opt2 = array();
        $opt2[] = array("label" => "　　　　" ,
                        "value" => "00-000");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[$row["GRADE"]] = array("label"  => $row["GRADE"],
                                         "value"  => $row["GRADE"]);
            $model->schoolKindAry[$row["GRADE"]] = $row["SCHOOL_KIND"];
            if (!isset($model->search["GRADE"])) $model->search["GRADE"] = $row["GRADE"];
            if ($row["GRADE"] == $model->search["GRADE"]) {
                $opt2[] = array("label" => $row["HR_NAMEABBV"] ,
                                "value" => $row["GRADE"] ."-" .$row["HR_CLASS"]);

                if (!isset($model->search["HR_CLASS"]) || $model->cmd == "maingrade") {
                    $model->search["HR_CLASS"] = $row["GRADE"] ."-" .$row["HR_CLASS"];
                    $model->cmd = "main";
                }
            }
        }

        //学年
        $extra = "onchange=\"btn_submit('maingrade');\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->search["GRADE"], $opt1, $extra, 1);
        //hidden
        knjCreateHidden($objForm, "GRADE_SEND", $model->search["GRADE"]);

        $model->mstLastYear = $model->search["GRADE"] == "01" ? $model->exeYear : $model->exeYear - 1;

        //年組
        $extra = "onchange=\"btn_submit('mainclass');\"";
        $arg["HR_CLASS"] = knjCreateCombo($objForm, "HR_CLASS", $model->search["HR_CLASS"], $opt2, $extra, 1);
        //hidden
        knjCreateHidden($objForm, "HR_CLASS_SEND", $model->search["HR_CLASS"]);

        /********************/
        /* チェックボックス */
        /********************/
        //基準額1
        $extra = "onclick=\"checkAll(this)\"";
        $arg["COUNTRY_BASE_ALL_1"] = knjCreateCheckBox($objForm, "COUNTRY_BASE_ALL_1", "1", $extra);
        //基準額2
        $extra = "onclick=\"checkAll(this)\"";
        $arg["COUNTRY_BASE_ALL_2"] = knjCreateCheckBox($objForm, "COUNTRY_BASE_ALL_2", "1", $extra);
        //加算1
        $extra = "onclick=\"checkAll(this)\"";
        $arg["COUNTRY_ADD_ALL_1"] = knjCreateCheckBox($objForm, "COUNTRY_ADD_ALL_1", "1", $extra);
        //加算2
        $extra = "onclick=\"checkAll(this)\"";
        $arg["COUNTRY_ADD_ALL_2"] = knjCreateCheckBox($objForm, "COUNTRY_ADD_ALL_2", "1", $extra);
        //減免額1
        $extra = "onclick=\"checkAll(this)\"";
        $arg["REDUC_SCHOOL_ALL1"] = knjCreateCheckBox($objForm, "REDUC_SCHOOL_ALL1", "1", $extra);
        //減免額2
        $extra = "onclick=\"checkAll(this)\"";
        $arg["REDUC_SCHOOL_ALL2"] = knjCreateCheckBox($objForm, "REDUC_SCHOOL_ALL2", "1", $extra);

        //特殊フラグ
        $optP711 = array();
        $value_flg = false;
        $query = knjp727Query::selectQuerySpecailCode($model);
        $name_result = $db->query($query);
        $optP711[] = array('label' => '',
                           'value' => 'DUMMY:0');
        while ($name_row = $name_result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optP711[] = array('label' => $name_row["LABEL"],
                               'value' => $name_row["VALUE"]);
        }

        //月コンボ用
        $optMonth[] = array("VALUE" => "", "LABEL" => "");
        for ($monthCnt = 4; $monthCnt <= 15; $monthCnt++) {
            $setMonth = $monthCnt <= 12 ? $monthCnt : $monthCnt - 12;
            $setMonthValue = $setMonth < 10 ? "0".$setMonth : $setMonth;
            $setMonthLabel = $setMonth < 10 ? "&nbsp;".$setMonth."月" : $setMonth."月";
            $optMonth[] = array("value" => $setMonthValue, "label" => $setMonthLabel);
        }

        //支援確定月1
        $extra = "";
        $arg["COUNTRY_DEC_MONTH1"] = knjCreateCombo($objForm, "COUNTRY_DEC_MONTH1", $model->decMonth["COUNTRY_DEC_MONTH1"], $optMonth, $extra, 1);

        //支援確定月2
        $extra = "";
        $arg["COUNTRY_DEC_MONTH2"] = knjCreateCombo($objForm, "COUNTRY_DEC_MONTH2", $model->decMonth["COUNTRY_DEC_MONTH2"], $optMonth, $extra, 1);

        //生徒授業料
        $model->slipPlan = array();
        $query = knjp727Query::getSlipPlan($model);
        $slipPlanResult = $db->query($query);
        while ($row = $slipPlanResult->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->slipPlan[$row["SLIP_NO"]] = $row;
        }
        $slipPlanResult->free();

        //支援金MST
        $model->countryMst = array();
        $query = knjp727Query::countryMstMonty($model);
        $countyrMstResult = $db->query($query);
        while ($row = $countyrMstResult->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->countryMst[] = $row;
        }
        $countyrMstResult->free();

        //支援金加算MST
        $model->countryAddMst = array();
        $query = knjp727Query::countryAddMstMonty($model);
        $countyrAddMstResult = $db->query($query);
        while ($row = $countyrAddMstResult->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->countryAddMst[] = $row;
        }
        $countyrAddMstResult->free();

        //支援ランク
        $model->cuntryRank = array();
        $query = knjp727Query::getNameMstSql($model, "P001");
        $cuntryRankResult = $db->query($query);
        $optCuntryRank[] = array("VALUE" => "", "LABEL" => "");
        while ($row = $cuntryRankResult->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->cuntryRank[$row["VALUE"]] = $row["LABEL"];
            $optCuntryRank[] = array("value" => $row["VALUE"], "label" => $row["LABEL"]);
        }
        $cuntryRankResult->free();

        //生徒減免(先)
        $model->reducStdSaki = array();
        $query = knjp727Query::getReductionStd($model, "1");
        $reducStdResult = $db->query($query);
        while ($row = $reducStdResult->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->reducStdSaki[$row["SCHREGNO"]] = $row;
        }
        $reducStdResult->free();

        //生徒減免(後)
        $model->reducStdAto = array();
        $query = knjp727Query::getReductionStd($model, "2");
        $reducStdResult = $db->query($query);
        while ($row = $reducStdResult->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->reducStdAto[$row["SCHREGNO"]][] = $row;
        }
        $reducStdResult->free();

        $query = knjp727Query::selectQuery($model);
        $result = $db->query($query);
        $model->slipno = array();

        $dataCnt = 0;
        $dataArray = array();
        $schregCnt = array();
        $model->slipSchreg = array();
        $model->slipInfo = array();
        $model->slipSchoolStd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataArray[] = $row;
            $schregCnt[$row["SCHREGNO"]]++;
            $model->slipSchreg[$row["SLIP_NO"]] = $row["SCHREGNO"];
            if ($row["IS_CREDITCNT"] != "1") {
                $row["CREDITS"] = "1";
            }
            $model->slipInfo[$row["SLIP_NO"]] = $row;
            $model->slipSchoolStd[$row["SLIP_NO"]] = array("SAKI1" => "", "ATO1" => "");
        }

        foreach ($dataArray as $key => $row) {
            $model->slipno[] = $row["SLIP_NO"];
            @array_walk($row, "htmlspecialchars_array");

            $setRow = array();
            if ($befSchregNo != $row["SCHREGNO"]) {
                if ($schregCnt[$row["SCHREGNO"]] > 1) {
                    $setRow["LEFT_ROWSPAN"] = "rowspan={$schregCnt[$row["SCHREGNO"]]}";
                }

                //年組番
                $setRow["HR_NAMEABBV"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];

                //氏名
                $setRow["NAME"] = $row["NAME"];

                //学籍番号
                $setRow["SCHREGNO"] = $row["SCHREGNO"];
            }
            $befSchregNo = $row["SCHREGNO"];

            //伝票番号
            $setRow["SLIP_NO"] = $row["SLIP_NO"];

            //確定
            $disabled = "";
            if ($row["COUNTRY_BASE_FLG_1"] == "1" ||
                $row["COUNTRY_ADD_FLG_1"] == "1" ||
                $row["COUNTRY_BASE_FLG_2"] == "1" ||
                $row["COUNTRY_ADD_FLG_2"] == "1"
            ) {
                $disabled = " disabled ";
            }
            $extra = " id=\"KAKUTEI_{$row["SLIP_NO"]}\" onclick=\"clickKakutei(this, '{$row["SLIP_NO"]}')\" ";
            $setRow["KAKUTEI"] = knjCreateCheckBox($objForm, "KAKUTEI[]", $row["SLIP_NO"], $disabled.$extra);

            $jugyouryou = $model->slipPlan[$row["SLIP_NO"]]["TOTAL_JUGYOU"];

            //合計額
            $countryTotalMoney = 0;
            /*****/
            /* 1 */
            /*****/

            //特殊コード：入力可/不可の判定用
            list($cuntryDispCd, $dispSetumei) = preg_split("/:/", $row["CUNTRY_TEXT_DISP"]);
            $countryTextNone1 = $cuntryDispCd == "1" ? "" : "display:none";

            //記号1
            $row["COUNTRY_RARE_CASE_CD"] = $row["COUNTRY_RARE_CASE_CD"] ? $row["COUNTRY_RARE_CASE_CD"] : $optP711[0]["value"];
            $extra = " id='COUNTRY_RARE_CASE_CD_{$row["SLIP_NO"]}' onChange=\"kick_chgIncome(this, '{$row["SLIP_NO"]}', 'COUNTRY_', '{$dataCnt}')\"";
            $extra .= ($row["COUNTRY_BASE_FLG_1"] == "1" || $row["COUNTRY_BASE_FLG_2"] == "1" || $row["COUNTRY_BASE_FLG_2"] == "1" || $row["COUNTRY_ADD_FLG_2"] == "1") ? " onClick=\"alert('確定済みです。');\"" : "";
            $setRow["COUNTRY_RARE_CASE_CD"] = knjCreateCombo($objForm, "COUNTRY_RARE_CASE_CD[]", $row["COUNTRY_RARE_CASE_CD"], $optP711, $extra, 1);

            //REDUCTION_SEQ_1
            $extra = "id='REDUCTION_SEQ_1_{$row["SLIP_NO"]}' style=\"display:none\"";
            $setRow["REDUCTION_SEQ_1"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_1"], "REDUCTION_SEQ_1[]", "", "", $extra);

            //基準額
            if ($cuntryDispCd == "1") {
                $countryTotalMoney += ($row["COUNTRY_MONEY_1"]);
                $countryTotalMoney += ($row["COUNTRY_MONEY_2"]);
            } else {
                $setRow["COUNTRY_DISP_1"] = (is_numeric($row["COUNTRY_MONEY_1"])) ? number_format($row["COUNTRY_MONEY_1"]) : "";
                $countryTotalMoney += ($row["COUNTRY_MONEY_1"] * 3);
                $setRow["COUNTRY_DISP_2"] = (is_numeric($row["COUNTRY_MONEY_2"])) ? number_format($row["COUNTRY_MONEY_2"]) : "";
                $countryTotalMoney += ($row["COUNTRY_MONEY_2"] * 9);
            }

            //支援ランク1
            $extra = " id='COUNTRY_RANK_1_{$row["SLIP_NO"]}' ";
            $setRow["COUNTRY_RANK_1"] = knjCreateCombo($objForm, "COUNTRY_RANK_1[]", $row["COUNTRY_RANK_1"], $optCuntryRank, $extra, 1);

            //基準額1
            $color = ($row["COUNTRY_BASE_FLG_1"] == "1") ? "#999999" : "#ffffff";
            $readOnly = ($row["COUNTRY_BASE_FLG_1"] == "1") ? " readOnly=\"readOnly\" " : "";
            $extra = "id='COUNTRY_MONEY_1_{$row["SLIP_NO"]}' style=\"text-align:right; background-color:{$color}; {$countryTextNone1}\" onChange=\"setTotalmony('{$row["SLIP_NO"]}');\"";
            $setRow["COUNTRY_MONEY_1"] = knjCreateTextBox($objForm, $row["COUNTRY_MONEY_1"], "COUNTRY_MONEY_1[]", "6", "6", $extra.$readOnly);

            //基準額決定1
            $checked = $row["COUNTRY_BASE_FLG_1"] == "1" ? "checked=checked" : "";
            $disabled = "";
            $extra = "{$checked} {$disabled} id='COUNTRY_BASE_FLG_1_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
            $setRow["COUNTRY_BASE_FLG_1"] = knjCreateCheckBox($objForm, "COUNTRY_BASE_FLG_1[]", $row["SLIP_NO"], $extra);

            //支援ランク2
            $extra = " id='COUNTRY_RANK_2_{$row["SLIP_NO"]}' ";
            $setRow["COUNTRY_RANK_2"] = knjCreateCombo($objForm, "COUNTRY_RANK_2[]", $row["COUNTRY_RANK_2"], $optCuntryRank, $extra, 1);

            //基準額2
            $color = ($row["COUNTRY_BASE_FLG_2"] == "1") ? "#999999" : "#ffffff";
            $readOnly = ($row["COUNTRY_BASE_FLG_2"] == "1") ? " readOnly=\"readOnly\" " : "";
            $extra = "id='COUNTRY_MONEY_2_{$row["SLIP_NO"]}' style=\"text-align:right; background-color:{$color}; {$countryTextNone1}\" onChange=\"setTotalmony('{$row["SLIP_NO"]}');\"";
            $setRow["COUNTRY_MONEY_2"] = knjCreateTextBox($objForm, $row["COUNTRY_MONEY_2"], "COUNTRY_MONEY_2[]", "6", "6", $extra.$readOnly);

            //基準額決定2
            $checked = $row["COUNTRY_BASE_FLG_2"] == "1" ? "checked=checked" : "";
            $disabled = "";
            $extra = "{$checked} {$disabled} id='COUNTRY_BASE_FLG_2_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
            $setRow["COUNTRY_BASE_FLG_2"] = knjCreateCheckBox($objForm, "COUNTRY_BASE_FLG_2[]", $row["SLIP_NO"], $extra);

            //加算額
            if ($cuntryDispCd == "1") {
                $countryTotalMoney += ($row["COUNTRY_ADD_MONEY_1"]);
                $countryTotalMoney += ($row["COUNTRY_ADD_MONEY_2"]);
            } else {
                $setRow["COUNTRY_ADD_DISP_1"] = (is_numeric($row["COUNTRY_ADD_MONEY_1"])) ? number_format($row["COUNTRY_ADD_MONEY_1"]) : "";
                $countryTotalMoney += ($row["COUNTRY_ADD_MONEY_1"] * 3);
                $setRow["COUNTRY_ADD_DISP_2"] = (is_numeric($row["COUNTRY_ADD_MONEY_2"])) ? number_format($row["COUNTRY_ADD_MONEY_2"]) : "";
                $countryTotalMoney += ($row["COUNTRY_ADD_MONEY_2"] * 9);
            }
            $setRow["COUNTRY_TOTAL_MONEY"] = number_format($countryTotalMoney);

            //加算額1
            $color = ($row["COUNTRY_ADD_FLG_1"] == "1") ? "#999999" : "#ffffff";
            $readOnly = ($row["COUNTRY_ADD_FLG_1"] == "1") ? " readOnly=\"readOnly\" " : "";
            $extra = "id='COUNTRY_ADD_MONEY_1_{$row["SLIP_NO"]}' style=\"text-align:right; background-color:{$color}; {$countryTextNone1} \" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"setTotalmony('{$row["SLIP_NO"]}');\"";
            $setRow["COUNTRY_ADD_MONEY_1"] = knjCreateTextBox($objForm, $row["COUNTRY_ADD_MONEY_1"], "COUNTRY_ADD_MONEY_1[]", "6", "6", $extra.$readOnly);

            //加算額決定1
            $checked = $row["COUNTRY_ADD_FLG_1"] == "1" ? "checked=checked" : "";
            $disabled = "";
            $extra = "{$checked} {$disabled} id='COUNTRY_ADD_FLG_1_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
            $setRow["COUNTRY_ADD_FLG_1"] = knjCreateCheckBox($objForm, "COUNTRY_ADD_FLG_1[]", $row["SLIP_NO"], $extra);

            //加算額2
            $color = ($row["COUNTRY_ADD_FLG_2"] == "1") ? "#999999" : "#ffffff";
            $readOnly = ($row["COUNTRY_ADD_FLG_2"] == "1") ? " readOnly=\"readOnly\" " : "";
            $extra = "id='COUNTRY_ADD_MONEY_2_{$row["SLIP_NO"]}' style=\"text-align:right; background-color:{$color}; {$countryTextNone1} \" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"setTotalmony('{$row["SLIP_NO"]}');\"";
            $setRow["COUNTRY_ADD_MONEY_2"] = knjCreateTextBox($objForm, $row["COUNTRY_ADD_MONEY_2"], "COUNTRY_ADD_MONEY_2[]", "6", "6", $extra.$readOnly);

            //加算額決定2
            $checked = $row["COUNTRY_ADD_FLG_2"] == "1" ? "checked=checked" : "";
            $disabled = "";
            $extra = "{$checked} {$disabled} id='COUNTRY_ADD_FLG_2_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
            $setRow["COUNTRY_ADD_FLG_2"] = knjCreateCheckBox($objForm, "COUNTRY_ADD_FLG_2[]", $row["SLIP_NO"], $extra);

            /*****/
            /* 2 */
            /*****/

            //REDUCTION_SEQ_2
            $extra = "id='REDUCTION_SEQ_2_{$row["SLIP_NO"]}' style=\"display:none\"";
            $setRow["REDUCTION_SEQ_2"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_2"], "REDUCTION_SEQ_2[]", "", "", $extra);

            /* TipMessage */
            $genmen = "";
            if (get_count($model->reducStdSaki[$row["SCHREGNO"]]) > 0 ||get_count($model->reducStdAto[$row["SCHREGNO"]]) > 0) {
                $genmen = "<span style=\"color: blue;font-weight: bold;\" onMouseOver=\"getInfo('".$row["SCHREGNO"]."','G')\" onMouseOut=\"goOffInfo();\">減</span>";
            }
            if (0 < $row["COUNTTRANSFER"]) {
                $transfer = "<span style=\"color: #B8860B;font-weight: bold;\" onMouseOver=\"getInfo('".$row["SCHREGNO"]."','T')\" onMouseOut=\"goOffInfo();\">異</span>";
            } else {
                $transfer = "　";
            }
            $setRow["GENMEN_TRANSFER_MARK"] = $genmen." ".$transfer;

            //減免先
            $setFirstSaki1 = 0;
            $setLastSaki1 = 0;
            list($jugyouSaki, $genmenJugyouSaki) = knjp727Model::setReductionStd($model->reducStdSaki[$row["SCHREGNO"]], $model->slipPlan[$row["SLIP_NO"]]["TOTAL_JUGYOU"], "saki");
            list($retMoney1, $sMonth1, $eMonth1) = knjp727Model::getReductionMoneyCal($model->reducStdSaki[$row["SCHREGNO"]], $jugyouSaki);
            if ($model->reducStdSaki[$row["SCHREGNO"]]["REDUCTION_DIV"] == "2") {
                $setFirstSaki1 += $genmenJugyouSaki;
            } else if ($model->reducStdSaki[$row["SCHREGNO"]]["REDUCTION_DIV"] == "1") {
                $setLastSaki1 += $genmenJugyouSaki;
            }
            $model->reducStdSaki[$row["SCHREGNO"]]["MONTH_MONEY"] = $jugyouSaki;
            $model->reducStdSaki[$row["SCHREGNO"]]["S_MONTH"] = $sMonth1;
            $model->reducStdSaki[$row["SCHREGNO"]]["E_MONTH"] = $eMonth1;

            //減免先換算後の授業料・授業料等をセット
            if ($jugyouSaki > 0) $jugyouryou = $jugyouSaki;
            $setRow["JUGYOURYOU"] = (is_numeric($jugyouryou)) ? number_format($jugyouryou) : "";

            //減免後
            if (!is_array($model->reducStdAto[$row["SCHREGNO"]])) {
                $model->reducStdAto[$row["SCHREGNO"]] = array();
            }

            $setFirstAto1 = 0;
            $setFirstAto2 = 0;
            $setLastAto1 = 0;
            $setLastAto2 = 0;
            $denominator = 0;
            foreach ($model->reducStdAto[$row["SCHREGNO"]] as $scoolStdKey => $scoolStdVal) {
                list($jugyouAto, $genmenJugyouAto) = knjp727Model::setReductionStd($scoolStdVal, $model->slipPlan[$row["SLIP_NO"]]["TOTAL_JUGYOU"], "ato");
                list($jugyouTouAto, $genmenJugyouTouAto) = knjp727Model::setReductionStd($scoolStdVal, $model->slipPlan[$row["SLIP_NO"]]["TOTAL_JUGYOUTOU"], "ato");
                list($retMoney1, $sMonth1, $eMonth1) = knjp727Model::getReductionMoneyCal($scoolStdVal, $jugyouAto);
                list($retMoney2, $sMonth2, $eMonth2) = knjp727Model::getReductionMoneyCal($scoolStdVal, $jugyouTouAto);
                if ($scoolStdVal["REDUCTION_DIV"] == "2") {
                    $setFirstAto1 += $genmenJugyouAto;
                    $setFirstAto2 += $genmenJugyouTouAto;
                    $denominator += $genmenJugyouAto + $genmenJugyouTouAto;
                } else if ($scoolStdVal["REDUCTION_DIV"] == "1") {
                    $setLastAto1 += $genmenJugyouAto;
                    $setLastAto2 += $genmenJugyouTouAto;
                    $denominator += $genmenJugyouAto + $genmenJugyouTouAto;
                }

                $model->reducStdAto[$row["SCHREGNO"]][$scoolStdKey]["MONTH_MONEY"] = $jugyouAto;
                $model->reducStdAto[$row["SCHREGNO"]][$scoolStdKey]["S_MONTH"] = $sMonth1;
                $model->reducStdAto[$row["SCHREGNO"]][$scoolStdKey]["E_MONTH"] = $eMonth1;
                $model->reducStdAto[$row["SCHREGNO"]][$scoolStdKey]["MONTH_MONEY_TOU"] = $jugyouTouAto;
                $model->reducStdAto[$row["SCHREGNO"]][$scoolStdKey]["S_MONTH_TOU"] = $sMonth2;
                $model->reducStdAto[$row["SCHREGNO"]][$scoolStdKey]["E_MONTH_TOU"] = $eMonth2;
            }

            $calcAdjustmentMoney = 0;
            $setSakiGenmen = $genmenJugyouSaki;
            for ($i = 1; $i <= 2; $i++) {
                $setNumerator = ($i == 1) ? 1 : 3;

                $calcSchoolMoney = ($jugyouryou / 4 * $setNumerator) - (($row["COUNTRY_MONEY_".$i] + $row["COUNTRY_ADD_MONEY_".$i]) * 12 / 4 * $setNumerator);

                $calcAdjustmentMoney += $calcSchoolMoney;
            }
            $setAtoGenmen = $row["SCOOL_DIV_LAST_1"] ? $row["SCOOL_DIV_LAST_1"] : 0;
            if ($calcAdjustmentMoney > 0) {
                if ($calcAdjustmentMoney < $setAtoGenmen) {
                    $setAtoGenmen = $calcAdjustmentMoney;
                }
                $calcAdjustmentMoney = 0;
            } else {
                $calcAdjustmentMoney = -1 * $calcAdjustmentMoney;
                $setSakiGenmen = $model->slipPlan[$row["SLIP_NO"]]["TOTAL_JUGYOU"];
                $setAtoGenmen = 0;
            }
            //調整金
            $setRow["ADJUSTMENT_MONEY"] = number_format($calcAdjustmentMoney);

            //減免
            $setRow["SCOOL_DIV_FIRST_1"]    = ($row["REDUCTION_JUGYOURYOU"]) ? number_format($setSakiGenmen) : 0;
            $setRow["SCOOL_DIV_LAST_1"]     = ($row["SCOOL_DIV_LAST_1"]) ? number_format($setAtoGenmen) : 0;

            //換算用
            knjCreateHidden($objForm, "SCOOL_DIV_FIRST_SAKI_1_{$row["SLIP_NO"]}", $setFirstSaki1);
            knjCreateHidden($objForm, "SCOOL_DIV_FIRST_ATO_1_{$row["SLIP_NO"]}", $setFirstAto1);
            knjCreateHidden($objForm, "SCOOL_DIV_LAST_SAKI_1_{$row["SLIP_NO"]}", $setLastSaki1);
            knjCreateHidden($objForm, "SCOOL_DIV_LAST_ATO_1_{$row["SLIP_NO"]}", $setLastAto1);

            //更新用
            knjCreateHidden($objForm, "SCOOL_DIV_FIRST_1_{$row["SLIP_NO"]}", $row["REDUCTION_JUGYOURYOU"]);
            knjCreateHidden($objForm, "SCOOL_DIV_LAST_1_{$row["SLIP_NO"]}", $row["SCOOL_DIV_LAST_1"]);

            //減免決定1
            $checked = $row["REDUC_SCHOOL_FLG_1"] == "1" ? "checked=checked" : "";
            $disabled = "";
            $extra = "{$checked} {$disabled} id='REDUC_SCHOOL_FLG_1_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
            $setRow["REDUC_SCHOOL_FLG_1"] = knjCreateCheckBox($objForm, "REDUC_SCHOOL_FLG_1[]", $row["SLIP_NO"], $extra);

            //減免決定2
            $checked = $row["REDUC_SCHOOL_FLG_2"] == "1" ? "checked=checked" : "";
            $disabled = "";
            $extra = "{$checked} {$disabled} id='REDUC_SCHOOL_FLG_2_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
            $setRow["REDUC_SCHOOL_FLG_2"] = knjCreateCheckBox($objForm, "REDUC_SCHOOL_FLG_2[]", $row["SLIP_NO"], $extra);

            //備考
            $extra = "onblur=\"chkByte(this)\"";
            $setRow["REDUC_REMARK"] = knjCreateTextBox($objForm, $row["REMARK"], "REDUC_REMARK[]", "21", "10", $extra);

            $arg["data"][] = $setRow;
            $arg["data2"][] = $setRow;

            $dataCnt++;
        }
        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('cancel');\"";
        $arg["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_close"] = knjCreateBtn($objForm, "btn_close", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "hiddenSchoolKind", $model->schoolKindAry[$model->search["GRADE"]]);

        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp727Form1.html", $arg);
    }
}
?>
