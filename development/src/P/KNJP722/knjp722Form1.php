<?php

require_once('for_php7.php');

class knjp722Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("", "POST", "knjp722index.php", "", "");

        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["WindowWidth"] = $model->windowWidth - 50;
        $arg["titleWindowWidth"] = $model->windowWidth - 491;
        $arg["valWindowWidth"] = $model->windowWidth - 474;
        $arg["valWindowHeight"] = $model->windowHeight - 200;
        $arg["tcolWindowHeight"] = $model->windowHeight - 217;
        $arg["setWidth"] = 1200;
        $resizeFlg = $model->cmd == "cmdStart" ? true : false;

        //対象年度
        $arg["CTRL_YEAR"] = CTRL_YEAR;

        //学年
        $model->schoolKindAry = array();
        $result = $db->query(knjp722Query::selectQueryGrade($model));
        $opt1 = $opt2 = array();
        $opt2[] = array("label" => "　　　　", "value" => "00-000");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[$row["GRADE"]] = array("label"  => $row["GRADE"],
                                         "value"  => $row["GRADE"]);
            $model->schoolKindAry[$row["GRADE"]] = $row["SCHOOL_KIND"];
            if (!isset($model->search["GRADE"])) $model->search["GRADE"] = $row["GRADE"];
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
            //補助額1
            $extra = "onclick=\"checkAll(this)\"";
            $arg["PREFALL"] = knjCreateCheckBox($objForm, "PREFALL", "1", $extra);
            //減免額1
            $extra = "onclick=\"checkAll(this)\"";
            $arg["REDUC_SCHOOL_ALL"] = knjCreateCheckBox($objForm, "REDUC_SCHOOL_ALL", "1", $extra);

            //特殊フラグ
            $optP711 = array();
            $value_flg = false;
            $query = knjp722Query::selectQuerySpecailCode();
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

            //府県確定月
            $extra = "";
            $arg["PREF_DEC_MONTH"] = knjCreateCombo($objForm, "PREF_DEC_MONTH", $model->decMonth["PREF_DEC_MONTH"], $optMonth, $extra, 1);

            //生徒入学金
            $model->slipPlan = array();
            $query = knjp722Query::getSlipPlan($model);
            $slipPlanResult = $db->query($query);
            while ($row = $slipPlanResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->slipPlan[$row["SLIP_NO"]][$row["COLLECT_L_CD"].'_'.$row["COLLECT_M_CD"]] = array("DIV"      => $row["REDUCTION_DIV"].':'.$row["IS_REDUCTION_SCHOOL"],
                                                                                                         "MONEY"    => $row["MONEY"],
                                                                                                         "KANSAN"   => "");
            }
            $slipPlanResult->free();

            //補助金MST
            $model->reductionMst = array();
            $query = knjp722Query::getReductionMst($model);
            $reductionMstResult = $db->query($query);
            while ($row = $reductionMstResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->reductionMst[$row["PREFECTURESCD"]][] = $row;
            }
            $reductionMstResult->free();

            //補助ランク
            $model->prefRank = array();
            $query = knjp722Query::getNameMstSql($model, "P002");
            $prefRankResult = $db->query($query);
            $optPrefRank[] = array("VALUE" => "", "LABEL" => "");
            while ($row = $prefRankResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->prefRank[$row["VALUE"]] = $row["LABEL"];
                $optPrefRank[] = array("value" => $row["VALUE"], "label" => $row["LABEL"]);
            }
            $prefRankResult->free();

            //生徒減免
            $model->reducStdSaki = $model->reducStdAto = array();
            $query = knjp722Query::getReductionStd($model);
            $reducStdResult = $db->query($query);
            while ($row = $reducStdResult->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["REDUCTION_TIMING"] == '1') {
                    //先
                    $model->reducStdSaki[$row["SCHREGNO"]][] = $row;
                } else if ($row["REDUCTION_TIMING"] == '2') {
                    //後
                    $model->reducStdAto[$row["SCHREGNO"]][] = $row;
                }
            }
            $reducStdResult->free();

            //一覧取得
            $query = knjp722Query::selectQuery($model);
            $result = $db->query($query);
            $model->slipno = array();
            $model->slipnoPref = array();

            $dataCnt = 0;
            $dataArray = array();
            $schregCnt = array();
            $model->slipSchreg = array();
            $model->slipSchoolStd = array();
            $prefFlg1 = $prefFlg2 = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataArray[] = $row;
                $schregCnt[$row["SCHREGNO"]]++;
                $model->slipSchreg[$row["SLIP_NO"]] = $row["SCHREGNO"];
                $model->slipSchoolStd[$row["SLIP_NO"]] = array("SAKI1" => "", "ATO1" => "", "SAKI2" => "", "ATO2" => "");
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

                //確定済み
                $disabled = "";
                $prefZumiList = "";
                if ($row["PREF_FLG"] == "1") {
                    $disabled = "disabled";
                    $prefZumiList = "PREF_FLG";
                }
                knjCreateHidden($objForm, "PREF_ZUMI_{$row["SLIP_NO"]}", $prefZumiList);

                //再計算対象
                $checked = in_array($row["SLIP_NO"], $model->calcFlg) ? "checked" : "";
                $extra = " {$checked} {$disabled} id='CALC_FLG_{$row["SLIP_NO"]}' ";
                $setRow["CALC_FLG"] = knjCreateCheckBox($objForm, "CALC_FLG[]", $row["SLIP_NO"], $extra);

                //伝票番号
                $setRow["SLIP_NO"] = $row["SLIP_NO"];

                //入金済み
                $setRow["SLIP_NO_BGCOLOR"] = ($row["PAID_FLG"]) ? "#ffa500" : "#ffffff";
                knjCreateHidden($objForm, "PAID_FLG_{$row["SLIP_NO"]}", $row["PAID_FLG"]);

                //都道府県
                $setRow["G_PREF"] = $row["G_PREF"];

                //所得割額1
                if ($row["PREF_FLG"] == "1") {
                    $color = "#999999";
                    $readMoney = " readOnly ";
                } else {
                    $color = "#000000";
                    $readMoney = "";
                }
                knjCreateHidden($objForm, "SET_REDUC_INCOME_{$row["SLIP_NO"]}", $row["REDUC_INCOME"]);
                $extra = " readOnly id='REDUC_INCOME_{$row["SLIP_NO"]}' style=\"text-align:right;color:#999999\"";
                $setRow["REDUC_INCOME"] = knjCreateTextBox($objForm, $row["REDUC_INCOME"], "REDUC_INCOME[]", "10", "8", $extra);

                //減免先
                $nyugakukinSaki = 0;
                $setGenmenSaki = 0;
                if (is_array($model->slipPlan[$row["SLIP_NO"]])) {
                    foreach ($model->slipPlan[$row["SLIP_NO"]] as $slipPlanKey => $slipPlanVal) {
                        list ($reduction_div, $is_reduction_school) = explode(':', $slipPlanVal["DIV"]);

                        if ($is_reduction_school == '1') {
                            if (is_array($model->reducStdSaki[$row["SCHREGNO"]])) {
                                foreach ($model->reducStdSaki[$row["SCHREGNO"]] as $reducKey => $reducVal) {
                                    list($nyugakukin, $genmenNyugakukin) = knjp722Model::setReductionStd($reducVal, $slipPlanVal["MONEY"], "saki");
                                    $slipPlanVal["MONEY"] = $nyugakukin;
                                    $model->slipPlan[$row["SLIP_NO"]][$slipPlanKey]["KANSAN"] = $nyugakukin;
                                    $setGenmenSaki += $genmenNyugakukin;
                                }
                            }
                        }
                        //入学金をそれぞれ合算
                        $nyugakukinSaki += $slipPlanVal["MONEY"];
                    }
                }

                //減免先換算後の入学金をセット
                $setRow["NYUGAKUKIN"] = (is_numeric($nyugakukinSaki)) ? number_format($nyugakukinSaki) : "";

                //再計算
                if ($model->cmd == 'calc' && in_array($row["SLIP_NO"], $model->calcFlg)) {
                    $calcRow = $model->getCalcModel($db, $row);
                    foreach ($calcRow as $ckey => $cval) {
                        $row[$ckey] = $cval;
                    }
                }

                //合計額
                $prefTotalMoney = 0;

                /**********/
                /* 補助額 */
                /**********/

                //REDUCTION_SEQ
                $extra = "id='REDUCTION_SEQ_{$row["SLIP_NO"]}' style=\"display:none\"";
                $setRow["REDUCTION_SEQ"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ"], "REDUCTION_SEQ[]", "", "", $extra);

                //特殊コード：入力可/不可の判定用
                list($prefDispCd, $dispSetumei) = preg_split("/:/", $row["PREF_TEXT_DISP"]);
                $prefTextNone2 = $prefDispCd == "1" ? "" : "display:none";

                //補助額
                if ($prefDispCd != "1") {
                    //初期値セット
                    if (!$row["REDUCTION_SCHREGNO"] && !$row["PREF_RANK"]) {
                        $row["PREF_RANK"] = 'XX';
                        $row["PREF_MONEY"] = 0;
                    }
                    //補助額1
                    $setRow["PREF_DISP"] = (is_numeric($row["PREF_MONEY"])) ? number_format($row["PREF_MONEY"]) : "";
                }

                //補助ランク1
                $extra = " id='PREF_RANK_{$row["SLIP_NO"]}' onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '1');\" ";
                $setRow["PREF_RANK"] = knjCreateCombo($objForm, "PREF_RANK[]", $row["PREF_RANK"], $optPrefRank, $extra, 1);

                //補助額1
                $color = ($row["PREF_FLG"] == "1") ? "#999999" : "#000000";
                $readOnly = ($row["PREF_FLG"] == "1") ? " readOnly=\"readOnly\" " : "";
                $extra = "id='PREF_MONEY_{$row["SLIP_NO"]}' style=\"text-align:right; color:{$color}; {$prefTextNone2}\" onChange=\"chgIncome(this, '{$row["SLIP_NO"]}', '1');\"";
                $setRow["PREF_MONEY"] = knjCreateTextBox($objForm, $row["PREF_MONEY"], "PREF_MONEY[]", "6", "6", $extra.$readOnly);

                //補助額確定
                $checked = $row["PREF_FLG"] == "1" ? "checked=checked" : "";
                $disabled = "";
                $extra = "{$checked} {$disabled} id='PREF_FLG_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
                $setRow["PREF_FLG"] = knjCreateCheckBox($objForm, "PREF_FLG[]", $row["SLIP_NO"], $extra);
                if ($row["PREF_FLG"] == "1") $prefFlg1[] = $row["SLIP_NO"];

                //特殊フラグ（補助額）
                $row["PREF_RARE_CASE_CD"] = $row["PREF_RARE_CASE_CD"] ? $row["PREF_RARE_CASE_CD"] : $optP711[0]["value"];
                $extra = " id='PREF_RARE_CASE_CD_{$row["SLIP_NO"]}' onChange=\"kick_chgIncome(this, '{$row["SLIP_NO"]}', 'PREF_')\"";
                $setRow["PREF_RARE_CASE_CD"] = knjCreateCombo($objForm, "PREF_RARE_CASE_CD[]", $row["PREF_RARE_CASE_CD"], $optP711, $extra, 1);

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

                $prefNyugakukin = 0;
                $setGenmenAto = 0;
                if (is_array($model->slipPlan[$row["SLIP_NO"]])) {
                    foreach ($model->slipPlan[$row["SLIP_NO"]] as $slipPlanKey => $slipPlanVal) {
                        list ($reduction_div, $is_reduction_school) = explode(':', $slipPlanVal["DIV"]);

                        if ($reduction_div == '3') {
                            $prefNyugakukin += ($slipPlanVal["KANSAN"]) ? $slipPlanVal["KANSAN"] : $slipPlanVal["MONEY"];
                        }

                        if ($is_reduction_school == '1') {
                            if (is_array($model->reducStdAto[$row["SCHREGNO"]])) {
                                foreach ($model->reducStdAto[$row["SCHREGNO"]] as $reducKey => $reducVal) {
                                    list($nyugakukin, $genmenNyugakukin) = $model->setReductionStd($reducVal, $slipPlanVal["MONEY"], "ato");
                                    $slipPlanVal["MONEY"] = $nyugakukin;
                                    $setGenmenAto += $genmenNyugakukin;
                                }
                            }
                        }
                    }
                }
                $setMoney = ($prefNyugakukin > 0) ? $prefNyugakukin : 0;
                list($retMoney, $hoge, $moneyDiv, $burdenChargeFlg, $prefSeq) = $model->setReductionMst($model->reductionMst[$row["G_PREF_CD"]], $row["REDUC_INCOME"], $setMoney, 'rank', $row["PREF_RANK"]);
                knjCreateHidden($objForm, "MONEY_DIV_{$row["SLIP_NO"]}", $moneyDiv);
                knjCreateHidden($objForm, "REDUCTIONMONEY_{$row["SLIP_NO"]}", $retMoney);

                //再計算
                if ($model->cmd == 'calc' && in_array($row["SLIP_NO"], $model->calcFlg)) {
                    //減免先の値を加算しセット
                    $row["REDUCTION_NYUGAKUKIN"]    += $setGenmenSaki;
                }

                //減免がない（＝初めて開いた）場合、減免先の値をセット
                if ($row["REDUCTION_NYUGAKUKIN"] == 0) {
                    $row["REDUCTION_NYUGAKUKIN"]    = $setGenmenSaki;
                    if (!$row["REDUCTION_SCHREGNO"]) {
                        $row["REDUCTION_NYUGAKUKIN"]   += $setGenmenAto;
                    }
                }

                //減免
                $setRow["REDUCTION_NYUGAKUKIN"]     = ($row["REDUCTION_NYUGAKUKIN"]) ? number_format($row["REDUCTION_NYUGAKUKIN"]) : 0;

                //負担金(計算方法が未定のため固定で0セット）
                $calcBurdenCharge = 0;
                $setRow["BURDEN_CHARGE"] = number_format($calcBurdenCharge);

                //調整金(計算方法が未定のため固定で0セット）
                $calcAdjustmentMoney = 0;
                $setRow["ADJUSTMENT_MONEY"] = number_format($calcAdjustmentMoney);

                //換算用
                knjCreateHidden($objForm, "REDUCTION_NYUGAKUKIN_SAKI_{$row["SLIP_NO"]}", $setGenmenSaki);
                knjCreateHidden($objForm, "NYUGAKUKIN_{$row["SLIP_NO"]}", $nyugakukinSaki);

                //更新用
                knjCreateHidden($objForm, "REDUCTION_NYUGAKUKIN_{$row["SLIP_NO"]}", $row["REDUCTION_NYUGAKUKIN"]);

                //確定（減免1）
                $checked = $row["REDUC_SCHOOL_FLG"] == "1" ? "checked=checked" : "";
                $disabled = "";
                $extra = "{$checked} {$disabled} id='REDUC_SCHOOL_FLG_{$row["SLIP_NO"]}' onclick=\"chkFlg(this,'{$row["SLIP_NO"]}')\"";
                $setRow["REDUC_SCHOOL_FLG"] = knjCreateCheckBox($objForm, "REDUC_SCHOOL_FLG[]", $row["SLIP_NO"], $extra);

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
            knjCreateHidden($objForm, "TMP_PREF_FLG", implode(',' ,$prefFlg1));
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

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "hiddenSchoolKind", $model->schoolKindAry[$model->search["GRADE"]]);

        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp722Form1.html", $arg);
    }
}
?>
