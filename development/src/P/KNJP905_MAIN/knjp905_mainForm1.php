<?php

require_once('for_php7.php');

class knjp905_mainForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp905_mainindex.php", "", "main");

        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit' && $model->cmd !== 'torikomi') {
            $Row = knjp905_mainQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        if (DEF_UPDATABLE == $model->auth) {
            $arg["updOk"] = "1";
        }

        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjp905_mainQuery::getCloseFlgData($model));

        //入金科目と決済の状況を取得(比較時に利用)
        if ($model->getRequestNo) {
            $model->getIncomeLMcd      = $db->getOne(knjp905_mainQuery::getLevyData($model, "INCOME_LM_CD"));
            $model->getApproval        = $db->getOne(knjp905_mainQuery::getLevyData($model, "APPROVAL"));
            $model->getCancel          = $db->getOne(knjp905_mainQuery::getLevyData($model, "CANCEL"));
        } else {
            $model->getIncomeLMcd      = "";
            $model->getApproval        = "";
            $model->getCancel          = "";
        }
        //別画面より遷移し、新規作成の場合
        if (!$Row["OUTGO_L_M_CD"]) {
            $Row["OUTGO_L_M_CD"] = $model->getOutgoLMcd;
        }
        //単独画面から表示する場合は、パラメータ用に値をセット
        if ($model->getKounyuRequestNo == "") {
            $model->getKounyuRequestNo = $Row["KOUNYU_NO"];
        }
        if ($model->getSekouRequestNo == "") {
            $model->getSekouRequestNo = $Row["SEKOU_NO"];
        }
        if ($model->getSeisanRequestNo == "") {
            $model->getSeisanRequestNo = $Row["SEISAN_NO"];
        }

        //精算票の場合は支払額を支出金額にセットする(精算票の支払額 >= 0かつデータがない場合)
        $model->getSiharaiGk = "";
        $model->getZanGk = "";
        if ($model->getSeisanRequestNo) {
            $model->getSiharaiGk = $db->getOne(knjp905_mainQuery::getSeisanGk($model, "SIHARAI_GK"));
            $model->getZanGk = $db->getOne(knjp905_mainQuery::getSeisanGk($model, "ZAN_GK"));
            if ($Row["REQUEST_GK"] == "") {
                $Row["REQUEST_GK"] = $model->getSiharaiGk;
            }
        }
        knjCreateHidden($objForm, "SEISAN_REQUEST_NO", $model->getSeisanRequestNo);
        knjCreateHidden($objForm, "SEISAN_SIHARAIGK", $model->getSiharaiGk);

        //表示項目
        //伝票番号
        $arg["data"]["REQUEST_NO"] = $model->getRequestNo;

        //各項目
        //収入科目
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjp905_mainQuery::getIncomeLDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["INCOME_LM_CD"], "INCOME_LM_CD", $extra, 1, "BLANK", $model);
        $model->selectedLCd = substr($Row["INCOME_LM_CD"], 0, 2);
        
        //収入残高を表示
        //収入高
        $getIncomeRequestGk = $db->getOne(knjp905_mainQuery::getIncomeSumRequestGk($model, $Row["INCOME_LM_CD"]));

        //支出高(入力伝票以外)
        $getOutgoRequestGk = $db->getOne(knjp905_mainQuery::getOutGoSumRequestGk($model, $Row));

        //収入残高を計算
        $model->field["TOTAL_ZAN_GK"] = $getIncomeRequestGk - ($getOutgoRequestGk + intval($Row["REQUEST_GK"]));
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_ZAN_GK"] = knjCreateTextBox($objForm, $model->field["TOTAL_ZAN_GK"], "TOTAL_ZAN_GK", 10, 10, $extra);

        //支出項目
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjp905_mainQuery::getLevyMDiv($model);
        $model->field["OUTGO_L_M_CD"] = ($model->field["OUTGO_L_M_CD"] == "") ? $Row["OUTGO_L_M_CD"] : $model->field["OUTGO_L_M_CD"];
        makeCombo($objForm, $arg, $db, $query, $Row["OUTGO_L_M_CD"], "OUTGO_L_M_CD", $extra, 1, "BLANK", $model);

        //支出伺い日
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$Row["REQUEST_DATE"]),"");

        $arg["LEVY_BUDGET_FLG"] = false;
        if ($model->Properties["LevyBudget"] == "1") {
            $arg["LEVY_BUDGET_FLG"] = true;

            //予算情報取得
            $budgetInfo = $db->getRow(knjp905_mainQuery::getBudgetInfo($model), DB_FETCHMODE_ASSOC);

            //予算額
            $model->field["BUDGET_TOTAL"] = $budgetInfo["BUDGET_TOTAL"];
            $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
            $arg["data"]["BUDGET_TOTAL"] = knjCreateTextBox($objForm, $model->field["BUDGET_TOTAL"], "BUDGET_TOTAL", 10, 10, $extra);

            //予算残高
            $model->field["BUDGET_ZAN"] = $budgetInfo["BUDGET_ZAN"];
            $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
            $arg["data"]["BUDGET_ZAN"] = knjCreateTextBox($objForm, $model->field["BUDGET_ZAN"], "BUDGET_ZAN", 10, 10, $extra);
        }
        $arg["DEFAULT_FLG"] = !$arg["LEVY_BUDGET_FLG"];

        //収入伺い日までの収入残高を表示
        //収入高
        $getIncomeRequestGk = $db->getOne(knjp905_mainQuery::getIncomeSumRequestGk($model, $Row["INCOME_LM_CD"], $Row["REQUEST_DATE"]));

        //支出高(入力伝票以外)
        $getOutgoRequestGk = $db->getOne(knjp905_mainQuery::getOutGoSumRequestGk($model, $Row, $Row["REQUEST_DATE"]));

        //収入残高を計算
        $model->field["TOTAL_ZAN_DATE_GK"] = $getIncomeRequestGk - ($getOutgoRequestGk + intval($Row["REQUEST_GK"]));
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_ZAN_DATE_GK"] = knjCreateTextBox($objForm, $model->field["TOTAL_ZAN_DATE_GK"], "TOTAL_ZAN_DATE_GK", 10, 10, $extra);

        //起案者
        $query = knjp905_mainQuery::getRequestStaff($model, $Row["REQUEST_STAFF"]);
        $setStaff = $db->getOne($query);
        $arg["data"]["REQUEST_STAFF"] = $setStaff;
        //伝票作成者
        knjCreateHidden($objForm, "REQUEST_STAFF", $Row["REQUEST_STAFF"]);

        //決議理由
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["REQUEST_REASON"] = knjCreateTextBox($objForm, $Row["REQUEST_REASON"], "REQUEST_REASON", 60, 60, $extra);

        //支出金額
        $arg["data"]["REQUEST_GK"] = $model->reqestGk = $Row["REQUEST_GK"];
        knjCreateHidden($objForm, "REQUEST_GK", $Row["REQUEST_GK"]);

        //摘要内容
        $model->sumTotalPrice = "";
        $model->sumTotalTax = "";
        $sumBudgetMoneyArr = array();
        $chkCntKoumoku = 0;
        for ($i = 1; $i <= $model->koumoku; $i++) {
            //通常表示
            if ($model->cmd !== 'edit' && $model->cmd !== 'torikomi') {
                //品名等
                $daitaiFlg = false;
                if ($model->Properties["LevyBudget"] == "1") {
                    $Row2 = $db->getRow(knjp905_mainQuery::getRow2($model, $i, $Row["OUTGO_L_M_S_CD".$i]), DB_FETCHMODE_ASSOC);
                    $daitaiFlg = ($Row2["BUDGET_L_M_S_CD"] != "") && ($Row["OUTGO_L_M_S_CD".$i] != $Row2["BUDGET_L_M_S_CD"]);

                    //代替文言作成
                    $budgetSName = trim($Row2["BUDGET_S_NAME"]);
                    $levySName   = trim($Row["LEVY_S_NAME".$i]);
                    $daitaiName = "{$budgetSName}の代替として<br>{$levySName}";
                }
                $dispLevySName = ($daitaiFlg) ? $daitaiName : $Row["LEVY_S_NAME".$i];
                $arg["data"]["LEVY_S_NAME".$i]     =  $dispLevySName;
                //単価
                $arg["data"]["COMMODITY_PRICE".$i] = $Row["COMMODITY_PRICE".$i];
                //税込
                $arg["data"]["IN_TAX".$i]          = ($Row["TOTAL_TAX".$i] == 0 && $Row["TOTAL_TAX".$i] != "") ? "レ": "";
                //数量
                $arg["data"]["COMMODITY_CNT".$i]   = $Row["COMMODITY_CNT".$i];
                //消費税
                $arg["data"]["TOTAL_TAX".$i]       = $Row["TOTAL_TAX".$i];
                //金額(税込み)
                $arg["data"]["TOTAL_PRICE".$i]     = $Row["TOTAL_PRICE".$i];
                if ($model->Properties["LevyBudget"] == "1") {
                    //予算額
                    $arg["data"]["BUDGET_MONEY".$i]    = $Row2["BUDGET_MONEY"];
                    //予算額合計
                    $sumBudgetMoneyArr[$Row2["BUDGET_L_M_S_CD"]] = $Row2["BUDGET_MONEY"];
                }
                //備考
                $arg["data"]["REMARK".$i]          = $Row["REMARK".$i];

                //消費税合計
                $model->sumTotalTax   += $Row["TOTAL_TAX".$i];
                //金額合計(税込み)
                $model->sumTotalPrice += $Row["TOTAL_PRICE".$i];
                //以下、サブミット時に表示するためhidden格納
                knjCreateHidden($objForm, "LEVY_S_NAME".$i,     $dispLevySName);
                knjCreateHidden($objForm, "COMMODITY_PRICE".$i, $Row["COMMODITY_PRICE".$i]);
                knjCreateHidden($objForm, "COMMODITY_CNT".$i,   $Row["COMMODITY_CNT".$i]);
                knjCreateHidden($objForm, "TOTAL_TAX".$i,       $Row["TOTAL_TAX".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE".$i,     $Row["TOTAL_PRICE".$i]);
                if ($model->Properties["LevyBudget"] == "1") {
                    knjCreateHidden($objForm, "BUDGET_MONEY".$i,    $Row2["BUDGET_MONEY"]);
                }
                knjCreateHidden($objForm, "REMARK".$i,          $Row["REMARK".$i]);
            //サブミット表示用
            } else {
                //品名等
                $arg["data"]["LEVY_S_NAME".$i]     = $model->setRow["LEVY_S_NAME".$i];
                //単価
                $arg["data"]["COMMODITY_PRICE".$i] = $model->setRow["COMMODITY_PRICE".$i];
                //税込
                $arg["data"]["IN_TAX".$i]          = ($model->setRow["TOTAL_TAX".$i] == 0 && $model->setRow["TOTAL_TAX".$i] != "") ? "レ": "";
                //数量
                $arg["data"]["COMMODITY_CNT".$i]   = $model->setRow["COMMODITY_CNT".$i];
                //消費税
                $arg["data"]["TOTAL_TAX".$i]       = $model->setRow["TOTAL_TAX".$i];
                //金額(税込み)
                $arg["data"]["TOTAL_PRICE".$i]     = $model->setRow["TOTAL_PRICE".$i];
                if ($model->Properties["LevyBudget"] == "1") {
                    //予算額
                    $arg["data"]["BUDGET_MONEY".$i]    = $model->setRow["BUDGET_MONEY".$i];
                    //予算合計
                    $sumBudgetMoneyArr[$Row2["BUDGET_L_M_S_CD"]] = $model->setRow["BUDGET_MONEY".$i];
                }
                //備考
                $arg["data"]["REMARK".$i]          = $model->setRow["REMARK".$i];

                //消費税合計
                $model->sumTotalTax += $model->setRow["TOTAL_TAX".$i];
                //金額合計(税込み)
                $model->sumTotalPrice += $model->setRow["TOTAL_PRICE".$i];
                //以下、サブミット時に表示するためhidden格納
                knjCreateHidden($objForm, "LEVY_S_NAME".$i,     $model->setRow["LEVY_S_NAME".$i]);
                knjCreateHidden($objForm, "COMMODITY_PRICE".$i, $model->setRow["COMMODITY_PRICE".$i]);
                knjCreateHidden($objForm, "COMMODITY_CNT".$i,   $model->setRow["COMMODITY_CNT".$i]);
                knjCreateHidden($objForm, "TOTAL_TAX".$i,       $model->setRow["TOTAL_TAX".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE".$i,     $model->setRow["TOTAL_PRICE".$i]);
                if ($model->Properties["LevyBudget"] == "1") {
                    knjCreateHidden($objForm, "BUDGET_MONEY".$i, $model->setRow["BUDGET_MONEY".$i]);
                }
                knjCreateHidden($objForm, "REMARK".$i,          $model->setRow["REMARK".$i]);
            }

            //行コピー用checkbox
            if ($arg["data"]["LEVY_S_NAME".$i] != '') {
                $extra = "id=\"COPY_CHECK{$i}\" class=\"changeColor\" data-name=\"COPY_CHECK{$i}\" ";
                $arg["data"]["COPY_CHECK".$i] = knjCreateCheckBox($objForm, "COPY_CHECK".$i, "1", $extra);
                $arg["data"]["COPY_CHECK".$i."_NAME"] = "COPY_CHECK{$i}";

                $chkCntKoumoku++;
            }
        }
        //行コピー時に使用
        //allCheck
        $extra = "id=\"COPY_CHECK_ALL\" onClick=\"chkAll(this);\"";
        $arg["data"]["COPY_CHECK_ALL"] = knjCreateCheckBox($objForm, "COPY_CHECK_ALL", "1", $extra);
        knjCreateHidden($objForm, "koumoku", $model->koumoku);
        knjCreateHidden($objForm, "chkCntKoumoku", $chkCntKoumoku);
        //コピーボタン
        if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0 || $chkCntKoumoku == 0) {
            $extra = " disabled";
        } else if (($model->cmd === 'edit' || $model->cmd === 'torikomi') && $model->getOutgoLMcd != "") {
            $extra = " disabled";
        } else {
            $extra = "onclick=\"return btn_submit('line_copy');\"";
        }
        $arg["button"]["btn_lineCopy"] = knjCreateBtn($objForm, "btn_lineCopy", "行コピー", $extra);

        //消費税
        $arg["data"]["SUM_TOTAL_TAX"] = $model->sumTotalTax;

        //総支出額
        $arg["data"]["SUM_TOTAL_PRICE_ALL"] = $model->sumTotalPrice;
        knjCreateHidden($objForm, "SUM_TOTAL_PRICE", $model->sumTotalPrice);

        if ($model->Properties["LevyBudget"] == "1") {
            //総予算額
            $arg["data"]["SUM_BUDGET_MONEY_ALL"] = array_sum($sumBudgetMoneyArr);
        }

        //状態表示
        if ($model->getApproval === '1' && $model->getCancel == "") {
            $arg["data"]["SET_STATUS"] = '<font color="red">決裁済み</font>';
        } else if ($model->getCancel === '1') {
            $arg["data"]["SET_STATUS"] = '<font color="red">キャンセル</font>';
        }

        //業者情報登録*********/
        //業者コード
        $extra = "";
        $query = knjp905_mainQuery::getTraderCd($model);
        makeCombo($objForm, $arg, $db, $query, $Row["TRADER_CD"], "TRADER_CD", $extra, 1, "BLANK", $model);
        if ($model->cmd === 'torikomi') {
            $traderRow = array();
            $traderRow = $db->getRow(knjp905_mainQuery::getTraderData($model), DB_FETCHMODE_ASSOC);
            $Row["TRADER_NAME"] = $traderRow["TRADER_NAME"];
            $Row["BANKCD"] = $traderRow["BANKCD"];
            $Row["BRANCHCD"] = $traderRow["BRANCHCD"];
            $Row["BANK_DEPOSIT_ITEM"] = $traderRow["BANK_DEPOSIT_ITEM"];
            $Row["BANK_ACCOUNTNO"] = $traderRow["BANK_ACCOUNTNO"];
            $Row["ACCOUNTNAME"] = $traderRow["ACCOUNTNAME"];
            $Row["ACCOUNTNAME_KANA"] = $traderRow["ACCOUNTNAME_KANA"];
            $Row["PAY_DIV"] = $traderRow["PAY_DIV"];
        }

        //決議理由
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["TRADER_NAME"] = knjCreateTextBox($objForm, $Row["TRADER_NAME"], "TRADER_NAME", 45, 120, $extra);

        //銀行コード
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjp905_mainQuery::getBankCd();
        makeCombo($objForm, $arg, $db, $query, $Row["BANKCD"], "BANKCD", $extra, 1, "BLANK", $model);

        //支店コード
        $extra = "";
        $query = knjp905_mainQuery::getBranchCd($Row["BANKCD"]);
        makeCombo($objForm, $arg, $db, $query, $Row["BRANCHCD"], "BRANCHCD", $extra, 1, "BLANK", $model);

        //口座種別
        $extra = "";
        $query = knjp905_mainQuery::getNameMst("G203");
        makeCombo($objForm, $arg, $db, $query, $Row["BANK_DEPOSIT_ITEM"], "BANK_DEPOSIT_ITEM", $extra, 1, "BLANK", $model);

        //口座番号
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BANK_ACCOUNTNO"] = knjCreateTextBox($objForm, $Row["BANK_ACCOUNTNO"], "BANK_ACCOUNTNO", 7, 7, $extra);

        //口座名義
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["ACCOUNTNAME"] = knjCreateTextBox($objForm, $Row["ACCOUNTNAME"], "ACCOUNTNAME", 45, 120, $extra);

        //口座名義カナ
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["ACCOUNTNAME_KANA"] = knjCreateTextBox($objForm, $Row["ACCOUNTNAME_KANA"], "ACCOUNTNAME_KANA", 45, 120, $extra);

        //支払方法
        $extra = "";
        $query = knjp905_mainQuery::getNameMst("G217");
        makeCombo($objForm, $arg, $db, $query, $Row["PAY_DIV"], "PAY_DIV", $extra, 1, "BLANK", $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $Row);

        //checkbox
        $extra = " id=\"CHANGE_CLASS\" ";
        if ($model->field["CHANGE_CLASS"] == "1") {
            $extra .= " checked ";
        }
        $arg["data"]["CHANGE_CLASS"] = knjCreateCheckBox($objForm, "CHANGE_CLASS", "1", $extra);

        //hidden作成
        makeHidden($objForm, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp905_mainForm1.html", $arg); 
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $Row) {
    //新規登録
    $flg = "1";
    $root = REQUESTROOT;
    $auth = AUTHORITY;

    //取込
    $extra = "onclick=\"return btn_submit('torikomi');\"";
    $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

    //印刷ボタンを作成する
    if ($model->getRequestNo == "") {
        $extra = " onclick=\"btn_error('new');\"";
    } else {
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    }
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //生徒振り分け設定
    for ($i = 1; $i <= $model->koumoku; $i++) {
        if ($model->getRequestNo == "") {
            $extra = " onclick=\"btn_error('new');\"";
        } else if (($model->cmd === 'edit' || $model->cmd === 'torikomi') && $model->getOutgoLMcd != "") {
            $extra = " onclick=\"btn_error('huriwake');\"";
        } else {
            $subdata  = "wopen('".REQUESTROOT."/P/KNJP905_SCHREG/knjp905_schregindex.php?cmd=main&SEND_AUTH={$model->auth}";
            $subdata .= "&SEND_PRGID=KNJP905_MAIN&SEND_OUTGO_L_CD={$Row["OUTGO_L_CD"]}&SEND_OUTGO_M_CD={$Row["OUTGO_M_CD"]}";
            $subdata .= "&SEND_SCHOOL_KIND={$Row["SCHOOL_KIND"]}&SEND_REQUEST_NO={$Row["REQUEST_NO"]}&SEND_YEAR={$Row["YEAR"]}&SEND_LINE_NO=".$i."";
            $subdata .= "&SEND_OUTGO_L_M_CD={$Row["OUTGO_L_M_CD"]}&SEND_INCOME_LM_CD={$Row["INCOME_LM_CD"]}";
            $subdata .= "&SEND_REQUEST_DATE={$Row["REQUEST_DATE"]}";
            $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
            $extra = " onclick=\"$subdata\"";
        }
        $arg["button"]["btn_schreg".$i] = knjCreateBtn($objForm, "btn_schreg".$i, "登録画面", $extra);
    }
    //更 新
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    //変更での更新
    } else if (($model->cmd === 'edit' || $model->cmd === 'torikomi') && $model->getRequestNo != "" && $model->getOutgoLMcd != "") {
        if ($model->getKounyuRequestNo == "" && $model->getSekouRequestNo == "" && $model->getSeisanRequestNo == "") {
            if (($Row["OUTGO_L_M_CD"] != $model->getOutgoLMcd) || $Row["INCOME_LM_CD"] != $model->getIncomeLMcd) {
                $extra = " onclick=\"return btn_submit('delete_update');\"";
            } else {
                $extra = " onclick=\"return btn_submit('update');\"";
            }
        } else {
            if ($Row["OUTGO_L_M_CD"] != $model->getOutgoLMcd) {
                $extra = " onclick=\"btn_error('chenge_error');\"";
            } else {
                if ($Row["INCOME_LM_CD"] != $model->getIncomeLMcd) {
                    $extra = " onclick=\"return btn_submit('delete_update');\"";
                } else {
                    $extra = " onclick=\"return btn_submit('update');\"";
                }
            }
        }
    //新規での更新
    } else if ($model->getRequestNo == "") {
        if ($model->getKounyuRequestNo == "" && $model->getSekouRequestNo == "" && $model->getSeisanRequestNo == "") {
            $extra = " onclick=\"return btn_submit('update');\"";
        } else {
            if ($Row["OUTGO_L_M_CD"] != $model->getOutgoLMcd) {
                $extra = " onclick=\"btn_error('chenge_error');\"";
            } else {
                $extra = " onclick=\"return btn_submit('update');\"";
            }
        }
    } else {
        $extra = " onclick=\"return btn_submit('update');\"";
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削 除
    if ($model->getApproval === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        $extra = " onclick=\"return btn_submit('delete');\"";
    }
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('cancel');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra, "reset");
    //決済画面
    if ($model->getRequestNo == "") {
        $extra = " onclick=\"btn_error('new');\"";
    } else if ($model->sumTotalPrice != $Row["REQUEST_GK"]) {
        $extra = " onclick=\"btn_error('kessai');\"";
    } else if ($model->getSeisanRequestNo != "" && $model->getSiharaiGk != $Row["REQUEST_GK"]) {
        $extra = " onclick=\"btn_error('seisan');\"";
    } else if (($model->cmd === 'edit' || $model->cmd === 'torikomi') && $model->getOutgoLMcd != "") {
        $extra = " onclick=\"btn_error('huriwake');\"";
    } else {
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP905_APPROVAL/knjp905_approvalindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJP905_MAIN&SEND_OUTGO_L_CD={$Row["OUTGO_L_CD"]}&SEND_OUTGO_M_CD={$Row["OUTGO_M_CD"]}";
        $subdata .= "&SEND_SCHOOL_KIND={$Row["SCHOOL_KIND"]}&SEND_REQUEST_NO={$Row["REQUEST_NO"]}&SEND_YEAR={$Row["YEAR"]}";
        $subdata .= "&SEND_OUTGO_L_M_CD={$Row["OUTGO_L_M_CD"]}&SEND_INCOME_LM_CD={$Row["INCOME_LM_CD"]}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
    }
    $arg["button"]["btn_kessai"] = knjCreateBtn($objForm, "btn_kessai", "決裁画面", $extra);

    //購入画面
    if ($model->getKounyuRequestNo != "" || $Row["KOUNYU_NO"] != "") {
        $arg["kounyu_hyouji"] = "1";
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP920_MAIN/knjp920_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJP905_MAIN&SEND_KOUNYU_L_CD={$model->getOutgoLcd}&SEND_KOUNYU_M_CD={$model->getOutgoMcd}";
        $subdata .= "&SEND_SCHOOL_KIND={$model->getSchoolKind}&SEND_OUTGO_REQUEST_NO={$model->getRequestNo}&SEND_YEAR={$model->getYear}&SEND_REQUEST_NO={$model->getKounyuRequestNo}";
        $subdata .= "&SEND_KOUNYU_L_M_CD={$model->getOutgoLMcd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
        $arg["button"]["btn_kounyu"] = knjCreateBtn($objForm, "btn_kounyu", "購入伺画面", $extra);
    }
    //施行画面
    if ($model->getSekouRequestNo != "" || $Row["SEKOU_NO"] != "") {
        $arg["sekou_hyouji"] = "1";
        if (!$model->getSekouRequestNo) {
            $Row["SEKOU_NO"] = $model->getKounyuRequestNo;
        }
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP930_MAIN/knjp930_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJP905_MAIN&SEND_SEKOU_L_CD={$model->getOutgoLcd}&SEND_SEKOU_M_CD={$model->getOutgoMcd}";
        $subdata .= "&SEND_SCHOOL_KIND={$model->getSchoolKind}&SEND_OUTGO_REQUEST_NO={$model->getRequestNo}&SEND_YEAR={$model->getYear}&SEND_REQUEST_NO={$model->getSekouRequestNo}";
        $subdata .= "&SEND_SEKOU_L_M_CD={$model->getOutgoLMcd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
        $arg["button"]["btn_sekou"] = knjCreateBtn($objForm, "btn_sekou", "施行伺画面", $extra);
    }
    //精算画面
    //決裁済みのときのみ
    if ($model->getSeisanRequestNo != "" || $Row["SEISAN_NO"] != "") {
        $arg["seisan_hyouji"] = "1";
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP940_MAIN/knjp940_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJP905_MAIN&SEND_SEISAN_L_CD={$model->getOutgoLcd}&SEND_SEISAN_M_CD={$model->getOutgoMcd}";
        $subdata .= "&SEND_SCHOOL_KIND={$model->getSchoolKind}&SEND_OUTGO_REQUEST_NO={$model->getRequestNo}&SEND_YEAR={$model->getYear}&SEND_REQUEST_NO={$model->getSeisanRequestNo}";
        $subdata .= "&SEND_SEISAN_L_M_CD={$model->getOutgoLMcd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
        $arg["button"]["btn_seisan"] = knjCreateBtn($objForm, "btn_seisan", "精算票画面", $extra);
    } 

    //手数料／備考入力
    if ($model->Properties["useLevyIraisyo"] == "1") {
        $extra  = "onclick=\"return btn_submit('tesuryo_bikou');\"";
        $arg["button"]["tesuryo_bikou"] = knjCreateBtn($objForm, "tesuryo_bikou", "手数料／備考入力", $extra);
    }

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJP952");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "REQUEST_NO", $model->getRequestNo);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SCHOOL_KIND", $model->getSchoolKind);
    knjCreateHidden($objForm, "GET_YEAR", $model->getYear);
    knjCreateHidden($objForm, "GET_OUTGO_L_CD", $model->getOutgoLcd);
    knjCreateHidden($objForm, "GET_OUTGO_M_CD", $model->getOutgoMcd);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "KNJP952_PrintStdNameMeisai", $model->Properties["KNJP952_PrintStdNameMeisai"]);
    knjCreateHidden($objForm, "KNJP952_PrintStdCntMeisai", $model->Properties["KNJP952_PrintStdCntMeisai"]);
    knjCreateHidden($objForm, "useLevyIraisyo", $model->Properties["useLevyIraisyo"]);
}
?>
