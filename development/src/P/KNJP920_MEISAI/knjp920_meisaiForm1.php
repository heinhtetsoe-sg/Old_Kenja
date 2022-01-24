<?php

require_once('for_php7.php');
/*
 *　修正履歴
 *
 */
class knjp920_meisaiForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjp920_meisaiForm1", "POST", "knjp920_meisaiindex.php", "", "knjp920_meisaiForm1");

        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit') {
            $Row = knjp920_meisaiQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjp920_meisaiQuery::getCloseFlgData($model));

        //支出伺の決済の状況を取得(比較時に利用)
        if ($model->getKounyuLMcd) {
            $model->getApproval = $db->getOne(knjp920_meisaiQuery::getOutgoData($model, "APPROVAL"));
            $model->getCancel   = $db->getOne(knjp920_meisaiQuery::getOutgoData($model, "CANCEL"));
        } else {
            $model->getApproval = "";
            $model->getCancel   = "";
        }
                
        //購入科目
        $query = knjp920_meisaiQuery::getLevyMDiv($model);
        $arg["data"]["KOUNYU_M_NAME"] = $db->getOne($query);

        //NO
        $arg["data"]["NO"] = $model->getLineNo;

        //支出細目名称取得
        $levySnames = $sep = "";
        $cmbCnt = 1;
        $query = knjp920_meisaiQuery::getLevySDiv($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $levySnames .= $sep.$row["NAME"];
            $sep = ":";
            $cmbCnt++;
        }
        knjCreateHidden($objForm, "LEVY_S_NAMES", $levySnames);

        //購入細目
        $extra = " onchange=\"return btn_submit('edit');\"";
        $query = knjp920_meisaiQuery::getLevySDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["KOUNYU_L_M_S_CD"], "KOUNYU_L_M_S_CD", $extra, 1, "BLANK", $model);
        //テキスト
        $extra = "";
        $arg["data"]["LEVY_S_NAME"] = knjCreateTextBox($objForm, $model->field["LEVY_S_NAME"], "LEVY_S_NAME", 40, 90, $extra);
        //max会計科目コード取得
        $model->maxScd = sprintf("%02d", $db->getOne(knjp920_meisaiQuery::getLevySDiv($model, "MAX")) + 1);
        $setLMScd      = "{$model->getKounyuLMcd}"."{$model->maxScd}" ;
        //button
        $extra = " onclick=\"return add('{$setLMScd}', '{$cmbCnt}');\"";
        $arg["button"]["btn_TXT_TO_CMB"] = knjCreateBtn($objForm, "btn_TXT_TO_CMB", "追加", $extra);

        //給付対象等、取得
        $query = knjp920_meisaiQuery::getLevySmstInfo($model, $Row["KOUNYU_L_M_S_CD"]);
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
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //給付対象使用するか
        if ($model->Properties["useBenefit"] == "1") {
            $arg["useBenefit"] = "1";

            //給付対象checkbox
            $checked = ($Row["BENEFIT"] == "1") ? " checked": "";
            $extra = "id=\"BENEFIT\"";
            $arg["data"]["BENEFIT"] = knjCreateCheckBox($objForm, "BENEFIT", "1", $extra.$checked);
        }

        //単価
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["COMMODITY_PRICE"] = knjCreateTextBox($objForm, $Row["COMMODITY_PRICE"], "COMMODITY_PRICE", 6, 6, $extra);

        //税込checkbox
        $checked = ($Row["TOTAL_TAX"] == 0 && $Row["TOTAL_TAX"] != "") ? " checked": "";
        $extra = "id=\"IN_TAX\"";
        $arg["data"]["IN_TAX"] = knjCreateCheckBox($objForm, "IN_TAX", "1", $extra.$checked);

        //数量
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["COMMODITY_CNT"] = knjCreateTextBox($objForm, $Row["COMMODITY_CNT"], "COMMODITY_CNT", 6, 6, $extra);
        
        //合計金額(税抜き)
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_PRICE_ZEINUKI"] = knjCreateTextBox($objForm, $Row["TOTAL_PRICE_ZEINUKI"], "TOTAL_PRICE_ZEINUKI", 6, 6, $extra);
        
        //消費税
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_TAX"] = knjCreateTextBox($objForm, $Row["TOTAL_TAX"], "TOTAL_TAX", 6, 6, $extra);
        
        //合計金額(税込み)
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_PRICE"] = knjCreateTextBox($objForm, $Row["TOTAL_PRICE"], "TOTAL_PRICE", 6, 6, $extra);

        //摘要
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 120, $extra);

        //生徒数
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["SCH_CNT"] = knjCreateTextBox($objForm, $Row["SCH_CNT"], "SCH_CNT", 6, 6, $extra);

        //生徒単価
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["SCH_PRICE"] = knjCreateTextBox($objForm, $Row["SCH_PRICE"], "SCH_PRICE", 6, 6, $extra);

        //端数
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["HASUU"] = knjCreateTextBox($objForm, $Row["HASUU"], "HASUU", 6, 6, $extra);

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp920_meisaiForm1.html", $arg); 
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
function makeButton(&$objForm, &$arg, $model)
{
    //更 新
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        $extra = " onclick=\"return btn_submit('update');\"";
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
    $subdata  = "wopen('".REQUESTROOT."/P/KNJP920_MAIN/knjp920_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
    $subdata .= "&SEND_PRGID=KNJP920_MEISAI&SEND_KOUNYU_L_CD={$model->getKounyuLcd}&SEND_KOUNYU_M_CD={$model->getKounyuMcd}";
    $subdata .= "&SEND_SCHOOL_KIND={$model->getSchoolKind}&SEND_REQUEST_NO={$model->getRequestNo}";
    $subdata .= "&SEND_KOUNYU_L_M_CD={$model->getKounyuLMcd}&SEND_YEAR={$model->getYear}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $extra = "onclick=\"$subdata\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}

?>
