<?php

require_once('for_php7.php');


class knjmp900_mainForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjmp900_mainindex.php", "", "main");
        
        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit') {
            $Row = knjmp900_mainQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjmp900_mainQuery::getCloseFlgData($model));
        
        //入金科目と決済の状況を取得(比較時に利用)
        if ($model->getIncomeLMcd) {
            $model->getCollectLMScd = $db->getOne(knjmp900_mainQuery::getLevyData($model, "COLLECTCD"));
            $model->getApproval = $db->getOne(knjmp900_mainQuery::getLevyData($model, "APPROVAL"));
            $model->getCancel   = $db->getOne(knjmp900_mainQuery::getLevyData($model, "CANCEL"));
        } else {
            $model->getCollectLMScd = "";
            $model->getApproval = "";
            $model->getCancel   = "";
        }

        //表示項目
        //伝票番号
        $arg["data"]["REQUEST_NO"] = $model->getRequestNo;
        
        //各項目
        //入金科目
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjmp900_mainQuery::getCollectLDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["COLLECT_L_M_S_CD"], "COLLECT_L_M_S_CD", $extra, 1, "BLANK", $model);
        
        //収入項目
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjmp900_mainQuery::getLevyMDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["INCOME_L_M_CD"], "INCOME_L_M_CD", $extra, 1, "BLANK", $model);
        
        //収入伺い日
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$Row["REQUEST_DATE"]),"");
        
        //起案者
        $extra = "";
        $query = knjmp900_mainQuery::getRequestStaff($model);
        makeCombo($objForm, $arg, $db, $query, $Row["REQUEST_STAFF"], "REQUEST_STAFF", $extra, 1, "BLANK", $model);
        
        //決議理由
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["REQUEST_REASON"] = knjCreateTextBox($objForm, $Row["REQUEST_REASON"], "REQUEST_REASON", 60, 60, $extra);
        
        //収入金額
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["REQUEST_GK"] = knjCreateTextBox($objForm, $Row["REQUEST_GK"], "REQUEST_GK", 7, 7, $extra);

        //摘要内容
        $model->sumTotalPrice = "";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            //通常表示
            if ($model->cmd !== 'edit') {
                //摘要
                $arg["data"]["COMMODITY_NAME".$i] = $Row["COMMODITY_NAME".$i];
                //単価
                $arg["data"]["COMMODITY_PRICE".$i] = $Row["COMMODITY_PRICE".$i];
                //数量
                $arg["data"]["COMMODITY_CNT".$i] = $Row["COMMODITY_CNT".$i];
                //金額
                $arg["data"]["TOTAL_PRICE".$i] = $Row["TOTAL_PRICE".$i];
                //備考
                $arg["data"]["REMARK".$i] = $Row["REMARK".$i];
                
                //金額合計
                $model->sumTotalPrice += $Row["TOTAL_PRICE".$i];
                //以下、サブミット時に表示するためhidden格納
                knjCreateHidden($objForm, "COMMODITY_NAME".$i, $Row["COMMODITY_NAME".$i]);
                knjCreateHidden($objForm, "COMMODITY_PRICE".$i, $Row["COMMODITY_PRICE".$i]);
                knjCreateHidden($objForm, "COMMODITY_CNT".$i, $Row["COMMODITY_CNT".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE".$i, $Row["TOTAL_PRICE".$i]);
                knjCreateHidden($objForm, "REMARK".$i, $Row["REMARK".$i]);
            } else {
                //摘要
                $arg["data"]["COMMODITY_NAME".$i] = $model->setRow["COMMODITY_NAME".$i];
                //単価
                $arg["data"]["COMMODITY_PRICE".$i] = $model->setRow["COMMODITY_PRICE".$i];
                //数量
                $arg["data"]["COMMODITY_CNT".$i] = $model->setRow["COMMODITY_CNT".$i];
                //金額
                $arg["data"]["TOTAL_PRICE".$i] = $model->setRow["TOTAL_PRICE".$i];
                //備考
                $arg["data"]["REMARK".$i] = $model->setRow["REMARK".$i];
                
                //金額合計
                $model->sumTotalPrice += $model->setRow["TOTAL_PRICE".$i];
                //以下、サブミット時に表示するためhidden格納
                knjCreateHidden($objForm, "COMMODITY_NAME".$i, $model->setRow["COMMODITY_NAME".$i]);
                knjCreateHidden($objForm, "COMMODITY_PRICE".$i, $model->setRow["COMMODITY_PRICE".$i]);
                knjCreateHidden($objForm, "COMMODITY_CNT".$i, $model->setRow["COMMODITY_CNT".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE".$i, $model->setRow["TOTAL_PRICE".$i]);
                knjCreateHidden($objForm, "REMARK".$i, $model->setRow["REMARK".$i]);
            }
        }
        //総収入額
        $arg["data"]["SUM_TOTAL_PRICE"] = $model->sumTotalPrice;
        knjCreateHidden($objForm, "SUM_TOTAL_PRICE", $model->sumTotalPrice);

        //状態表示
        if ($model->getApproval === '1' && $model->getCancel == "") {
            $arg["data"]["SET_STATUS"] = '<font color="red">決裁済み</font>';
        } else if ($model->getCancel === '1') {
            $arg["data"]["SET_STATUS"] = '<font color="red">キャンセル</font>';
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $Row);

        //hidden作成
        makeHidden($objForm, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp900_mainForm1.html", $arg); 
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
        if ($name == "NEW_SHINSEI_DIV" && ($row["VALUE"] == "2" || $row["VALUE"] == "4")) {
            continue;
        }
        if ($name != "NEW_SHINSEI_G" && $model->sectioncd === '0002' && $row["VALUE"] != "7") {
            continue;
        }
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "NEW_SHINSEI_G") {
        $value = ($value && $value_flg) ? $value : 4;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $Row) {
    //新規登録
    $flg = "1";
    $root = REQUESTROOT;
    $auth = AUTHORITY;
    
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    
    //生徒振り分け設定
    for ($i = 1; $i <= $model->koumoku; $i++) {
        if ($model->getIncomeLMcd == "") {
            $extra = " onclick=\"btn_error('new');\"";
        } else if ($model->cmd === 'edit' && $model->getIncomeLMcd != "") {
            $extra = " onclick=\"btn_error('huriwake');\"";
        } else {
            $subdata  = "wopen('".REQUESTROOT."/M/KNJMP900_SCHREG/knjmp900_schregindex.php?cmd=main&SEND_AUTH={$model->auth}";
            $subdata .= "&SEND_PRGID=KNJMP900_MAIN&SEND_INCOME_L_CD={$Row["INCOME_L_CD"]}&SEND_INCOME_M_CD={$Row["INCOME_M_CD"]}";
            $subdata .= "&SEND_REQUEST_NO={$Row["REQUEST_NO"]}&SEND_YEAR={$Row["YEAR"]}&SEND_LINE_NO=".$i."";
            $subdata .= "&SEND_INCOME_L_M_CD={$Row["INCOME_L_M_CD"]}&SEND_COLLECT_L_M_S_CD={$Row["COLLECT_L_M_S_CD"]}";
            $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
            $extra = " onclick=\"$subdata\"";
        }
        $arg["button"]["btn_schreg".$i] = knjCreateBtn($objForm, "btn_schreg".$i, "登録画面", $extra);
    }
    //更 新
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else if ($model->cmd === 'edit' && $model->getIncomeLMcd != "") {
        $extra = " onclick=\"return btn_submit('delete_update');\"";
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
    if ($model->sumTotalPrice != $Row["REQUEST_GK"]) {
        $extra = " onclick=\"btn_error('kessai');\"";
    } else if ($model->cmd === 'edit' && $model->getIncomeLMcd != "") {
        $extra = " onclick=\"btn_error('huriwake');\"";
    } else {
        $subdata  = "wopen('".REQUESTROOT."/M/KNJMP900_APPROVAL/knjmp900_approvalindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJMP900_MAIN&SEND_INCOME_L_CD={$Row["INCOME_L_CD"]}&SEND_INCOME_M_CD={$Row["INCOME_M_CD"]}";
        $subdata .= "&SEND_REQUEST_NO={$Row["REQUEST_NO"]}&SEND_YEAR={$Row["YEAR"]}";
        $subdata .= "&SEND_INCOME_L_M_CD={$Row["INCOME_L_M_CD"]}&SEND_COLLECT_L_M_S_CD={$Row["COLLECT_L_M_S_CD"]}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
    }
    $arg["button"]["btn_kessai"] = knjCreateBtn($objForm, "btn_kessai", "決裁画面", $extra);
    
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJMP951");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "REQUEST_NO", $model->getRequestNo);
    
}
?>
