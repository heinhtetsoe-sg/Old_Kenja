<?php

require_once('for_php7.php');

class knjp721Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("", "POST", "knjp721index.php", "", "");
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["WindowWidth"] = $model->windowWidth - 50;
        $arg["titleWindowWidth"] = $model->windowWidth - 491;
        $arg["valWindowWidth"] = $model->windowWidth - 474;
        $arg["valWindowHeight"] = $model->windowHeight - 200;
        $arg["tcolWindowHeight"] = $model->windowHeight - 217;
        $arg["setWidth"] = 3200;
        $resizeFlg = $model->cmd == "cmdStart" ? true : false;

        //対象年度
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $arg["THIS_YEAR"] = CTRL_YEAR."年";
        $arg["LAST_YEAR"] = (CTRL_YEAR - 1)."年";

        //学年
        $model->schoolKindAry = array();
        $result = $db->query(knjp721Query::selectQueryGrade($model));
        $opt1 = $opt2 = array();
        $opt2[] = array("label" => "　　　　", "value" => "00-000");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[$row["GRADE"]] = array("label"  => $row["GRADE"],
                                         "value"  => $row["GRADE"]);
            $model->schoolKindAry[$row["GRADE"]] = $row["SCHOOL_KIND"];
            if (!isset($model->search["GRADE"])) {
                $model->search["GRADE"] = $row["GRADE"];
            }
            if ($row["GRADE"] == $model->search["GRADE"]) {
                $opt2[] = array("label" => $row["HR_NAMEABBV"] ,
                                "value" => $row["GRADE"] ."-" .$row["HR_CLASS"]);
            }
        }

        if ($model->cmd == "maingrade") {
            $model->search["HR_CLASS"] = "00-000";
            $model->cmd = "main";
        }

        //学年
        $extra = "onchange=\"btn_submit('maingrade');\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->search["GRADE"], $opt1, $extra, 1);
        //hidden
        knjCreateHidden($objForm, "GRADE_SEND", $model->search["GRADE"]);

        $model->mstLastYear = $model->search["GRADE"] == "01" ? CTRL_YEAR : CTRL_YEAR - 1;

        //年組
        $extra = "onchange=\"btn_submit('mainclass');\"";
        $arg["HR_CLASS"] = knjCreateCombo($objForm, "HR_CLASS", $model->search["HR_CLASS"], $opt2, $extra, 1);
        //hidden
        knjCreateHidden($objForm, "HR_CLASS_SEND", $model->search["HR_CLASS"]);

        if ($model->search["HR_CLASS"] && $model->search["HR_CLASS"] != "00-000") {
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
            //補助額1
            $extra = "onclick=\"checkAll(this)\"";
            $arg["PREFALL_1"] = knjCreateCheckBox($objForm, "PREFALL_1", "1", $extra);
            //補助額2
            $extra = "onclick=\"checkAll(this)\"";
            $arg["PREFALL_2"] = knjCreateCheckBox($objForm, "PREFALL_2", "1", $extra);
            //減免額1
            $extra = "onclick=\"checkAll(this)\"";
            $arg["REDUC_SCHOOL_ALL1"] = knjCreateCheckBox($objForm, "REDUC_SCHOOL_ALL1", "1", $extra);
            //減免額2
            $extra = "onclick=\"checkAll(this)\"";
            $arg["REDUC_SCHOOL_ALL2"] = knjCreateCheckBox($objForm, "REDUC_SCHOOL_ALL2", "1", $extra);

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

            //府県確定月1
            $extra = "";
            $arg["PREF_DEC_MONTH1"] = knjCreateCombo($objForm, "PREF_DEC_MONTH1", $model->decMonth["PREF_DEC_MONTH1"], $optMonth, $extra, 1);

            //府県確定月2
            $extra = "";
            $arg["PREF_DEC_MONTH2"] = knjCreateCombo($objForm, "PREF_DEC_MONTH2", $model->decMonth["PREF_DEC_MONTH2"], $optMonth, $extra, 1);

            //生徒授業料
            $model->slipPlan = array();
            $query = knjp721Query::getSlipPlan($model);
            $slipPlanResult = $db->query($query);
            while ($row = $slipPlanResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->slipPlan[$row["SLIP_NO"]][$row["COLLECT_L_CD"].'_'.$row["COLLECT_M_CD"]] = array("DIV"      => $row["GAKUNOKIN_DIV"].':'.$row["REDUCTION_DIV"].':'.$row["IS_REDUCTION_SCHOOL"],
                                                                                                         "MONEY"    => $row["MONEY"],
                                                                                                         "KANSAN"   => "");
            }
            $slipPlanResult->free();

            //支援金MST
            $model->countryMst = array();
            $query = knjp721Query::countryMstMonty($model);
            $countyrMstResult = $db->query($query);
            while ($row = $countyrMstResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->countryMst[] = $row;
            }
            $countyrMstResult->free();

            //支援金加算MST
            $model->countryAddMst = array();
            $query = knjp721Query::countryAddMstMonty($model);
            $countyrAddMstResult = $db->query($query);
            while ($row = $countyrAddMstResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->countryAddMst[] = $row;
            }
            $countyrAddMstResult->free();

            //支援ランク
            $model->countryRank = array();
            $query = knjp721Query::getNameMstSql($model, "P001");
            $countryRankResult = $db->query($query);
            $optCountryRank[] = array("VALUE" => "", "LABEL" => "");
            while ($row = $countryRankResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->countryRank[$row["VALUE"]] = $row["LABEL"];
                $optCountryRank[] = array("value" => $row["VALUE"], "label" => $row["LABEL"]);
            }
            $countryRankResult->free();

            //府県補助金マスタ
            $model->prefGradeMst = array();
            $query = knjp721Query::getPrefGradeMst($model);
            $prefGradeMstResult = $db->query($query);
            while ($row = $prefGradeMstResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->prefGradeMst[$row["PREFECTURESCD"]] = $row;
            }
            $prefGradeMstResult->free();

            //補助金MST
            $model->reductionMst = array();
            $query = knjp721Query::getReductionMst($model);
            $reductionMstResult = $db->query($query);
            while ($row = $reductionMstResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->reductionMst[$row["PREFECTURESCD"]][] = $row;
            }
            $reductionMstResult->free();

            //補助ランク
            $model->prefRank = array();
            $query = knjp721Query::getNameMstSql($model, "P002");
            $prefRankResult = $db->query($query);
            $optPrefRank[] = array("VALUE" => "", "LABEL" => "");
            while ($row = $prefRankResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->prefRank[$row["VALUE"]] = $row["LABEL"];
                $optPrefRank[] = array("value" => $row["VALUE"], "label" => $row["LABEL"]);
            }
            $prefRankResult->free();

            //生徒減免
            $model->reducStdSaki = $model->reducStdAto = array();
            $query = knjp721Query::getReductionStd($model);
            $reducStdResult = $db->query($query);
            while ($row = $reducStdResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["REDUCTION_TIMING"] == '1') {
                    //先
                    $model->reducStdSaki[$row["SCHREGNO"]][] = $row;
                } elseif ($row["REDUCTION_TIMING"] == '2') {
                    //後
                    $model->reducStdAto[$row["SCHREGNO"]][] = $row;
                }
            }
            $reducStdResult->free();

            //一覧取得
            $query = knjp721Query::selectQuery($model);
            $result = $db->query($query);
            $model->slipno = array();
            $model->slipnoPref = array();

            $dataCnt = 0;
            $dataArray = array();
            $schregCnt = array();
            $model->slipSchreg = array();
            $model->slipSchoolStd = array();
            $countryBaseFlg1 = $countryBaseFlg2 = array();
            $countryAddFlg1 = $countryAddFlg2 = array();
            $prefFlg1 = $prefFlg2 = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataArray[] = $row;
                $schregCnt[$row["SCHREGNO"]]++;
                $model->slipSchreg[$row["SLIP_NO"]] = $row["SCHREGNO"];
                $model->slipSchoolStd[$row["SLIP_NO"]]["REDUCTION_JUGYOURYOU1"] = "";
                $model->slipSchoolStd[$row["SLIP_NO"]]["REDUCTION_JUGYOURYOU2"] = "";
            }

            foreach ($dataArray as $key => $row) {
                $model->slipno[] = $row["SLIP_NO"];
                $model->slipnoPref[$row["SLIP_NO"]] = $row["G_PREF_CD"];
                @array_walk($row, "htmlspecialchars_array");

                //hidden
                knjCreateHidden($objForm, "G_PREF_CD_{$row["SLIP_NO"]}", $row["G_PREF_CD"]);

                $setRow = array();
                if ($befSchregNo != $row["SCHREGNO"]) {
                    if ($schregCnt[$row["SCHREGNO"]] > 1) {
                        $setRow["LEFT_ROWSPAN"] = "rowspan={$schregCnt[$row["SCHREGNO"]]}";
                    }

                    //年組番
                    $setRow["HR_NAMEABBV"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];

                    //氏名
                    $setRow["NAME_SHOW"] = $row["NAME_SHOW"];

                    //学籍番号
                    $setRow["SCHREGNO"] = $row["SCHREGNO"];
                }
                $befSchregNo = $row["SCHREGNO"];

                //再計算対象（全チェック）
                $extra = "onclick=\"checkAll2(this)\"";
                $arg["CALC_FLG_ALL"] = knjCreateCheckBox($objForm, "CALC_FLG_ALL", "1", $extra);

                //入金済み
                $setRow["SLIP_NO_BGCOLOR"] = ($row["PAID_FLG1"] == "1" && $row["PAID_FLG2"] == "1") ? "#ffa500" : "#ffffff";
                $setRow["PAID_FLG1"] = knjCreateHidden($objForm, "PAID_FLG1_{$row["SLIP_NO"]}", $row["PAID_FLG1"]);
                $setRow["PAID_FLG2"] = knjCreateHidden($objForm, "PAID_FLG2_{$row["SLIP_NO"]}", $row["PAID_FLG2"]);

                $setRow["PAID1_BGCOLOR"] = ($row["PAID_FLG1"] == "1") ? "#ffa500" : "#ffffff";
                $setRow["PAID2_BGCOLOR"] = ($row["PAID_FLG2"] == "1") ? "#ffa500" : "#ffffff";

                $disabledPaid1 = ($row["PAID_FLG1"] == "1") ? true : false;
                $disabledPaid2 = ($row["PAID_FLG2"] == "1") ? true : false;

                knjCreateHidden($objForm, "SCH_PAID_FLG1_{$row["SLIP_NO"]}", $row["SCH_PAID_FLG1"]);
                knjCreateHidden($objForm, "SCH_PAID_FLG2_{$row["SLIP_NO"]}", $row["SCH_PAID_FLG2"]);

                //確定済み
                $disabled1 = $disabled2 = false;
                $countryZumiList = $prefZumiList = "";
                foreach (array("COUNTRY_BASE_FLG_1", "COUNTRY_BASE_FLG_2", "COUNTRY_ADD_FLG_1", "COUNTRY_ADD_FLG_2", "REDUC_SCHOOL_FLG_1", "REDUC_SCHOOL_FLG_2") as $zumiKey) {
                    if ($row[$zumiKey] == "1" || $row["PAID_FLG".substr($zumiKey, -1)] == "1") {
                        if (substr($zumiKey, -1) == '1') {
                            $disabled1 = true;
                        }
                        if (substr($zumiKey, -1) == '2') {
                            $disabled2 = true;
                        }
                        $countryZumiList .= (strlen($countryZumiList) ? ',' : '').$zumiKey;
                    }
                }
                foreach (array("PREF_FLG_1", "PREF_FLG_2") as $zumiKey) {
                    if ($row[$zumiKey] == "1" || $row["PAID_FLG".substr($zumiKey, -1)] == "1") {
                        if (substr($zumiKey, -1) == '1') {
                            $disabled1 = true;
                        }
                        if (substr($zumiKey, -1) == '2') {
                            $disabled2 = true;
                        }
                        $prefZumiList .= (strlen($prefZumiList) ? ',' : '').$zumiKey;
                    }
                }

                knjCreateHidden($objForm, "COUNTRY_ZUMI_{$row["SLIP_NO"]}", $countryZumiList);
                knjCreateHidden($objForm, "PREF_ZUMI_{$row["SLIP_NO"]}", $prefZumiList);

                //前期＆後期が確定済みor入金済みのとき、再計算は使用不可
                $disabled = (($disabled1 || $disabledPaid1) && ($disabled2 || $disabledPaid2)) ? "disabled" : "";

                //再計算対象
                $checked = in_array($row["SLIP_NO"], $model->calcFlg) ? "checked" : "";
                $extra = " {$checked} {$disabled} id='CALC_FLG_{$row["SLIP_NO"]}' ";
                $setRow["CALC_FLG"] = knjCreateCheckBox($objForm, "CALC_FLG[]", $row["SLIP_NO"], $extra);

                //伝票番号
                $setRow["SLIP_NO"] = $row["SLIP_NO"];

                //都道府県
                $setRow["G_PREF"] = $row["G_PREF"];

                knjCreateHidden($objForm, "REFER_YEAR_DIV1_{$row["SLIP_NO"]}", $model->prefGradeMst[$row["G_PREF_CD"]]["REFER_YEAR_DIV1"]);
                knjCreateHidden($objForm, "REFER_YEAR_DIV2_{$row["SLIP_NO"]}", $model->prefGradeMst[$row["G_PREF_CD"]]["REFER_YEAR_DIV2"]);
                knjCreateHidden($objForm, "REFER_YEAR_DIV_FLG");

                //所得割額1
                if ($row["COUNTRY_ADD_FLG_1"] == 1
                    || $row["COUNTRY_BASE_FLG_1"] == "1"
                    || $row["PREF_FLG_1"] == "1"
                    || $row["REDUC_SCHOOL_FLG_1"] == "1"
                    || $row["SCH_PAID_FLG1"] == "1"
                ) {
                    $color = "#999999";
                    $readMoney = " readOnly ";
                } else {
                    $color = "#000000";
                    $readMoney = "";
                }
                knjCreateHidden($objForm, "SET_REDUC_INCOME1_{$row["SLIP_NO"]}", $row["REDUC_INCOME_1"]);
                $extra = " readOnly id='REDUC_INCOME_1_{$row["SLIP_NO"]}' style=\"text-align:right;color:#999999\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '1');\" ";
                $setRow["REDUC_INCOME_1"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_1"], "REDUC_INCOME_1[]", "10", "8", $extra);
                //市町村所得割1
                $extra = "{$readMoney} id='REDUC_INCOME_1_1_{$row["SLIP_NO"]}' style=\"text-align:right;color:{$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"setSumIncome('1', '{$row["SLIP_NO"]}');\"";
                $setRow["REDUC_INCOME_1_1"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_1_1"], "REDUC_INCOME_1_1[]", "10", "8", $extra);
                //都道府県所得割1
                $extra = "{$readMoney} id='REDUC_INCOME_1_2_{$row["SLIP_NO"]}' style=\"text-align:right;color:{$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"setSumIncome('1', '{$row["SLIP_NO"]}');\"";
                $setRow["REDUC_INCOME_1_2"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_1_2"], "REDUC_INCOME_1_2[]", "10", "8", $extra);
                //市町村所得割2
                $extra = "{$readMoney} id='REDUC_INCOME_1_3_{$row["SLIP_NO"]}' style=\"text-align:right;color:{$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"setSumIncome('1', '{$row["SLIP_NO"]}');\"";
                $setRow["REDUC_INCOME_1_3"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_1_3"], "REDUC_INCOME_1_3[]", "10", "8", $extra);
                //都道府県所得割2
                $extra = "{$readMoney} id='REDUC_INCOME_1_4_{$row["SLIP_NO"]}' style=\"text-align:right;color:{$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"setSumIncome('1', '{$row["SLIP_NO"]}');\"";
                $setRow["REDUC_INCOME_1_4"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_1_4"], "REDUC_INCOME_1_4[]", "10", "8", $extra);

                //所得割額2
                if ($row["COUNTRY_ADD_FLG_2"] == 1
                    || $row["COUNTRY_BASE_FLG_2"] == "1"
                    || $row["PREF_FLG_2"] == "1"
                    || $row["REDUC_SCHOOL_FLG_2"] == "1"
                    || $row["SCH_PAID_FLG2"] == "1"
                ) {
                    $color = "#999999";
                    $readMoney = " readOnly ";
                } else {
                    $color = "#000000";
                    $readMoney = "";
                }
                knjCreateHidden($objForm, "SET_REDUC_INCOME2_{$row["SLIP_NO"]}", $row["REDUC_INCOME_2"]);
                $extra = " readOnly id='REDUC_INCOME_2_{$row["SLIP_NO"]}' style=\"text-align:right;color:#999999\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '2');\" ";
                $setRow["REDUC_INCOME_2"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_2"], "REDUC_INCOME_2[]", "10", "8", $extra);
                //市町村所得割1
                $extra = "{$readMoney} id='REDUC_INCOME_2_1_{$row["SLIP_NO"]}' style=\"text-align:right;color:{$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"setSumIncome('2', '{$row["SLIP_NO"]}');\"";
                $setRow["REDUC_INCOME_2_1"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_2_1"], "REDUC_INCOME_2_1[]", "10", "8", $extra);
                //都道府県所得割1
                $extra = "{$readMoney} id='REDUC_INCOME_2_2_{$row["SLIP_NO"]}' style=\"text-align:right;color:{$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"setSumIncome('2', '{$row["SLIP_NO"]}');\"";
                $setRow["REDUC_INCOME_2_2"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_2_2"], "REDUC_INCOME_2_2[]", "10", "8", $extra);
                //市町村所得割2
                $extra = "{$readMoney} id='REDUC_INCOME_2_3_{$row["SLIP_NO"]}' style=\"text-align:right;color:{$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"setSumIncome('2', '{$row["SLIP_NO"]}');\"";
                $setRow["REDUC_INCOME_2_3"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_2_3"], "REDUC_INCOME_2_3[]", "10", "8", $extra);
                //都道府県所得割2
                $extra = "{$readMoney} id='REDUC_INCOME_2_4_{$row["SLIP_NO"]}' style=\"text-align:right;color:{$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"setSumIncome('2', '{$row["SLIP_NO"]}');\"";
                $setRow["REDUC_INCOME_2_4"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_2_4"], "REDUC_INCOME_2_4[]", "10", "8", $extra);

                //減免先
                $jugyouSaki = array();
                $countryJugyouryou1 = $countryJugyouryou2 = 0;
                $setGenmenSaki = 0;
                if (is_array($model->slipPlan[$row["SLIP_NO"]])) {
                    foreach ($model->slipPlan[$row["SLIP_NO"]] as $slipPlanKey => $slipPlanVal) {
                        list($gakunokin_div, $reduction_div, $is_reduction_school) = explode(':', $slipPlanVal["DIV"]);
                        $flg = ($gakunokin_div == '1') ? 1 : 2;
                        if ($is_reduction_school == '1') {
                            if (is_array($model->reducStdSaki[$row["SCHREGNO"]])) {
                                foreach ($model->reducStdSaki[$row["SCHREGNO"]] as $reducKey => $reducVal) {
                                    list($jugyou, $genmenJugyou) = knjp721Model::setReductionStd($reducVal, $slipPlanVal["MONEY"], "saki");
                                    $slipPlanVal["MONEY"] = $jugyou;
                                    $model->slipPlan[$row["SLIP_NO"]][$slipPlanKey]["KANSAN"] = $jugyou;
                                    $setGenmenSaki += $genmenJugyou;
                                }
                            }
                        }
                        if ($reduction_div == '1') {
                            ${"countryJugyouryou".$flg} += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                        }
                        //授業料、授業料等をそれぞれ合算
                        $jugyouSaki[$flg] += $slipPlanVal["MONEY"];
                    }
                }

                //支援金用授業料
                $maxCountryJugyouryou1 = ($countryJugyouryou1 + $countryJugyouryou2) > 0 ? ($countryJugyouryou1 + $countryJugyouryou2) / 4 : 0;
                $maxCountryJugyouryou2 = ($countryJugyouryou1 + $countryJugyouryou2) > 0 ? ($countryJugyouryou1 + $countryJugyouryou2) / 4 * 3 : 0;

                //減免先換算後の授業料をセット
                $showJugyou = $jugyouSaki[1];
                $setRow["JUGYOURYOU"] = (is_numeric($showJugyou)) ? number_format($showJugyou) : "";

                //減免先換算後の授業料等をセット
                $showJugyouTou = $jugyouSaki[2];
                $setRow["JUGYOURYOUTOU"] = (is_numeric($showJugyouTou)) ? number_format($showJugyouTou) : "";

                //標準授業料
                $schoolFeeFlg = $model->prefGradeMst[$row["G_PREF_CD"]]["STANDARD_SCHOOL_FEE"];
                $schoolFee = $model->prefGradeMst[$row["G_PREF_CD"]]["STANDARD_SCHOOL_FEE"] ? $model->prefGradeMst[$row["G_PREF_CD"]]["STANDARD_SCHOOL_FEE"] : 0;
                knjCreateHidden($objForm, "STANDARD_SCHOOL_FEE_{$row["SLIP_NO"]}", $schoolFee);

                //再計算
                if ($model->cmd == 'calc' && in_array($row["SLIP_NO"], $model->calcFlg)) {
                    $calcRow = $model->getCalcModel($db, $row);
                    foreach ($calcRow as $ckey => $cval) {
                        $row[$ckey] = $cval;
                    }
                }

                //合計額
                $countryTotalMoney1 = $countryTotalMoney2 = 0;
                $prefTotalMoney = 0;
                /**********/
                /* 支援金 */
                /**********/

                //REDUCTION_SEQ_1
                $extra = "id='REDUCTION_SEQ_1_{$row["SLIP_NO"]}' style=\"display:none\"";
                $setRow["REDUCTION_SEQ_1"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_1"], "REDUCTION_SEQ_1[]", "", "", $extra);

                //基準額
                if ($countryDispCd == "1") {
                    $countryTotalMoney1 += ($row["COUNTRY_MONEY_1"]);
                    $countryTotalMoney2 += ($row["COUNTRY_MONEY_2"]);
                } else {
                    //初期値セット
                    if (!$row["REDUCTION_SCHREGNO"] && !$row["COUNTRY_RANK_1"] && !$row["COUNTRY_RANK_2"]) {
                        $row["COUNTRY_RANK_1"] = $row["COUNTRY_RANK_2"] = 'XX';
                        $row["COUNTRY_MONEY_1"] = $row["COUNTRY_MONEY_2"] = $row["COUNTRY_ADD_MONEY_1"] = $row["COUNTRY_ADD_MONEY_2"] = 0;
                    }
                    $setRow["COUNTRY_DISP_1"] = (is_numeric($row["COUNTRY_MONEY_1"])) ? number_format($row["COUNTRY_MONEY_1"]) : "";
                    $countryTotalMoney1 += ($row["COUNTRY_MONEY_1"] * 3);
                    $setRow["COUNTRY_DISP_2"] = (is_numeric($row["COUNTRY_MONEY_2"])) ? number_format($row["COUNTRY_MONEY_2"]) : "";
                    $countryTotalMoney2 += ($row["COUNTRY_MONEY_2"] * 9);
                }

                //支援ランク1
                $extra = " id='COUNTRY_RANK_1_{$row["SLIP_NO"]}' onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '1');\" ";
                $setRow["COUNTRY_RANK_1"] = knjCreateCombo($objForm, "COUNTRY_RANK_1[]", $row["COUNTRY_RANK_1"], $optCountryRank, $extra, 1);

                //基準額1
                $color = ($row["COUNTRY_BASE_FLG_1"] == "1") ? "#999999" : "#000000";
                $readOnly = ($row["COUNTRY_BASE_FLG_1"] == "1") ? " readOnly=\"readOnly\" " : "";
                $extra = "id='COUNTRY_MONEY_1_{$row["SLIP_NO"]}' style=\"text-align:right; color:{$color}; display:none;\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '1');\"";
                $setRow["COUNTRY_MONEY_1"] = knjCreateTextBox($objForm, $row["COUNTRY_MONEY_1"], "COUNTRY_MONEY_1[]", "6", "6", $extra.$readOnly);

                //確定（基準額1）
                $checked = $row["COUNTRY_BASE_FLG_1"] == "1" ? "checked=checked" : "";
                $disabled = "";
                $extra = "{$checked} {$disabled} id='COUNTRY_BASE_FLG_1_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
                $setRow["COUNTRY_BASE_FLG_1"] = knjCreateCheckBox($objForm, "COUNTRY_BASE_FLG_1[]", $row["SLIP_NO"], $extra);
                if ($row["COUNTRY_BASE_FLG_1"] == "1") {
                    $countryBaseFlg1[] = $row["SLIP_NO"];
                }

                //支援ランク2
                $extra = " id='COUNTRY_RANK_2_{$row["SLIP_NO"]}' onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '2');\" ";
                $setRow["COUNTRY_RANK_2"] = knjCreateCombo($objForm, "COUNTRY_RANK_2[]", $row["COUNTRY_RANK_2"], $optCountryRank, $extra, 1);

                //基準額2
                $color = ($row["COUNTRY_BASE_FLG_2"] == "1") ? "#999999" : "#000000";
                $readOnly = ($row["COUNTRY_BASE_FLG_2"] == "1") ? " readOnly=\"readOnly\" " : "";
                $extra = "id='COUNTRY_MONEY_2_{$row["SLIP_NO"]}' style=\"text-align:right; color:{$color}; display:none;\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '2');\"";
                $setRow["COUNTRY_MONEY_2"] = knjCreateTextBox($objForm, $row["COUNTRY_MONEY_2"], "COUNTRY_MONEY_2[]", "6", "6", $extra.$readOnly);

                //確定（基準額2）
                $checked = $row["COUNTRY_BASE_FLG_2"] == "1" ? "checked=checked" : "";
                $disabled = "";
                $extra = "{$checked} {$disabled} id='COUNTRY_BASE_FLG_2_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
                $setRow["COUNTRY_BASE_FLG_2"] = knjCreateCheckBox($objForm, "COUNTRY_BASE_FLG_2[]", $row["SLIP_NO"], $extra);
                if ($row["COUNTRY_BASE_FLG_2"] == "1") {
                    $countryBaseFlg2[] = $row["SLIP_NO"];
                }

                //加算額
                if ($countryDispCd == "1") {
                    $countryTotalMoney1 += ($row["COUNTRY_ADD_MONEY_1"]);
                    $countryTotalMoney2 += ($row["COUNTRY_ADD_MONEY_2"]);
                } else {
                    $setRow["COUNTRY_ADD_DISP_1"] = (is_numeric($row["COUNTRY_ADD_MONEY_1"])) ? number_format($row["COUNTRY_ADD_MONEY_1"]) : "";
                    $countryTotalMoney1 += ($row["COUNTRY_ADD_MONEY_1"] * 3);
                    $setRow["COUNTRY_ADD_DISP_2"] = (is_numeric($row["COUNTRY_ADD_MONEY_2"])) ? number_format($row["COUNTRY_ADD_MONEY_2"]) : "";
                    $countryTotalMoney2 += ($row["COUNTRY_ADD_MONEY_2"] * 9);
                }

                //加算額1
                $color = ($row["COUNTRY_ADD_FLG_1"] == "1") ? "#999999" : "#000000";
                $readOnly = ($row["COUNTRY_ADD_FLG_1"] == "1") ? " readOnly=\"readOnly\" " : "";
                $extra = "id='COUNTRY_ADD_MONEY_1_{$row["SLIP_NO"]}' style=\"text-align:right; color:{$color}; display:none;\" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '1');\"";
                $setRow["COUNTRY_ADD_MONEY_1"] = knjCreateTextBox($objForm, $row["COUNTRY_ADD_MONEY_1"], "COUNTRY_ADD_MONEY_1[]", "6", "6", $extra.$readOnly);

                //確定（加算額1）
                $checked = $row["COUNTRY_ADD_FLG_1"] == "1" ? "checked=checked" : "";
                $disabled = "";
                $extra = "{$checked} {$disabled} id='COUNTRY_ADD_FLG_1_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
                $setRow["COUNTRY_ADD_FLG_1"] = knjCreateCheckBox($objForm, "COUNTRY_ADD_FLG_1[]", $row["SLIP_NO"], $extra);
                if ($row["COUNTRY_ADD_FLG_1"] == "1") {
                    $countryAddFlg1[] = $row["SLIP_NO"];
                }

                //加算額2
                $color = ($row["COUNTRY_ADD_FLG_1"] == "1") ? "#999999" : "#000000";
                $readOnly = ($row["COUNTRY_ADD_FLG_1"] == "1") ? " readOnly=\"readOnly\" " : "";
                $extra = "id='COUNTRY_ADD_MONEY_2_{$row["SLIP_NO"]}' style=\"text-align:right; color:{$color}; display:none;\" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '2');\"";
                $setRow["COUNTRY_ADD_MONEY_2"] = knjCreateTextBox($objForm, $row["COUNTRY_ADD_MONEY_2"], "COUNTRY_ADD_MONEY_2[]", "6", "6", $extra.$readOnly);

                //確定（加算額2）
                $checked = $row["COUNTRY_ADD_FLG_2"] == "1" ? "checked=checked" : "";
                $disabled = "";
                $extra = "{$checked} {$disabled} id='COUNTRY_ADD_FLG_2_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
                $setRow["COUNTRY_ADD_FLG_2"] = knjCreateCheckBox($objForm, "COUNTRY_ADD_FLG_2[]", $row["SLIP_NO"], $extra);
                if ($row["COUNTRY_ADD_FLG_2"] == "1") {
                    $countryAddFlg2[] = $row["SLIP_NO"];
                }

                //支援額合計
                $countryTotalMoney1 = min($countryTotalMoney1, $maxCountryJugyouryou1);
                $countryTotalMoney2 = min($countryTotalMoney2, $maxCountryJugyouryou2);
                $countryTotalMoney = $countryTotalMoney1 + $countryTotalMoney2;
                $setRow["COUNTRY_TOTAL_MONEY"] = number_format($countryTotalMoney);

                /**********/
                /* 補助額 */
                /**********/

                //補助額
                if ($prefDispCd != "1") {
                    //初期値セット
                    if (!$row["REDUCTION_SCHREGNO"] && !$row["PREF_RANK_1"] && !$row["PREF_RANK_2"]) {
                        $row["PREF_RANK_1"] = $row["PREF_RANK_2"] = 'XX';
                        $row["PREF_MONEY_1"] = $row["PREF_MONEY_2"] = 0;
                    }
                    //補助額1
                    $setRow["PREF_DISP_1"] = (is_numeric($row["PREF_MONEY_1"])) ? number_format($row["PREF_MONEY_1"]) : "";
                    //補助額2
                    $setRow["PREF_DISP_2"] = (is_numeric($row["PREF_MONEY_2"])) ? number_format($row["PREF_MONEY_2"]) : "";
                }

                //補助ランク1
                $extra = " id='PREF_RANK_1_{$row["SLIP_NO"]}' onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '1');\" ";
                $setRow["PREF_RANK_1"] = knjCreateCombo($objForm, "PREF_RANK_1[]", $row["PREF_RANK_1"], $optPrefRank, $extra, 1);

                //補助額1
                $color = ($row["PREF_FLG_1"] == "1") ? "#999999" : "#000000";
                $readOnly = ($row["PREF_FLG_1"] == "1") ? " readOnly=\"readOnly\" " : "";
                $extra = "id='PREF_MONEY_1_{$row["SLIP_NO"]}' style=\"text-align:right; color:{$color}; display:none;\" onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '1');\"";
                $setRow["PREF_MONEY_1"] = knjCreateTextBox($objForm, $row["PREF_MONEY_1"], "PREF_MONEY_1[]", "6", "6", $extra.$readOnly);

                //補助額確定1
                $checked = $row["PREF_FLG_1"] == "1" ? "checked=checked" : "";
                $disabled = "";
                $extra = "{$checked} {$disabled} id='PREF_FLG_1_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
                $setRow["PREF_FLG_1"] = knjCreateCheckBox($objForm, "PREF_FLG_1[]", $row["SLIP_NO"], $extra);
                if ($row["PREF_FLG_1"] == "1") {
                    $prefFlg1[] = $row["SLIP_NO"];
                }

                //補助ランク2
                $extra = " id='PREF_RANK_2_{$row["SLIP_NO"]}' onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '2');\" ";
                $setRow["PREF_RANK_2"] = knjCreateCombo($objForm, "PREF_RANK_2[]", $row["PREF_RANK_2"], $optPrefRank, $extra, 1);

                //補助額2
                $color = ($row["PREF_FLG_2"] == "1") ? "#999999" : "#000000";
                $readOnly = ($row["PREF_FLG_2"] == "1") ? " readOnly=\"readOnly\" " : "";
                $extra = "id='PREF_MONEY_2_{$row["SLIP_NO"]}' style=\"text-align:right; color:{$color}; display:none;\" onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '2');\"";
                $setRow["PREF_MONEY_2"] = knjCreateTextBox($objForm, $row["PREF_MONEY_2"], "PREF_MONEY_2[]", "6", "6", $extra.$readOnly);

                //補助額確定2
                $checked = $row["PREF_FLG_2"] == "1" ? "checked=checked" : "";
                $disabled = "";
                $extra = "{$checked} {$disabled} id='PREF_FLG_2_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
                $setRow["PREF_FLG_2"] = knjCreateCheckBox($objForm, "PREF_FLG_2[]", $row["SLIP_NO"], $extra);
                if ($row["PREF_FLG_2"] == "1") {
                    $prefFlg2[] = $row["SLIP_NO"];
                }

                //補助額合計
                $prefTotalMoney = $row["PREF_MONEY_1"] + $row["PREF_MONEY_2"];
                $setRow["PREF_TOTAL_MONEY"] = number_format($prefTotalMoney);

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

                //減免後
                if (!is_array($model->reducStdAto[$row["SCHREGNO"]])) {
                    $model->reducStdAto[$row["SCHREGNO"]] = array();
                }

                $contryJugyouryou1 = $contryJugyouryou2 = 0;
                $prefOnlyJugyouryou1 = $prefOnlyJugyouryou2 = 0;
                $setGenmenAto = 0;
                if (is_array($model->slipPlan[$row["SLIP_NO"]])) {
                    foreach ($model->slipPlan[$row["SLIP_NO"]] as $slipPlanKey => $slipPlanVal) {
                        list($gakunokin_div, $reduction_div, $is_reduction_school) = explode(':', $slipPlanVal["DIV"]);
                        $flg = ($gakunokin_div == '1') ? 1 : 2;

                        if ($reduction_div == '1') {
                            ${"contryJugyouryou".$flg} += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                        } elseif ($reduction_div == '2') {
                            ${"prefOnlyJugyouryou".$flg} += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                        }

                        if ($is_reduction_school == '1') {
                            if (is_array($model->reducStdAto[$row["SCHREGNO"]])) {
                                foreach ($model->reducStdAto[$row["SCHREGNO"]] as $reducKey => $reducVal) {
                                    list($jugyou, $genmenJugyou) = $model->setReductionStd($reducVal, $slipPlanVal["MONEY"], "ato");
                                    $slipPlanVal["MONEY"] = $jugyou;
                                    $setGenmenAto += $genmenJugyou;
                                }
                            }
                        }
                    }
                }

                //補助金用授業料、授業料等
                for ($flg = 1; $flg <= 2; $flg++) {
                    $tmpPrefJugyouryou = ${"contryJugyouryou".$flg} + ${"prefOnlyJugyouryou".$flg};
                    ${"prefJugyouryou".$flg} = ($tmpPrefJugyouryou > 0) ? $tmpPrefJugyouryou : 0;
                }

                $setMoneyArray = array("PREF_MONEY"         => $prefJugyouryou1,
                                       "PREF_MONEY_TOU"     => $prefJugyouryou2);

                $moneyDiv1 = $moneyDiv2 = "";
                $redMoney1 = $redMoney2 = array();
                for ($i = 1; $i <= 2; $i++) {
                    list($retMoney, $hoge, $moneyDiv, $burdenChargeFlg) = $model->setReductionMst($model->reductionMst[$row["G_PREF_CD"]], $row["REDUC_INCOME_".$i], $i, $setMoneyArray, 'rank', $row["PREF_RANK_".$i]);

                    ${"moneyDiv".$i} = $moneyDiv;
                    knjCreateHidden($objForm, "MONEY_DIV{$i}_{$row["SLIP_NO"]}", $moneyDiv);

                    ${"redMoney".$i} = $retMoney;
                    knjCreateHidden($objForm, "REDUCTIONMONEY{$i}_{$row["SLIP_NO"]}", $retMoney);

                    ${"burdenChargeFlg".$i} = $burdenChargeFlg;
                    knjCreateHidden($objForm, "BURDEN_CHARGE_FLG{$i}_{$row["SLIP_NO"]}", $burdenChargeFlg);
                }

                //再計算
                if ($model->cmd == 'calc' && in_array($row["SLIP_NO"], $model->calcFlg)) {
                    //減免先の値を加算しセット
                    $row["REDUCTION_JUGYOURYOU1"]   += ($setGenmenSaki) ? $setGenmenSaki / 4 : 0;
                    $row["REDUCTION_JUGYOURYOU2"]   += ($setGenmenSaki) ? $setGenmenSaki / 4 * 3 : 0;
                }

                //減免がない（＝初めて開いた）場合、減免の値をセット
                if ($row["REDUCTION_JUGYOURYOU1"] == 0 && $row["REDUCTION_JUGYOURYOU2"] == 0) {
                    $row["REDUCTION_JUGYOURYOU1"]   = ($setGenmenSaki) ? $setGenmenSaki / 4 : 0;
                    $row["REDUCTION_JUGYOURYOU2"]   = ($setGenmenSaki) ? $setGenmenSaki / 4 * 3 : 0;
                    if (!$row["REDUCTION_SCHREGNO"]) {
                        $row["REDUCTION_JUGYOURYOU1"]  += ($setGenmenAto) ? $setGenmenAto / 4 : 0;
                        $row["REDUCTION_JUGYOURYOU2"]  += ($setGenmenAto) ? $setGenmenAto / 4 * 3 : 0;
                    }
                }

                //減免
                $setRow["REDUCTION_JUGYOURYOU1"]    = ($row["REDUCTION_JUGYOURYOU1"]) ? number_format($row["REDUCTION_JUGYOURYOU1"]) : 0;
                $setRow["REDUCTION_JUGYOURYOU2"]    = ($row["REDUCTION_JUGYOURYOU2"]) ? number_format($row["REDUCTION_JUGYOURYOU2"]) : 0;

                //負担金
                $calcBurdenCharge = $row["TOTAL_BURDEN_CHARGE"] > 0 ? $row["TOTAL_BURDEN_CHARGE"] : 0;
                $setRow["BURDEN_CHARGE"] = number_format($calcBurdenCharge);

                //調整金(計算方法が未定のため固定で0セット）
                $calcAdjustmentMoney = 0;
                $setRow["ADJUSTMENT_MONEY"] = number_format($calcAdjustmentMoney);

                //換算用
                knjCreateHidden($objForm, "REDUCTION_JUGYOURYOU_SAKI_{$row["SLIP_NO"]}", $setGenmenSaki);
                knjCreateHidden($objForm, "REDUCTION_JUGYOURYOU_ATO1_{$row["SLIP_NO"]}", $setGenmenAto ? $setGenmenAto / 4 : 0);
                knjCreateHidden($objForm, "REDUCTION_JUGYOURYOU_ATO2_{$row["SLIP_NO"]}", $setGenmenAto ? $setGenmenAto / 4 * 3 : 0);

                knjCreateHidden($objForm, "JUGYOURYOU_{$row["SLIP_NO"]}", $jugyouSaki[1]);
                knjCreateHidden($objForm, "JUGYOURYOUTOU_{$row["SLIP_NO"]}", $jugyouSaki[2]);

                knjCreateHidden($objForm, "MAX_COUNTRY_JUGYOURYOU1_{$row["SLIP_NO"]}", $maxCountryJugyouryou1);
                knjCreateHidden($objForm, "MAX_COUNTRY_JUGYOURYOU2_{$row["SLIP_NO"]}", $maxCountryJugyouryou2);

                //更新用
                knjCreateHidden($objForm, "REDUCTION_JUGYOURYOU1_{$row["SLIP_NO"]}", $row["REDUCTION_JUGYOURYOU1"]);
                knjCreateHidden($objForm, "REDUCTION_JUGYOURYOU2_{$row["SLIP_NO"]}", $row["REDUCTION_JUGYOURYOU2"]);
                knjCreateHidden($objForm, "BURDEN_CHARGE1_{$row["SLIP_NO"]}", $row["BURDEN_CHARGE1"]);
                knjCreateHidden($objForm, "BURDEN_CHARGE2_{$row["SLIP_NO"]}", $row["BURDEN_CHARGE2"]);
                knjCreateHidden($objForm, "TOTAL_BURDEN_CHARGE_{$row["SLIP_NO"]}", $row["TOTAL_BURDEN_CHARGE"]);

                //確定（減免1）
                $checked = $row["REDUC_SCHOOL_FLG_1"] == "1" ? "checked=checked" : "";
                $disabled = "";
                $extra = "{$checked} {$disabled} id='REDUC_SCHOOL_FLG_1_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
                $setRow["REDUC_SCHOOL_FLG_1"] = knjCreateCheckBox($objForm, "REDUC_SCHOOL_FLG_1[]", $row["SLIP_NO"], $extra);

                //確定（減免2）
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
            /* hidden */
            /**********/
            //チェック用
            knjCreateHidden($objForm, "TMP_COUNTRY_BASE_FLG_1", implode(',', $countryBaseFlg1));
            knjCreateHidden($objForm, "TMP_COUNTRY_BASE_FLG_2", implode(',', $countryBaseFlg2));
            knjCreateHidden($objForm, "TMP_COUNTRY_ADD_FLG_1", implode(',', $countryAddFlg1));
            knjCreateHidden($objForm, "TMP_COUNTRY_ADD_FLG_2", implode(',', $countryAddFlg2));
            knjCreateHidden($objForm, "TMP_PREF_FLG_1", implode(',', $prefFlg1));
            knjCreateHidden($objForm, "TMP_PREF_FLG_2", implode(',', $prefFlg2));
        }

        //使用不可
        $arg["javascript"] = "optDisabled()";

        /**********/
        /* ボタン */
        /**********/
        //使用不可
        $disable = ($model->search["HR_CLASS"] && $model->search["HR_CLASS"] != "00-000") ? "" : " disabled";

        //更新
        $extra = "onclick=\"return btn_submit('update');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('cancel');\"".$disable;
        $arg["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_close"] = knjCreateBtn($objForm, "btn_close", "終 了", $extra);

        //再計算
        $extra = "onclick=\"return btn_submit('calc');\"".$disable;
        $arg["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "再計算", $extra);

        //ＣＳＶ出力
        $extra  = ($model->search["GRADE"]) ? "" : "disabled";
        $extra .= " onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "hiddenSchoolKind", $model->schoolKindAry[$model->search["GRADE"]]);

        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp721Form1.html", $arg);
    }
}
