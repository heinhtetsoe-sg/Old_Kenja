<?php

require_once('for_php7.php');

class knjp747aForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp747aindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            if ($model->cmd === 'bank') {
                $Row =& $model->field;
            } else {
                $Row = knjp747aQuery::getRow($model, $model->schoolKind, $model->formatDiv, $model->bankCd);
            }
        } else {
            $Row =& $model->field;
        }

        //db接続
        $db = Query::dbCheckOut();

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp747aQuery::getSchkind($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "KYOUTSU");

        //フォーマット区分(1:引落 2:返金)
        $opt = array(1, 2);
        $Row["FORMAT_DIV"] = ($Row["FORMAT_DIV"] == "") ? "1" : $Row["FORMAT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"FORMAT_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "FORMAT_DIV", $Row["FORMAT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //項目名セット
        for ($i=1; $i <= 12; $i++) {
            if ($model->nameArr[$i] != '') {
                $arg["ITEM_".$i] = $model->nameArr[$i];
            } else {
                $arg["ITEM_".$i] = "ヘッダー・レコード({$i})";
            }
        }

        //種別コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHUBETSU_CD"] = knjCreateTextBox($objForm, $Row["SHUBETSU_CD"], "SHUBETSU_CD", 2, 2, $extra);

        //コード区分
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CODE_DIV"] = knjCreateTextBox($objForm, $Row["CODE_DIV"], "CODE_DIV", 1, 1, $extra);

        //委託者コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ACCOUNT_CD"] = knjCreateTextBox($objForm, $Row["ACCOUNT_CD"], "ACCOUNT_CD", 10, 10, $extra);

        //委託者名(ｶﾅ)
        $extra = "";
        $arg["data"]["ACCOUNTNAME_KANA"] = knjCreateTextBox($objForm, $Row["ACCOUNTNAME_KANA"], "ACCOUNTNAME_KANA", 41, 120, $extra);

        //銀行コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BANK_CD"] = knjCreateTextBox($objForm, $Row["BANK_CD"], "BANK_CD", 4, 4, $extra);

        //銀行名(ｶﾅ)
        $extra = "";
        $arg["data"]["BANKNAME_KANA"] = knjCreateTextBox($objForm, $Row["BANKNAME_KANA"], "BANKNAME_KANA", 16, 45, $extra);

        //支店コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BRANCHCD"] = knjCreateTextBox($objForm, $Row["BRANCHCD"], "BRANCHCD", 3, 3, $extra);

        //支店名(ｶﾅ)
        $extra = "";
        $arg["data"]["BRANCHNAME_KANA"] = knjCreateTextBox($objForm, $Row["BRANCHNAME_KANA"], "BRANCHNAME_KANA", 16, 45, $extra);

        //預金種目(1:普通 2:当座)
        $opt = array(1, 2);
        $Row["DEPOSIT_TYPE"] = ($Row["DEPOSIT_TYPE"] == "") ? "1" : $Row["DEPOSIT_TYPE"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DEPOSIT_TYPE{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "DEPOSIT_TYPE", $Row["DEPOSIT_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //口座番号
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ACCOUNTNO"] = knjCreateTextBox($objForm, $Row["ACCOUNTNO"], "ACCOUNTNO", 7, 7, $extra);

        //引落手数料
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BANK_TRANSFER_FEE"] = knjCreateTextBox($objForm, $Row["BANK_TRANSFER_FEE"], "BANK_TRANSFER_FEE", 3, 3, $extra);

        //対象銀行区分(1:全て 2:特定銀行)
        $opt = array(1, 2);
        $Row["TARGET_BANK_DIV"] = ($Row["TARGET_BANK_DIV"] == "") ? "1" : $Row["TARGET_BANK_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TARGET_BANK_DIV{$val}\" onClick=\"targetBankDisabled(this)\"");
        }
        $radioArray = knjCreateRadio($objForm, "TARGET_BANK_DIV", $Row["TARGET_BANK_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //特定銀行コード
        $targetDis = ($Row["TARGET_BANK_DIV"] == '1') ? " disabled": "";
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["TARGET_BANK_CD"] = knjCreateTextBox($objForm, $Row["TARGET_BANK_CD"], "TARGET_BANK_CD", 4, 4, $extra.$targetDis);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add')\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", CTRL_YEAR);

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjp747aindex.php?cmd=list','left_frame');";
        }

        //db切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp747aForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "KYOUTSU") {
        $opt[] = array("label" => "共通", "value" => "99");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND && $value != "99") {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
