<?php

require_once('for_php7.php');

class knjp705Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp705index.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            if ($model->cmd === 'bank') {
                $Row =& $model->field;
            } else {
                $Row = knjp705Query::getRow($model,1);
            }
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //学校払込コード
        //textbox
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COLLECT_BANK_CD"] = knjCreateTextBox($objForm, $Row["COLLECT_BANK_CD"], "COLLECT_BANK_CD", 4, 4, $extra);

        //学校払込名称
        $extra = "";
        $arg["data"]["COLLECT_BANK_NAME"] = knjCreateTextBox($objForm, $Row["COLLECT_BANK_NAME"], "COLLECT_BANK_NAME", 20, 40, $extra);

        //銀行種別
        $opt = array();
        $opt[] = array('label' => '1:銀行', 'value' => '1');
        $opt[] = array('label' => '2:郵貯', 'value' => '2');
        $Row["COLLECT_BANK_DIV"] = ($Row["COLLECT_BANK_DIV"] == "") ? '1' : $Row["COLLECT_BANK_DIV"];
        $extra = "onchange=\"return btn_submit('bank');\"";
        $arg["data"]["COLLECT_BANK_DIV"] = knjCreateCombo($objForm, "COLLECT_BANK_DIV", $Row["COLLECT_BANK_DIV"], $opt, $extra, 1);
                        
        //画面の切替
        if ($Row["COLLECT_BANK_DIV"] === '1') {
            $arg["useBank"] = '1';
        } else {
            $arg["useYuucyo"] = '1';
        }
        
        //銀行用---------------------------
        //銀行コード
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $query = knjp705Query::getBankcd();
        $result = $db->query($query);
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row1["LABEL"],
                           'value' => $row1["VALUE"]);
            if ($Row["BANK_CD"] == $row1["VALUE"]) $value_flg = true;
        }
        $Row["BANK_CD"] = ($Row["BANK_CD"] && $value_flg) ? $Row["BANK_CD"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('bank');\"";
		if ($Row["BANK_CD"] !== $model->field["BANK_CD"] && strlen($model->field["BANK_CD"]) != 0) {
        	$Row["BANK_CD"] = $model->field["BANK_CD"];
        }
        $arg["data"]["BANK_CD"] = knjCreateCombo($objForm, "BANK_CD", $Row["BANK_CD"], $opt, $extra, 1);

        //支店コード
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $query = knjp705Query::getBanchcd($Row["BANK_CD"]);
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row2["LABEL"],
                           'value' => $row2["VALUE"]);
            if ($Row["BRAN_CHCD"] == $row2["VALUE"]) $value_flg = true;
        }
        $Row["BRAN_CHCD"] = ($Row["BRAN_CHCD"] && $value_flg) ? $Row["BRAN_CHCD"] : $opt[0]["value"];
		if ($Row["BRAN_CHCD"] !== $model->field["BRAN_CHCD"] && strlen($model->field["BRAN_CHCD"]) != 0) {
        	$Row["BANK_CD"] = $model->field["BRAN_CHCD"];
        }
        $extra = "";
        $arg["data"]["BRAN_CHCD"] = knjCreateCombo($objForm, "BRAN_CHCD", $Row["BRAN_CHCD"], $opt, $extra, 1);

		//預金種別
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $query = knjp705Query::getNameMst("G203");
        $value_flg = false;
        $result = $db->query($query);
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row3["LABEL"],
                           'value' => $row3["VALUE"]);
        }
        $extra = "";
        $arg["data"]["BANK_DEPOSIT_ITEM"] = knjCreateCombo($objForm, "BANK_DEPOSIT_ITEM", $Row["BANK_DEPOSIT_ITEM"], $opt, $extra, 1);

        //口座番号
        $extra = "style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BANK_ACCOUNTNO"] = knjCreateTextBox($objForm, $Row["BANK_ACCOUNTNO"], "BANK_ACCOUNTNO", 7, 7, $extra);

        //口座名
        $extra = "";
        $arg["data"]["BANK_ACCOUNTNAME"] = knjCreateTextBox($objForm, $Row["BANK_ACCOUNTNAME"], "BANK_ACCOUNTNAME", 30, 60, $extra);

        //郵便局用----------------------------
        
        //口座記号
        $extra = "style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["YUUCYO_CD"] = knjCreateTextBox($objForm, $Row["YUUCYO_CD"], "YUUCYO_CD", 5, 5, $extra);
        
		//預金種別
        $extra = "style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["YUUCYO_DEPOSIT_ITEM"] = knjCreateTextBox($objForm, $Row["YUUCYO_DEPOSIT_ITEM"], "YUUCYO_DEPOSIT_ITEM", 1, 1, $extra);

        //口座番号
        $extra = "style=\"text-align:right\" onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["YUUCYO_ACCOUNTNO"] = knjCreateTextBox($objForm, $Row["YUUCYO_ACCOUNTNO"], "YUUCYO_ACCOUNTNO", 8, 8, $extra);

        //口座名
        $extra = "";
        $arg["data"]["YUUCYO_ACCOUNTNAME"] = knjCreateTextBox($objForm, $Row["YUUCYO_ACCOUNTNAME"], "YUUCYO_ACCOUNTNAME", 60, 60, $extra);
        
        //共通----------------------------------
        //郵便番号
        $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $Row["ZIPCD"],"ADDR1");
    
        //住所1
        $extra = "";
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $Row["ADDR1"], "ADDR1", 50, 150, $extra);
        
        //住所2
        $extra = "";
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $Row["ADDR2"], "ADDR2", 50, 75, $extra);
    
        //電話番号1
        $extra = "";
        $arg["data"]["TELNO1"] = knjCreateTextBox($objForm, $Row["TELNO1"], "TELNO1", 14, 14, $extra);

        //電話番号2
        $extra = "";
        $arg["data"]["TELNO2"] = knjCreateTextBox($objForm, $Row["TELNO2"], "TELNO2", 14, 14, $extra);

		//ここまで-------
        //追加
        $extra = "onclick=\"return btn_submit('add')\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
        
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", CTRL_YEAR);
        
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjp705index.php?cmd=list','left_frame');";
        }
        
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp705Form2.html", $arg);
    }
}
?>
