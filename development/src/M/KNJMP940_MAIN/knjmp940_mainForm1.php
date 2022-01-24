<?php

require_once('for_php7.php');


class knjmp940_mainForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjmp940_mainindex.php", "", "main");
        
        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit') {
            $Row = knjmp940_mainQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjmp940_mainQuery::getCloseFlgData($model));

        //支出伺の決済の状況を取得(比較時に利用)
        if ($model->getRequestNo) {
            $model->getApproval = $db->getOne(knjmp940_mainQuery::getOutgoData($model, "APPROVAL"));
            $model->getCancel   = $db->getOne(knjmp940_mainQuery::getOutgoData($model, "CANCEL"));
            //支出伺画面から遷移してきていない場合
            if(!$model->getOutgoRequestNo) {
                $model->getOutgoRequestNo = $db->getOne(knjmp940_mainQuery::getOutgoData($model, "REQUEST_NO"));
            }
        } else {
            $model->getApproval = "";
            $model->getCancel   = "";
        }
        
        //表示項目
        //伝票番号
        $arg["data"]["REQUEST_NO"] = $model->getRequestNo;
        
        //各項目
        //精算項目(支出項目)
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjmp940_mainQuery::getLevyMDiv();
        makeCombo($objForm, $arg, $db, $query, $Row["SEISAN_L_M_CD"], "SEISAN_L_M_CD", $extra, 1, "BLANK", $model);
        
        //伺い日
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$Row["REQUEST_DATE"]),"");
        
        //精算者
        $extra = "";
        $query = knjmp940_mainQuery::getRequestStaff($model);
        makeCombo($objForm, $arg, $db, $query, $Row["REQUEST_STAFF"], "REQUEST_STAFF", $extra, 1, "BLANK", $model);
        
        //件名
        $extra = " STYLE=\"ime-mode: active;\"";
        $arg["data"]["SEISAN_TITLE"] = knjCreateTextBox($objForm, $Row["SEISAN_TITLE"], "SEISAN_TITLE", 45, 120, $extra);
        
        //現金受領者
        $extra = "";
        $query = knjmp940_mainQuery::getRequestStaff($model);
        makeCombo($objForm, $arg, $db, $query, $Row["GENKIN_JURYOU_STAFF"], "GENKIN_JURYOU_STAFF", $extra, 1, "BLANK", $model);
        
        //精算内容
        $extra = " STYLE=\"ime-mode: active;height:60px;\"";
        $arg["data"]["SEISAN_NAIYOU"] = KnjCreateTextArea($objForm, "SEISAN_NAIYOU", 3, 76, "soft", $extra, $Row["SEISAN_NAIYOU"]);
        
        //受年月日
        $arg["data"]["JURYOU_DATE"] = View::popUpCalendar($objForm, "JURYOU_DATE",str_replace("-","/",$Row["JURYOU_DATE"]),"");
        
        //受領額
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);keisanZangk(this.value);\"";
        $arg["data"]["JURYOU_GK"] = knjCreateTextBox($objForm, $Row["JURYOU_GK"], "JURYOU_GK", 7, 7, $extra);
        
        //支払年月日
        $arg["data"]["SIHARAI_DATE"] = View::popUpCalendar($objForm, "SIHARAI_DATE",str_replace("-","/",$Row["SIHARAI_DATE"]),"");

        //支払額
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);keisanZangk(this.value)\"";
        $arg["data"]["SIHARAI_GK"] = knjCreateTextBox($objForm, $Row["SIHARAI_GK"], "SIHARAI_GK", 7, 7, $extra);
        
        //残額
        $extra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["ZAN_GK"] = knjCreateTextBox($objForm, $Row["ZAN_GK"], "ZAN_GK", 7, 7, $extra);
        
        //備考
        $extra = " STYLE=\"ime-mode: active;\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 120, $extra);
        
        //出納責任者
        $extra = "";
        $query = knjmp940_mainQuery::getRequestStaff($model);
        makeCombo($objForm, $arg, $db, $query, $Row["SUITOU_STAFF"], "SUITOU_STAFF", $extra, 1, "BLANK", $model);

        //預金口座入金年月日
        $arg["data"]["INCOME_DATE"] = View::popUpCalendar($objForm, "INCOME_DATE",str_replace("-","/",$Row["INCOME_DATE"]),"");

        //支出伺 状態表示
        if ($model->getOutgoRequestNo) {
            if ($model->getApproval === '1' && $model->getCancel == "") {
                $arg["data"]["SET_STATUS"] = $model->getOutgoRequestNo.'<font color="red">(決裁 済み)</font>';
            } else if ($model->getCancel === '1') {
                $arg["data"]["SET_STATUS"] = $model->getOutgoRequestNo.'<font color="red">(キャンセル)</font>';
            } else {
                $arg["data"]["SET_STATUS"] = $model->getOutgoRequestNo.'<font>(伺い中)</font>';
            }
        } else {
            $arg["data"]["SET_STATUS"] = '未作成';
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
        View::toHTML($model, "knjmp940_mainForm1.html", $arg); 
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
    
    //印刷ボタンを作成する
    if ($model->getRequestNo == "") {
        $extra = " onclick=\"btn_error('new');\"";
    } else {
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    }
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    
    //更 新
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
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
    //支出伺画面
    if ($model->getRequestNo == "") {
        $extra = " onclick=\"btn_error('new');\"";
    } else if ($model->cmd === 'edit' && $model->getSeisanLMcd != "") {
        $extra = " onclick=\"btn_error('huriwake');\"";
    } else {
        $subdata  = "wopen('".REQUESTROOT."/M/KNJMP910_MAIN/knjmp910_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJMP940_MAIN&SEND_OUTGO_L_CD={$Row["SEISAN_L_CD"]}&SEND_OUTGO_M_CD={$Row["SEISAN_M_CD"]}";
        $subdata .= "&SEND_REQUEST_NO={$model->getOutgoRequestNo}&SEND_YEAR={$Row["YEAR"]}";
        $subdata .= "&SEND_OUTGO_L_M_CD={$Row["SEISAN_L_M_CD"]}&SEND_SEISAN_REQUEST_NO={$model->getRequestNo}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
    }
    $arg["button"]["btn_kessai"] = knjCreateBtn($objForm, "btn_kessai", "支出伺画面", $extra);
    
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJMP955");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "REQUEST_NO", $model->getRequestNo);
    
}
?>
