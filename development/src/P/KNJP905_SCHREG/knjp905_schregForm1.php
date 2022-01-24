<?php

require_once('for_php7.php');

class knjp905_schregForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit' && $model->cmd !== 'chgBudgetSDiv' && $model->cmd !== 'exec_main') {
            $Row = knjp905_schregQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjp905_schregQuery::getCloseFlgData($model));

        //返金フラグと返金用の入金科目、決済の状況取得(比較時に利用)
        if ($model->getOutgoLMcd) {
            $model->getCollectLMSGrpcd = $db->getOne(knjp905_schregQuery::getLevyData($model, "COLLECT"));
            $model->getApproval        = $db->getOne(knjp905_schregQuery::getLevyData($model, "APPROVAL"));
            $model->getCancel          = $db->getOne(knjp905_schregQuery::getLevyData($model, "CANCEL"));
        } else {
            $model->getCollectLMSGrpcd = "";
            $model->getApproval        = "";
            $model->getCancel          = "";
        }

        //支出科目
        $query = knjp905_schregQuery::getLevyMDiv($model);
        $arg["data"]["OUTGO_M_NAME"] = $db->getOne($query);

        //生徒、生徒以外切替
        $opt = array(1, 2);
        //初期値(データなしの場合)
        if (!$Row["WARIHURI_DIV"]) {
            $Row["WARIHURI_DIV"] = "1";
        }
        $model->field["WARIHURI_DIV"] = ($model->field["WARIHURI_DIV"] == "") ? $Row["WARIHURI_DIV"] : $model->field["WARIHURI_DIV"];
        $extra = array("id=\"WARIHURI_DIV1\" onclick=\"return btn_submit('knjp905_schreg');\"", "id=\"WARIHURI_DIV2\" onclick=\"return btn_submit('knjp905_schreg');\"");
        $radioArray = knjCreateRadio($objForm, "WARIHURI_DIV", $model->field["WARIHURI_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        $arg["data"]["GOUKEI_GK_NAME"] = '合計額(税抜き)';

        //年組番表示
        $extra  = "id=\"HR_CLASS_HYOUJI_FLG\"  onclick=\"return btn_submit('edit');\"";
        if ($model->field["HR_CLASS_HYOUJI_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["HR_CLASS_HYOUJI_FLG"] = knjCreateCheckBox($objForm, "HR_CLASS_HYOUJI_FLG", "1", $extra);
        //NO
        $arg["data"]["NO"] = $model->getLineNo;

        //支出細目名称取得
        $levySnames = $sep = "";
        $cmbCnt = 1;
        $query = knjp905_schregQuery::getLevySDiv($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $levySnames .= $sep.$row["NAME"];
            $sep = ":";
            $cmbCnt++;
        }
        knjCreateHidden($objForm, "LEVY_S_NAMES", $levySnames);

        //会計細目
        //支出細目
        $extra = " onchange=\"return btn_submit('edit');\"";
        $query = knjp905_schregQuery::getLevySDiv($model);
        $model->field["OUTGO_L_M_S_CD"] = ($model->field["OUTGO_L_M_S_CD"] == "") ? $Row["OUTGO_L_M_S_CD"] : $model->field["OUTGO_L_M_S_CD"];
        makeCombo($objForm, $arg, $db, $query, $Row["OUTGO_L_M_S_CD"], "OUTGO_L_M_S_CD", $extra, 1, "BLANK", $model);
        //テキスト
        $extra = "";
        $arg["data"]["OUTGO_NAME"] = knjCreateTextBox($objForm, $model->field["OUTGO_NAME"], "OUTGO_NAME", 40, 90, $extra);
        //max会計科目コード取得
        $model->maxScd = sprintf("%03d", $db->getOne(knjp905_schregQuery::getLevySDiv($model, "MAX")) + 1);
        $setLMScd      = "{$model->getOutgoLMcd}"."{$model->maxScd}" ;
        //button
        $extra = " onclick=\"return add('{$setLMScd}', '{$cmbCnt}');\"";
        $arg["button"]["btn_TXT_TO_CMB"] = knjCreateBtn($objForm, "btn_TXT_TO_CMB", "追加", $extra);

        //給付対象等、取得
        $query = knjp905_schregQuery::getLevySmstInfo($model, $Row["OUTGO_L_M_S_CD"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row["REPAY_DIV"] = $row["REPAY_DIV"];
            $Row["BENEFIT"]   = $row["BENEFIT"];
        }

        //radio (1:返金可 2:不可)
        $opt = array(1, 2);
        $Row["REPAY_DIV"] = ($Row["REPAY_DIV"] == "") ? "2" : $Row["REPAY_DIV"];
        $extra = array("id=\"REPAY_DIV1\"", "id=\"REPAY_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "REPAY_DIV", $Row["REPAY_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //給付対象使用するか
        if ($model->Properties["useBenefit"] == "1") {
            $arg["useBenefit"] = "1";

            //給付対象checkbox
            $checked = ($Row["BENEFIT"] == "1") ? " checked": "";
            $extra = "id=\"BENEFIT\"";
            $arg["data"]["BENEFIT"] = knjCreateCheckBox($objForm, "BENEFIT", "1", $extra.$checked);
        }

        if ($model->Properties["LevyBudget"] == "1" && $model->field["WARIHURI_DIV"] == "1") {
            $arg["LEVY_BUDGET_FLG"] = "1";

            //予算情報取得
            $budgetInfo = $db->getRow(knjp905_schregQuery::getBudgetInfo($model), DB_FETCHMODE_ASSOC);
            //予算明細情報取得
            $budgetMeisaiInfo = $db->getRow(knjp905_schregQuery::getBudgetMeisaiInfo($model), DB_FETCHMODE_ASSOC);

            //予算細目
            $query = knjp905_schregQuery::getBudgetSDiv($model);
            $Row["BUDGET_L_M_S_CD"] = ($model->cmd == "main" || $model->cmd == "edit") ? $db->getOne(knjp905_schregQuery::getBudgetMeisaiInfo($model)) : $model->field["BUDGET_L_M_S_CD"];
            $extra = " onchange=\"return btn_submit('chgBudgetSDiv');\" ";
            makeCombo($objForm, $arg, $db, $query, $Row["BUDGET_L_M_S_CD"], "BUDGET_L_M_S_CD", $extra, 1, "BLANK", $model);

            //予算額
            $model->field["BUDGET_TOTAL"] = $budgetInfo["BUDGET_TOTAL"];
            $arg["data"]["BUDGET_TOTAL"] = $model->field["BUDGET_TOTAL"];

            //予算残高
            $model->field["BUDGET_ZAN"] = $budgetInfo["BUDGET_ZAN"];
            $arg["data"]["BUDGET_ZAN"] =  $model->field["BUDGET_ZAN"];

            //予算から引くか否かのチェックボックス（チェックonで引かない）
            $extra = " id=\"NOT_MINUS_FLG\" ";
            $arg["data"]["NOT_MINUS_FLG"] = knjCreateCheckBox($objForm, "NOT_MINUS_FLG", "1", $extra);
        }

        //支出総額
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toNumberMinus(this.value);\"";
        $arg["data"]["COMMODITY_PRICE"] = knjCreateTextBox($objForm, $Row["COMMODITY_PRICE"], "COMMODITY_PRICE", 8, 8, $extra);

        //税込checkbox
        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit' && $model->cmd !== 'exec_main') {
            $checked = ($Row["TOTAL_TAX"] == 0 && strlen($Row["TOTAL_TAX"]) > 0) ? " checked": "";
        } else {
            $checked = $Row["IN_TAX"] == "1" ? " checked": "";
        }
        $extra = "id=\"IN_TAX\"";
        $arg["data"]["IN_TAX"] = knjCreateCheckBox($objForm, "IN_TAX", "1", $extra.$checked);

        //数量
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["COMMODITY_CNT"] = knjCreateTextBox($objForm, $Row["COMMODITY_CNT"], "COMMODITY_CNT", 8, 8, $extra);

        //合計金額(税抜き)
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_PRICE_ZEINUKI"] = knjCreateTextBox($objForm, $Row["TOTAL_PRICE_ZEINUKI"], "TOTAL_PRICE_ZEINUKI", 8, 8, $extra);

        //消費税
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_TAX"] = knjCreateTextBox($objForm, $Row["TOTAL_TAX"], "TOTAL_TAX", 8, 8, $extra);

        //合計金額(税込み)
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_PRICE"] = knjCreateTextBox($objForm, $Row["TOTAL_PRICE"], "TOTAL_PRICE", 8, 8, $extra);

        //生徒以外の場合
        if ($model->field["WARIHURI_DIV"] == "2") {
            $arg["WARIHURI_NG"] = "1";
            //請求書番号
            $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toAlphanumeric(this.value);\"";
            $arg["data"]["TRADER_SEIKYU_NO"] = knjCreateTextBox($objForm, $Row["TRADER_SEIKYU_NO"], "TRADER_SEIKYU_NO", 10, 10, $extra);

            //請求月
            $extra = "";
            $query = knjp905_schregQuery::getSeikyuMonth();
            makeCombo($objForm, $arg, $db, $query, $Row["SEIKYU_MONTH"], "SEIKYU_MONTH", $extra, 1, "BLANK", $model);
        }

        //摘要
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 21, 30, $extra);

        //生徒数
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["SCH_CNT"] = knjCreateTextBox($objForm, $Row["SCH_CNT"], "SCH_CNT", 8, 8, $extra);

        //生徒単価
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["SCH_PRICE"] = knjCreateTextBox($objForm, $Row["SCH_PRICE"], "SCH_PRICE", 8, 8, $extra);

        //端数
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["HASUU"] = knjCreateTextBox($objForm, $Row["HASUU"], "HASUU", 8, 8, $extra);

        //異動者除外チェックボックス
        $extra = " id=\"CHK_TRANSFER\" onchange=\"changeTransferDisp();\"";
        if ($model->field["CHK_TRANSFER"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["CHK_TRANSFER"] = knjCreateCheckBox($objForm, "CHK_TRANSFER", "1", $extra);

        //生徒の場合
        if ($model->field["WARIHURI_DIV"] == "1") {
            $arg["WARIHURI_OK"] = "1";
            //生徒一覧 OR 科目一覧リスト
            $opt_right = array();
            $opt_left  = array();
            $model->schList = array();
            $schLabelArr = array();

            // ラベルをセット（年組番、学籍番号）
            $query = knjp905_schregQuery::getSchno($model, "get");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schLabelArr[$row["VALUE"]] = array('GHANO' => $row["LABEL1"],
                                                    'SCHNO' => $row["LABEL2"]);
            }
            $query = knjp905_schregQuery::getSchno($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schLabelArr[$row["VALUE"]] = array('GHANO' => $row["LABEL1"],
                                                    'SCHNO' => $row["LABEL2"]);
            }
            $selectStudent = ($model->selectStudent != "") ? explode(",", $model->selectStudent): array();
            $selectStudentRight = ($model->selectStudentRight != "") ? explode(",", $model->selectStudentRight): array();

            for ($i = 0; $i < get_count($selectStudent); $i++) {
                //年組番表示
                if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
                    $setLabel = $schLabelArr[$selectStudent[$i]]["GHANO"];
                } else {
                    $setLabel = $schLabelArr[$selectStudent[$i]]["SCHNO"];
                }
                $opt_left[] = array('label' => $setLabel,
                                    'value' => $selectStudent[$i]);
                $model->schList[] = $selectStudent[$i];
            }

            for ($i = 0; $i < get_count($selectStudentRight); $i++) {
                
                //年組番表示
                if ($model->field["HR_CLASS_HYOUJI_FLG"] === '1') {
                    $setLabel = $schLabelArr[$selectStudentRight[$i]]["GHANO"];
                } else {
                    $setLabel = $schLabelArr[$selectStudentRight[$i]]["SCHNO"];
                }
                
                $opt_right[] = array('label' => $setLabel,
                    'value' => $selectStudentRight[$i]);
                $model->schList[] = $selectStudentRight[$i];
            }

            if (get_count($selectStudent) == 0 &&get_count($selectStudentRight) == 0) {
                //左側
                $query = knjp905_schregQuery::getSchno($model, "get");
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt_left[] = array('label' => $row["LABEL"],
                                        'value' => $row["VALUE"]);
                    $model->schList[] = $row["VALUE"];
                }
                $result->free();
                //右側
                $query = knjp905_schregQuery::getSchno($model);
                $result = $db->query($query);

                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt_right[] = array('label' => $row["LABEL"],
                                         'value' => $row["VALUE"]);
                    $model->schList[] = $row["VALUE"];
                }
                $result->free();
            }

            //CSV取込でセットする
            if ($model->cmd == "exec_main") {
                $toLeftSchList = array();
                foreach ($model->data_arr as $schregNo) {
                    //右リストから対象生徒を除外
                    foreach ($opt_right as $key => $rightArr) {
                        if ($schregNo == $rightArr["value"]) {
                            $toLeftSchList[] = array('label' => $rightArr["label"],
                                                     'value' => $rightArr["value"]);
                            array_splice($opt_right, $key, 1); // 指定要素を削除
                        }
                    }
                }
                //左リストに対象生徒をセット
                $opt_left = array_merge($opt_left, $toLeftSchList);
            }

            $transferList = array();
            foreach ($opt_right as $key => $value) {
                //除外対象の生徒のリストを作成
                $isTransferStd = $db->getOne(knjp905_schregQuery::checkTransferStd($model, $value["value"]));
                if ($isTransferStd) {
                    $transferList[] = $value["value"];
                }
            }
            knjCreateHidden($objForm, "transferList", implode(",", $transferList));

            //生徒一覧
            $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
            $arg["data"]["CATEGORY_NAME"] = knjcreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

            //出力対象一覧リスト
            $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
            $arg["data"]["CATEGORY_SELECTED"] = knjcreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

            //対象取消ボタン（全部）
            $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
            $arg["button"]["btn_rights"] = knjcreateBtn($objForm, "btn_rights", ">>", $extra);

            //対象選択ボタン（全部）
            $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
            $arg["button"]["btn_lefts"] = knjcreateBtn($objForm, "btn_lefts", "<<", $extra);

            //対象取消ボタン（一部）
            $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
            $arg["button"]["btn_right1"] = knjcreateBtn($objForm, "btn_right1", "＞", $extra);

            //対象選択ボタン（一部）
            $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
            $arg["button"]["btn_left1"] = knjcreateBtn($objForm, "btn_left1", "＜", $extra);
        }

        /*******/
        /* CSV */
        /*******/
        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        //ヘッダ有
        if ($model->field["WARIHURI_DIV"] == "1") {
            $extra = ($model->field["HEADER"] == "on" || VARS::get("SEND_YEAR")) ? "checked" : "";
            $extra .= " id=\"HEADER\"";
            $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");
        } else {
            //hidden
            knjCreateHidden($objForm, "HEADER", $model->field["HEADER"]);
        }

        //データ取込
        if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
            $extra = " disabled";
        } else {
            $extra = "onclick=\"return btn_submit('exec');\"";
        }
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "データ取込", $extra);

        /**********/
        /* ボタン */
        /**********/
        //更 新
        if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
            $extra = " disabled";
        } else {
            if ($model->field["WARIHURI_DIV"] == "1") {
                $extra = " onclick=\"return btn_submit('update');\"";
            } else {
                $extra = " onclick=\"return btn_submit('not_warihuri_update');\"";
            }
        }
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削 除
        if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
            $extra = " disabled";
        } else {
            $extra = " onclick=\"return btn_submit('delete');\"";
        }
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);

        //戻る
        $extra = "";
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP905_MAIN/knjp905_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJP905_SCHREG&SEND_OUTGO_L_CD={$model->getOutgoLcd}&SEND_OUTGO_M_CD={$model->getOutgoMcd}";
        $subdata .= "&SEND_SCHOOL_KIND={$model->getSchoolKind}&SEND_REQUEST_NO={$model->getRequestNo}";
        $subdata .= "&SEND_OUTGO_L_M_CD={$model->getOutgoLMcd}&SEND_YEAR={$model->getYear}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectStudent");
        knjCreateHidden($objForm, "selectStudentRight");
        knjCreateHidden($objForm, "selectStudentLabel");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);
        $arg["start"]   = $objForm->get_start("knjp905_schregForm1", "POST", "knjp905_schregindex.php", "", "knjp905_schregForm1");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML5($model, "knjp905_schregForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model)
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
