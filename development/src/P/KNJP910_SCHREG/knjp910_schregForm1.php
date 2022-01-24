<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knjp910_schregForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjp910_schregForm1", "POST", "knjp910_schregindex.php", "", "knjp910_schregForm1");

        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit') {
            $Row = knjp910_schregQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjp910_schregQuery::getCloseFlgData($model));

        //返金フラグと返金用の入金科目、決済の状況取得(比較時に利用)
        if ($model->getOutgoLMcd) {
            $model->getHenkinFlg = $db->getOne(knjp910_schregQuery::getLevyData($model, "HENKIN_FLG"));
            $model->getHenkinApproval = $db->getOne(knjp910_schregQuery::getLevyData($model, "HENKIN_APPROVAL"));
            $model->getCollectLMSGrpcd = $db->getOne(knjp910_schregQuery::getLevyData($model, "COLLECT"));
            $model->getApproval = $db->getOne(knjp910_schregQuery::getLevyData($model, "APPROVAL"));
            $model->getCancel   = $db->getOne(knjp910_schregQuery::getLevyData($model, "CANCEL"));
        } else {
            $model->getHenkinFlg = "";
            $model->getHenkinApproval = "";
            $model->getCollectLMSGrpcd = "";
            $model->getApproval = "";
            $model->getCancel   = "";
        }
                
        //支出科目
        $query = knjp910_schregQuery::getLevyMDiv($model);
        $arg["data"]["OUTGO_M_NAME"] = $db->getOne($query);
        
        //生徒、生徒以外切替
        $opt = array(1, 2);
        //初期値(データなしの場合)
        if (!$Row["WARIHURI_DIV"]) {
            $Row["WARIHURI_DIV"] = "1";
        }
        $model->field["WARIHURI_DIV"] = ($model->field["WARIHURI_DIV"] == "") ? $Row["WARIHURI_DIV"] : $model->field["WARIHURI_DIV"];
        $extra = array("id=\"WARIHURI_DIV1\" onclick=\"return btn_submit('knjp910_schreg');\"", "id=\"WARIHURI_DIV2\" onclick=\"return btn_submit('knjp910_schreg');\"");
        $radioArray = knjCreateRadio($objForm, "WARIHURI_DIV", $model->field["WARIHURI_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        //返金フラグNULLのときだけ2を表示(WARIHURI_DIV = '1'固定)
        if ($model->getHenkinFlg == "") {
            $arg["TSUJYOU_HYOUJI"] = "1";
            $arg["data"]["GOUKEI_GK_NAME"] = '合計額(税抜き)';
        } else {
            $arg["data"]["GOUKEI_GK_NAME"] = '返金額合計';
        }

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
                
        //支出細目
        $extra = "";
        $query = knjp910_schregQuery::getLevySDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["OUTGO_L_M_S_CD"], "OUTGO_L_M_S_CD", $extra, 1, "BLANK", $model);
        
        //単価
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["COMMODITY_PRICE"] = knjCreateTextBox($objForm, $Row["COMMODITY_PRICE"], "COMMODITY_PRICE", 6, 6, $extra);

        //数量
        if ($model->field["WARIHURI_DIV"] == "1") {
            $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        } else {
            $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        }
        $arg["data"]["COMMODITY_CNT"] = knjCreateTextBox($objForm, $Row["COMMODITY_CNT"], "COMMODITY_CNT", 6, 6, $extra);
        
        //合計金額(税抜き)
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_PRICE_ZEINUKI"] = knjCreateTextBox($objForm, $Row["TOTAL_PRICE_ZEINUKI"], "TOTAL_PRICE_ZEINUKI", 6, 6, $extra);
        
        //税込み単価
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        if ($Row["COMMODITY_CNT"] > 0) {
            $setPriceTax = $Row["COMMODITY_PRICE"] + ($Row["TOTAL_TAX"] / $Row["COMMODITY_CNT"]);
        }
        $arg["data"]["PRICE_TAX"] = knjCreateTextBox($objForm, $setPriceTax, "PRICE_TAX", 6, 6, $extra);
        
        //消費税
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_TAX"] = knjCreateTextBox($objForm, $Row["TOTAL_TAX"], "TOTAL_TAX", 6, 6, $extra);
        
        //合計金額(税込み)
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_PRICE"] = knjCreateTextBox($objForm, $Row["TOTAL_PRICE"], "TOTAL_PRICE", 6, 6, $extra);
        
        //生徒以外の場合
        if ($model->field["WARIHURI_DIV"] == "2") {
            $arg["WARIHURI_NG"] = "1";
            //請求書番号
            $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toAlphanumeric(this.value);\"";
            $arg["data"]["TRADER_SEIKYU_NO"] = knjCreateTextBox($objForm, $Row["TRADER_SEIKYU_NO"], "TRADER_SEIKYU_NO", 10, 10, $extra);
            
            //請求月
            $extra = "";
            $query = knjp910_schregQuery::getSeikyuMonth();
            makeCombo($objForm, $arg, $db, $query, $Row["SEIKYU_MONTH"], "SEIKYU_MONTH", $extra, 1, "BLANK", $model);
        }

        //摘要
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 120, $extra);

        //生徒の場合
        if ($model->field["WARIHURI_DIV"] == "1") {
            $arg["WARIHURI_OK"] = "1";
            //生徒一覧 OR 科目一覧リスト
            $opt_right = array();
            $opt_left  = array();

            //左側
            if (!$model->getHenkinFlg) {
                $query = knjp910_schregQuery::getSchno($model, "get");
            //返金用
            } else {
                $query = knjp910_schregQuery::getHenkinSchno($model, "get");
            }
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_left[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            }
            $result->free();
            //右側
            if (!$model->getHenkinFlg) {
                $query = knjp910_schregQuery::getSchno($model);
            //返金用
            } else {
                $query = knjp910_schregQuery::getHenkinSchno($model);
            }
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
            $result->free();

            //生徒一覧
            $extra = "multiple style=\"width:275px\" ondblclick=\"move1('left')\"";
            $arg["data"]["CATEGORY_NAME"] = knjcreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

            //出力対象一覧リスト
            $extra = "multiple style=\"width:275px\" ondblclick=\"move1('right')\"";
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

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp910_schregForm1.html", $arg); 
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
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHenkinApproval === '1' || $model->getHonjimeCount > 0) {
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
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHenkinApproval === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        $extra = " onclick=\"return btn_submit('delete');\"";
    }
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    
    //戻る
    $extra = "";
    $subdata  = "wopen('".REQUESTROOT."/P/KNJP910_MAIN/knjp910_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
    $subdata .= "&SEND_PRGID=KNJP910_SCHREG&SEND_OUTGO_L_CD={$model->getOutgoLcd}&SEND_OUTGO_M_CD={$model->getOutgoMcd}";
    $subdata .= "&SEND_SCHOOL_KIND={$model->getSchoolKind}&SEND_REQUEST_NO={$model->getRequestNo}";
    $subdata .= "&SEND_OUTGO_L_M_CD={$model->getOutgoLMcd}&SEND_YEAR={$model->getYear}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $extra = "onclick=\"$subdata\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectStudent");
    knjCreateHidden($objForm, "selectStudentLabel");
}

?>
