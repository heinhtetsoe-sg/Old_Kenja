<?php

require_once('for_php7.php');


class knjmp910_mainForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjmp910_mainindex.php", "", "main");
        
        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit' && $model->cmd !== 'torikomi') {
            $Row = knjmp910_mainQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjmp910_mainQuery::getCloseFlgData($model));

        //返金チェックと入金科目と決済の状況を取得(比較時に利用)
        if ($model->getRequestNo) {
            $model->getHenkinFlg = $db->getOne(knjmp910_mainQuery::getLevyData($model, "HENKIN_FLG"));
            $model->getHenkinApproval = $db->getOne(knjmp910_mainQuery::getLevyData($model, "HENKIN_APPROVAL"));
            $model->getCollectLMSGrpcd = $db->getOne(knjmp910_mainQuery::getLevyData($model, "COLLECT"));
            $model->getIncomeLcd = $db->getOne(knjmp910_mainQuery::getLevyData($model, "INCOME_L_CD"));
            $model->getApproval  = $db->getOne(knjmp910_mainQuery::getLevyData($model, "APPROVAL"));
            $model->getCancel    = $db->getOne(knjmp910_mainQuery::getLevyData($model, "CANCEL"));
        } else {
            $model->getHenkinFlg = "";
            $model->getHenkinApproval = "";
            $model->getCollectLMSGrpcd = "";
            $model->getIncomeLcd = "";
            $model->getApproval  = "";
            $model->getCancel    = "";
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
            $model->getSiharaiGk = $db->getOne(knjmp910_mainQuery::getSeisanGk($model, "SIHARAI_GK"));
            $model->getZanGk = $db->getOne(knjmp910_mainQuery::getSeisanGk($model, "ZAN_GK"));
            if ($Row["REQUEST_GK"] == "") {
                $Row["REQUEST_GK"] = $model->getSiharaiGk;
            }
        }
        knjCreateHidden($objForm, "SEISAN_REQUEST_NO", $model->getSeisanRequestNo);
        knjCreateHidden($objForm, "SEISAN_SIHARAIGK", $model->getSiharaiGk);

        //表示項目
        //伝票番号
        $arg["data"]["REQUEST_NO"] = $model->getRequestNo;

        //返金用のチェックボックス(単独のみ)
        if ($model->getKounyuRequestNo == "" && $model->getSekouRequestNo == "" && $model->getSeisanRequestNo == "") {
            $arg["HENKIN_FLG_SET"] = "1";
            $extra  = "id=\"HENKIN_FLG\"";
            if ($Row["HENKIN_FLG"] == "1") {
                $extra .= "checked='checked' ";
            } else {
                $extra .= "";
            }
            $extra .= " onclick=\"return btn_submit('edit');\"";
            $arg["data"]["HENKIN_FLG"] = knjCreateCheckBox($objForm, "HENKIN_FLG", "1", $extra);
        }
        //返金用入金細目
        if ($Row["HENKIN_FLG"] == "1") {
            $arg["HENKIN_HYOUJI"] = "1";
            //返金用の入金科目(細目)
            $extra = "onchange=\"return btn_submit('edit');\"";
            $query = knjmp910_mainQuery::getCollectSDiv($model);
            makeCombo($objForm, $arg, $db, $query, $Row["COLLECT_L_M_S_GRP_CD"], "COLLECT_L_M_S_GRP_CD", $extra, 1, "BLANK", $model);
        } else {
            $arg["NOT_HENKIN_HYOUJI"] = "1";
        }
        
        //各項目
        //収入科目
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjmp910_mainQuery::getIncomeLDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["INCOME_L_CD"], "INCOME_L_CD", $extra, 1, "BLANK", $model);

        //収入残高を表示
        //収入高
        $getIncomeRequestGk = $db->getOne(knjmp910_mainQuery::getIncomeSumRequestGk($model, $Row["INCOME_L_CD"]));

        //支出高(入力伝票以外)
        $getOutgoRequestGk = $db->getOne(knjmp910_mainQuery::getOutGoSumRequestGk($model, $Row));

        //収入残高を計算
        $model->field["TOTAL_ZAN_GK"] = $getIncomeRequestGk - ($getOutgoRequestGk + intval($Row["REQUEST_GK"]));
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_ZAN_GK"] = knjCreateTextBox($objForm, $model->field["TOTAL_ZAN_GK"], "TOTAL_ZAN_GK", 6, 6, $extra);

        //支出項目
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjmp910_mainQuery::getLevyMDiv();
        makeCombo($objForm, $arg, $db, $query, $Row["OUTGO_L_M_CD"], "OUTGO_L_M_CD", $extra, 1, "BLANK", $model);
        
        //支出伺い日
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$Row["REQUEST_DATE"]),"");

        //収入伺い日までの収入残高を表示
        //収入高
        $getIncomeRequestGk = $db->getOne(knjmp910_mainQuery::getIncomeSumRequestGk($model, $Row["INCOME_L_CD"], $Row["REQUEST_DATE"]));

        //支出高(入力伝票以外)
        $getOutgoRequestGk = $db->getOne(knjmp910_mainQuery::getOutGoSumRequestGk($model, $Row, $Row["REQUEST_DATE"]));

        //収入残高を計算
        $model->field["TOTAL_ZAN_DATE_GK"] = $getIncomeRequestGk - ($getOutgoRequestGk + intval($Row["REQUEST_GK"]));
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_ZAN_DATE_GK"] = knjCreateTextBox($objForm, $model->field["TOTAL_ZAN_DATE_GK"], "TOTAL_ZAN_DATE_GK", 6, 6, $extra);

        //起案者
        $extra = "";
        $query = knjmp910_mainQuery::getRequestStaff($model);
        makeCombo($objForm, $arg, $db, $query, $Row["REQUEST_STAFF"], "REQUEST_STAFF", $extra, 1, "BLANK", $model);
        
        //決議理由
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["REQUEST_REASON"] = knjCreateTextBox($objForm, $Row["REQUEST_REASON"], "REQUEST_REASON", 60, 60, $extra);
        
        //支出金額
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value); return btn_submit('edit');\"";
        $arg["data"]["REQUEST_GK"] = knjCreateTextBox($objForm, $Row["REQUEST_GK"], "REQUEST_GK", 7, 7, $extra);

        //摘要内容
        $model->sumTotalPrice = "";
        $model->sumTotalTax = "";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            //通常表示
            if ($model->cmd !== 'edit' && $model->cmd !== 'torikomi') {
                //品名等
                $arg["data"]["LEVY_S_NAME".$i] = $Row["LEVY_S_NAME".$i];
                //単価
                $arg["data"]["COMMODITY_PRICE".$i] = $Row["COMMODITY_PRICE".$i];
                //数量
                $arg["data"]["COMMODITY_CNT".$i] = $Row["COMMODITY_CNT".$i];
                //金額(税抜き)
                $arg["data"]["TOTAL_PRICE_ZEINUKI".$i] = $Row["TOTAL_PRICE_ZEINUKI".$i];
                //備考
                $arg["data"]["REMARK".$i] = $Row["REMARK".$i];
                
                //消費税
                $model->sumTotalTax += $Row["TOTAL_TAX".$i];
                //金額合計(税込み)
                $model->sumTotalPrice += $Row["TOTAL_PRICE".$i];
                //以下、サブミット時に表示するためhidden格納
                knjCreateHidden($objForm, "LEVY_S_NAME".$i, $Row["LEVY_S_NAME".$i]);
                knjCreateHidden($objForm, "COMMODITY_PRICE".$i, $Row["COMMODITY_PRICE".$i]);
                knjCreateHidden($objForm, "COMMODITY_CNT".$i, $Row["COMMODITY_CNT".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE_ZEINUKI".$i, $Row["TOTAL_PRICE_ZEINUKI".$i]);
                knjCreateHidden($objForm, "REMARK".$i, $Row["REMARK".$i]);
                knjCreateHidden($objForm, "TOTAL_TAX".$i, $Row["TOTAL_TAX".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE".$i, $Row["TOTAL_PRICE".$i]);
            //サブミット表示用
            } else {
                //品名等
                $arg["data"]["LEVY_S_NAME".$i] = $model->setRow["LEVY_S_NAME".$i];
                //単価
                $arg["data"]["COMMODITY_PRICE".$i] = $model->setRow["COMMODITY_PRICE".$i];
                //数量
                $arg["data"]["COMMODITY_CNT".$i] = $model->setRow["COMMODITY_CNT".$i];
                //金額(税抜き)
                $arg["data"]["TOTAL_PRICE_ZEINUKI".$i] = $model->setRow["TOTAL_PRICE_ZEINUKI".$i];
                //備考
                $arg["data"]["REMARK".$i] = $model->setRow["REMARK".$i];
                //消費税
                $model->sumTotalTax += $model->setRow["TOTAL_TAX".$i];
                //金額合計(税込み)
                $model->sumTotalPrice += $model->setRow["TOTAL_PRICE".$i];
                //以下、サブミット時に表示するためhidden格納
                knjCreateHidden($objForm, "LEVY_S_NAME".$i, $model->setRow["LEVY_S_NAME".$i]);
                knjCreateHidden($objForm, "COMMODITY_PRICE".$i, $model->setRow["COMMODITY_PRICE".$i]);
                knjCreateHidden($objForm, "COMMODITY_CNT".$i, $model->setRow["COMMODITY_CNT".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE_ZEINUKI".$i, $model->setRow["TOTAL_PRICE_ZEINUKI".$i]);
                knjCreateHidden($objForm, "REMARK".$i, $model->setRow["REMARK".$i]);
                knjCreateHidden($objForm, "TOTAL_TAX".$i, $model->setRow["TOTAL_TAX".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE".$i, $model->setRow["TOTAL_PRICE".$i]);
            }
        }
        //消費税
        $arg["data"]["SUM_TOTAL_TAX"] = $model->sumTotalTax;
        
        //振込手数料
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);keisanTotal(this.value);\"";
        $arg["data"]["REQUEST_TESUURYOU"] = knjCreateTextBox($objForm, $Row["REQUEST_TESUURYOU"], "REQUEST_TESUURYOU", 7, 7, $extra);
                
        //総支出額(振込手数料と合算)
        $arg["data"]["SUM_TOTAL_PRICE_ALL"] = $model->sumTotalPrice + $Row["REQUEST_TESUURYOU"];
        knjCreateHidden($objForm, "SUM_TOTAL_PRICE", $model->sumTotalPrice);

        //状態表示
        if ($model->getApproval === '1' && $model->getCancel == "") {
            $arg["data"]["SET_STATUS"] = '<font color="red">決裁済み</font>';
        } else if ($model->getCancel === '1') {
            $arg["data"]["SET_STATUS"] = '<font color="red">キャンセル</font>';
        }

        //返金実行状態表示
        if ($model->getApproval === '1' && $model->getHenkinApproval === '1') {
            $arg["data"]["SET_HENKIN_STATUS"] = '<font color="red">返金処理済み</font>';
        }

        //業者情報登録*********/
        //業者コード
        $extra = "";
        $query = knjmp910_mainQuery::getTraderCd($model);
        makeCombo($objForm, $arg, $db, $query, $Row["TRADER_CD"], "TRADER_CD", $extra, 1, "BLANK", $model);
        if ($model->cmd === 'torikomi') {
            $traderRow = array();
            $traderRow = $db->getRow(knjmp910_mainQuery::getTraderData($model), DB_FETCHMODE_ASSOC);
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
        $query = knjmp910_mainQuery::getBankCd();
        makeCombo($objForm, $arg, $db, $query, $Row["BANKCD"], "BANKCD", $extra, 1, "BLANK", $model);
        
        //支店コード
        $extra = "";
        $query = knjmp910_mainQuery::getBranchCd($Row["BANKCD"]);
        makeCombo($objForm, $arg, $db, $query, $Row["BRANCHCD"], "BRANCHCD", $extra, 1, "BLANK", $model);
        
        //口座種別
        $extra = "";
        $query = knjmp910_mainQuery::getNameMst("G203");
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
        $query = knjmp910_mainQuery::getNameMst("G217");
        makeCombo($objForm, $arg, $db, $query, $Row["PAY_DIV"], "PAY_DIV", $extra, 1, "BLANK", $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $Row);

        //hidden作成
        makeHidden($objForm, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp910_mainForm1.html", $arg); 
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
            $subdata  = "wopen('".REQUESTROOT."/M/KNJMP910_SCHREG/knjmp910_schregindex.php?cmd=main&SEND_AUTH={$model->auth}";
            $subdata .= "&SEND_PRGID=KNJMP910_MAIN&SEND_OUTGO_L_CD={$Row["OUTGO_L_CD"]}&SEND_OUTGO_M_CD={$Row["OUTGO_M_CD"]}";
            $subdata .= "&SEND_REQUEST_NO={$Row["REQUEST_NO"]}&SEND_YEAR={$Row["YEAR"]}&SEND_LINE_NO=".$i."";
            $subdata .= "&SEND_OUTGO_L_M_CD={$Row["OUTGO_L_M_CD"]}&SEND_INCOME_L_CD={$Row["INCOME_L_CD"]}";
            $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
            $extra = " onclick=\"$subdata\"";
        }
        $arg["button"]["btn_schreg".$i] = knjCreateBtn($objForm, "btn_schreg".$i, "登録画面", $extra);
    }
    //更 新
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHenkinApproval === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    //変更での更新
    } else if (($model->cmd === 'edit' || $model->cmd === 'torikomi') && $model->getRequestNo != "" && $model->getOutgoLMcd != "") {
        if ($model->getKounyuRequestNo == "" && $model->getSekouRequestNo == "" && $model->getSeisanRequestNo == "") {
            if (($Row["OUTGO_L_M_CD"] != $model->getOutgoLMcd) || $Row["INCOME_L_CD"] != $model->getIncomeLcd || $Row["HENKIN_FLG"] != $model->getHenkinFlg || $Row["COLLECT_L_M_S_GRP_CD"] != $model->getCollectLMSGrpcd) {
                $extra = " onclick=\"return btn_submit('delete_update');\"";
            } else {
                $extra = " onclick=\"return btn_submit('update');\"";
            }
        } else {
            if ($Row["OUTGO_L_M_CD"] != $model->getOutgoLMcd) {
                $extra = " onclick=\"btn_error('chenge_error');\"";
            } else if ($Row["HENKIN_FLG"] != $model->getHenkinFlg) {
                $extra = " onclick=\"btn_error('henkin_error');\"";
            } else {
                if ($Row["INCOME_L_CD"] != $model->getIncomeLcd || $Row["HENKIN_FLG"] != $model->getHenkinFlg || $Row["COLLECT_L_M_S_GRP_CD"] != $model->getCollectLMSGrpcd) {
                    $extra = " onclick=\"return btn_submit('delete_update');\"";
                } else {
                    $extra = " onclick=\"return btn_submit('update');\"";
                }
            }
        }
    //新規での更新
    } else if ($model->getRequestNo == ""){
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
    if ($model->getApproval === '1' || $model->getHenkinApproval === '1' || $model->getHonjimeCount > 0) {
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
    } else if ($model->sumTotalPrice + $Row["REQUEST_TESUURYOU"] != $Row["REQUEST_GK"]) {
        $extra = " onclick=\"btn_error('kessai');\"";
    } else if ($model->getSeisanRequestNo != "" && $model->getSiharaiGk != $Row["REQUEST_GK"]) {
        $extra = " onclick=\"btn_error('seisan');\"";
    } else if (($model->cmd === 'edit' || $model->cmd === 'torikomi') && $model->getOutgoLMcd != "") {
        $extra = " onclick=\"btn_error('huriwake');\"";
    } else {
        $subdata  = "wopen('".REQUESTROOT."/M/KNJMP910_APPROVAL/knjmp910_approvalindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJMP910_MAIN&SEND_OUTGO_L_CD={$Row["OUTGO_L_CD"]}&SEND_OUTGO_M_CD={$Row["OUTGO_M_CD"]}";
        $subdata .= "&SEND_REQUEST_NO={$Row["REQUEST_NO"]}&SEND_YEAR={$Row["YEAR"]}";
        $subdata .= "&SEND_OUTGO_L_M_CD={$Row["OUTGO_L_M_CD"]}&SEND_INCOME_L_CD={$Row["INCOME_L_CD"]}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
    }
    $arg["button"]["btn_kessai"] = knjCreateBtn($objForm, "btn_kessai", "決裁画面", $extra);
    
    //購入画面
    if ($model->getKounyuRequestNo != "" || $Row["KOUNYU_NO"] != "") {
        $arg["kounyu_hyouji"] = "1";
        $subdata  = "wopen('".REQUESTROOT."/M/KNJMP920_MAIN/knjmp920_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJMP910_MAIN&SEND_KOUNYU_L_CD={$model->getOutgoLcd}&SEND_KOUNYU_M_CD={$model->getOutgoMcd}";
        $subdata .= "&SEND_OUTGO_REQUEST_NO={$model->getRequestNo}&SEND_YEAR={$model->getYear}&SEND_REQUEST_NO={$model->getKounyuRequestNo}";
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
        $subdata  = "wopen('".REQUESTROOT."/M/KNJMP930_MAIN/knjmp930_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJMP910_MAIN&SEND_SEKOU_L_CD={$model->getOutgoLcd}&SEND_SEKOU_M_CD={$model->getOutgoMcd}";
        $subdata .= "&SEND_OUTGO_REQUEST_NO={$model->getRequestNo}&SEND_YEAR={$model->getYear}&SEND_REQUEST_NO={$model->getSekouRequestNo}";
        $subdata .= "&SEND_SEKOU_L_M_CD={$model->getOutgoLMcd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
        $arg["button"]["btn_sekou"] = knjCreateBtn($objForm, "btn_sekou", "施行伺画面", $extra);
    }
    //精算画面
    //決裁済みのときのみ
    if ($model->getSeisanRequestNo != "" || $Row["SEISAN_NO"] != "") {
        $arg["seisan_hyouji"] = "1";
        $subdata  = "wopen('".REQUESTROOT."/M/KNJMP940_MAIN/knjmp940_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJMP910_MAIN&SEND_SEISAN_L_CD={$model->getOutgoLcd}&SEND_SEISAN_M_CD={$model->getOutgoMcd}";
        $subdata .= "&SEND_OUTGO_REQUEST_NO={$model->getRequestNo}&SEND_YEAR={$model->getYear}&SEND_REQUEST_NO={$model->getSeisanRequestNo}";
        $subdata .= "&SEND_SEISAN_L_M_CD={$model->getOutgoLMcd}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
        $arg["button"]["btn_seisan"] = knjCreateBtn($objForm, "btn_seisan", "精算票画面", $extra);
    } 
    
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJMP952");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "REQUEST_NO", $model->getRequestNo);
}
?>
