<?php

require_once('for_php7.php');

class knjp983_2Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp983_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd !== 'edit') {
            $Row = knjp983_2Query::getRow($model->trader_cd);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //業者コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["TRADER_CD"] = knjCreateTextBox($objForm, $Row["TRADER_CD"], "TRADER_CD", 8, 8, $extra);

        //業者名
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["TRADER_NAME"] = knjCreateTextBox($objForm, $Row["TRADER_NAME"], "TRADER_NAME", 45, 120, $extra);

        //郵便番号
        $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $Row["ZIPCD"],"ADDR1");
        
        //住所
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $Row["ADDR1"], "ADDR1", 50, 150, $extra);
        
        //住所2
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $Row["ADDR2"], "ADDR2", 50, 150, $extra);
        
        //銀行コード
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjp983_2Query::getBankCd();
        makeCombo($objForm, $arg, $db, $query, $Row["BANKCD"], "BANKCD", $extra, 1, "BLANK", $model);
        
        //支店コード
        $extra = "";
        $query = knjp983_2Query::getBranchCd($Row["BANKCD"]);
        makeCombo($objForm, $arg, $db, $query, $Row["BRANCHCD"], "BRANCHCD", $extra, 1, "BLANK", $model);
        
        //口座種別
        $extra = "";
        $query = knjp983_2Query::getNameMst("G203");
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
        $query = knjp983_2Query::getNameMst("G217");
        makeCombo($objForm, $arg, $db, $query, $Row["PAY_DIV"], "PAY_DIV", $extra, 1, "BLANK", $model);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/P/KNJP983/knjp983index.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"] = "parent.left_frame.location.href='knjp983_2index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp983_2Form2.html", $arg); 
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
?>
