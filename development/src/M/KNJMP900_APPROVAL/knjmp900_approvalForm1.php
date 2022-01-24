<?php

require_once('for_php7.php');


class knjmp900_approvalForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjmp900_approvalindex.php", "", "main");
        
        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd != "check") {
            $Row = knjmp900_approvalQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjmp900_approvalQuery::getCloseFlgData($model));

        //決裁の状況と指定の収入の支出額の合計を取得(キャンセル時に利用)
        if ($model->getIncomeLMcd) {
            $model->getOutgoSumGk = $db->getOne(knjmp900_approvalQuery::getOutgoSumGk($model));
            $model->getApproval = $db->getOne(knjmp900_approvalQuery::getLevyData($model, "APPROVAL"));
            $model->getCancel   = $db->getOne(knjmp900_approvalQuery::getLevyData($model, "CANCEL"));
        } else {
            $model->getApproval = "";
            $model->getCancel   = "";
        }
        knjCreateHidden($objForm, "OUTGO_SUM_GK", $model->getOutgoSumGk);

        //収入科目
        $query = knjmp900_approvalQuery::getLevyMDiv($model);
        $arg["data"]["LEVY_M_NAME"] = $db->getOne($query);

        //収入金額
        $model->getRequestGk = $db->getOne(knjmp900_approvalQuery::getRequestGk($model));
        $arg["data"]["REQUEST_GK"] = $model->getRequestGk;
        knjCreateHidden($objForm, "REQUEST_GK", $model->getRequestGk);

        //収入番号
        $query = knjmp900_approvalQuery::getIncomeNo($model);
        $arg["data"]["INCOME_NO"] = $db->getOne($query);

        /************/
        /* 新規登録 */
        /************/

        //チェックボックス切り替え
        if ($Row["INCOME_CANCEL"] == "1") {
            $setfalse = "false ";
        }

        //決裁済み
        $extra  = "id=\"INCOME_APPROVAL\"";
        $extra .= ($Row["INCOME_APPROVAL"] == "1") ? " checked" : "";
        $arg["data"]["INCOME_APPROVAL"] = knjCreateCheckBox($objForm, "INCOME_APPROVAL", "1", $extra);
        knjCreateHidden($objForm, "INCOME_APPROVAL_VALUE", $Row["INCOME_APPROVAL"]);
        
        //キャンセル
        $extra  = "id=\"INCOME_CANCEL\"";
        $extra .= ($Row["INCOME_CANCEL"] == "1") ? " checked" : "";
        $arg["data"]["INCOME_CANCEL"] = knjCreateCheckBox($objForm, "INCOME_CANCEL", "1", $extra);
        knjCreateHidden($objForm, "INCOME_CANCEL_VALUE", $Row["INCOME_CANCEL"]);

        //収入日
        $arg["data"]["INCOME_DATE"] = View::popUpCalendar($objForm, "INCOME_DATE",str_replace("-","/",$Row["INCOME_DATE"]),"");
        
        //受取人氏名
        $extra = "";
        $query = knjmp900_approvalQuery::getIncomeStaff($model);
        makeCombo($objForm, $arg, $db, $query, $Row["INCOME_STAFF"], "INCOME_STAFF", $extra, 1, "BLANK", $model);
        
        //証書枚数
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["INCOME_CERTIFICATE_CNT"] = knjCreateTextBox($objForm, $Row["INCOME_CERTIFICATE_CNT"], "INCOME_CERTIFICATE_CNT", 3, 3, $extra);
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp900_approvalForm1.html", $arg); 
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
    if ($model->getCancel === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        $extra = " onclick=\"return btn_submit('update');\"";
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    
    //戻る
    $extra = "";
    $subdata  = "wopen('".REQUESTROOT."/M/KNJMP900_MAIN/knjmp900_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
    $subdata .= "&SEND_PRGID=KNJMP900_APPROVAL&SEND_INCOME_L_CD={$model->getIncomeLcd}&SEND_INCOME_M_CD={$model->getIncomeMcd}";
    $subdata .= "&SEND_REQUEST_NO={$model->getRequestNo}";
    $subdata .= "&SEND_INCOME_L_M_CD={$model->getIncomeLMcd}&SEND_YEAR={$model->getYear}";
    $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
    $extra = "onclick=\"$subdata\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
