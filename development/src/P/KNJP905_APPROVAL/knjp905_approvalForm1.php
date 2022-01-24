<?php

require_once('for_php7.php');


class knjp905_approvalForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp905_approvalindex.php", "", "main");
        
        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd != "check") {
            $Row = knjp905_approvalQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjp905_approvalQuery::getCloseFlgData($model));

        //決裁の状況を取得(比較時に利用)
        if ($model->getOutgoLMcd) {
            $model->getHenkinApproval = $db->getOne(knjp905_approvalQuery::getLevyData($model, "HENKIN_APPROVAL"));
            $model->getApproval = $db->getOne(knjp905_approvalQuery::getLevyData($model, "APPROVAL"));
        } else {
            $model->getHenkinApproval = "";
            $model->getApproval = "";
        }
        knjCreateHidden($objForm, "GET_HENKIN_APPROVAL", $model->getHenkinApproval);

        //支出項目
        $query = knjp905_approvalQuery::getLevyMDiv($model);
        $arg["data"]["LEVY_M_NAME"] = $db->getOne($query);

        //支出金額
        $query = knjp905_approvalQuery::getRequestGk($model);
        $arg["data"]["REQUEST_GK"] = $db->getOne($query);

        /************/
        /* 状況     */
        /************/

        //決裁済み
        $extra  = "id=\"OUTGO_APPROVAL\"";
        $extra .= ($Row["OUTGO_APPROVAL"] == "1") ? " checked" : "";
        $arg["data"]["OUTGO_APPROVAL"] = knjCreateCheckBox($objForm, "OUTGO_APPROVAL", "1", $extra);
        knjCreateHidden($objForm, "OUTGO_APPROVAL_VALUE", $Row["OUTGO_APPROVAL"]);
        
        //キャンセル
        $extra  = "id=\"OUTGO_CANCEL\"";
        $extra .= ($Row["OUTGO_CANCEL"] == "1") ? " checked" : "";
        $arg["data"]["OUTGO_CANCEL"] = knjCreateCheckBox($objForm, "OUTGO_CANCEL", "1", $extra);
        knjCreateHidden($objForm, "OUTGO_CANCEL_VALUE", $Row["OUTGO_CANCEL"]);
        
        /************/
        /* 摘要     */
        /************/

        //金額を領収しました。
        $extra  = "id=\"OUTGO_CHECK1\"";
        $extra .= ($Row["OUTGO_CHECK1"] == "1") ? " checked" : "";
        $arg["data"]["OUTGO_CHECK1"] = knjCreateCheckBox($objForm, "OUTGO_CHECK1", "1", $extra);
        
        //領収日
        $arg["data"]["OUTGO_CHECK1_DATE"] = View::popUpCalendar($objForm, "OUTGO_CHECK1_DATE",str_replace("-","/",$Row["OUTGO_CHECK1_DATE"]),"");
        
        //受取人氏名
        $query = knjp905_approvalQuery::getOutgoStaff($model, $Row["OUTGO_CHECK1_STAFF"]);
        $setStaff = $db->getOne($query);
        $arg["data"]["OUTGO_CHECK1_STAFF"] = $setStaff;
        //決済承認者
        knjCreateHidden($objForm, "OUTGO_CHECK1_STAFF", $Row["OUTGO_CHECK1_STAFF"]);

        //領収書、支払い証明書
        $extra  = "id=\"OUTGO_CHECK2\"";
        $extra .= ($Row["OUTGO_CHECK2"] == "1") ? " checked" : "";
        $arg["data"]["OUTGO_CHECK2"] = knjCreateCheckBox($objForm, "OUTGO_CHECK2", "1", $extra);
        
        //口座振替証明書
        $extra  = "id=\"OUTGO_CHECK3\"";
        $extra .= ($Row["OUTGO_CHECK3"] == "1") ? " checked" : "";
        $arg["data"]["OUTGO_CHECK3"] = knjCreateCheckBox($objForm, "OUTGO_CHECK3", "1", $extra);
        
        /************/
        /* 決裁     */
        /************/
        
        //支出日
        $arg["data"]["OUTGO_DATE"] = View::popUpCalendar($objForm, "OUTGO_DATE",str_replace("-","/",$Row["OUTGO_DATE"]),"");
        
        //精算
        $opt = array(1, 2);
        $model->field["OUTGO_EXPENSE_FLG"] = ($model->field["OUTGO_EXPENSE_FLG"] == "") ? "1" : $model->field["OUTGO_EXPENSE_FLG"];
        $extra = array("id=\"OUTGO_EXPENSE_FLG1\"", "id=\"OUTGO_EXPENSE_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "OUTGO_EXPENSE_FLG", $model->field["OUTGO_EXPENSE_FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //証書枚数
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["OUTGO_CERTIFICATE_CNT"] = knjCreateTextBox($objForm, $Row["OUTGO_CERTIFICATE_CNT"], "OUTGO_CERTIFICATE_CNT", 3, 3, $extra);
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp905_approvalForm1.html", $arg); 
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
function makeBtn(&$objForm, &$arg, $model) {
    //更 新
    if ($model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        $extra = " onclick=\"return btn_submit('update');\"";
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    
    //戻る
    $extra = "";
    $subdata  = "wopen('".REQUESTROOT."/P/KNJP905_MAIN/knjp905_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
    $subdata .= "&SEND_PRGID=KNJP905_APPROVAL&SEND_OUTGO_L_CD={$model->getOutgoLcd}&SEND_OUTGO_M_CD={$model->getOutgoMcd}";
    $subdata .= "&SEND_SCHOOL_KIND={$model->getSchoolKind}&SEND_REQUEST_NO={$model->getRequestNo}";
    $subdata .= "&SEND_OUTGO_L_M_CD={$model->getOutgoLMcd}&SEND_YEAR={$model->getYear}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $extra = "onclick=\"$subdata\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
