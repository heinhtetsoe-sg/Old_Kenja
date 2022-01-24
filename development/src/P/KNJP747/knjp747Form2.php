<?php

require_once('for_php7.php');

class knjp747Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp747index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            if ($model->cmd === 'bank') {
                $Row =& $model->field;
            } else {
                $Row = knjp747Query::getRow($model, $model->schoolKind, $model->formatDiv);
            }
        } else {
            $Row =& $model->field;
        }

        //db接続
        $db = Query::dbCheckOut();

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp747Query::getSchkind($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "KYOUTSU");

        //フォーマット区分(1:引落 2:返金)
        $opt = array(1, 2);
        $Row["FORMAT_DIV"] = ($Row["FORMAT_DIV"] == "") ? "1" : $Row["FORMAT_DIV"];
        $Row["FORMAT_DIV"] = ($model->field["FORMAT_DIV"] == "") ? $Row["FORMAT_DIV"] : $model->field["FORMAT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"FORMAT_DIV{$val}\" onClick=\"btn_submit('edit')\"");
        }
        $radioArray = knjCreateRadio($objForm, "FORMAT_DIV", $Row["FORMAT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($Row["FORMAT_DIV"] == '1') {
            $arg["FEE_TITLE"]  = '引落';
            $arg["HIKI_FLG"]   = '1';
            $arg["HENKIN_FLG"] = '';
        } else {
            $arg["FEE_TITLE"]  = '返金';
            $arg["HIKI_FLG"]   = '';
            $arg["HENKIN_FLG"] = '1';
        }

        //契約種別コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHUBETSU_CD"] = knjCreateTextBox($objForm, $Row["SHUBETSU_CD"], "SHUBETSU_CD", 2, 2, $extra);

        //JCコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JC_CD"] = knjCreateTextBox($objForm, $Row["JC_CD"], "JC_CD", 2, 2, $extra);

        //事業主番号
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ACCOUNT_CD"] = knjCreateTextBox($objForm, $Row["ACCOUNT_CD"], "ACCOUNT_CD", 8, 8, $extra);

        //事業主名(ｶﾅ)
        $extra = "";
        $arg["data"]["ACCOUNTNAME_KANA"] = knjCreateTextBox($objForm, $Row["ACCOUNTNAME_KANA"], "ACCOUNTNAME_KANA", 41, 120, $extra);

        //金融機関コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BANK_CD"] = knjCreateTextBox($objForm, $Row["BANK_CD"], "BANK_CD", 4, 4, $extra);

        //金融機関名(ｶﾅ)
        $extra = "";
        $arg["data"]["BANKNAME_KANA"] = knjCreateTextBox($objForm, $Row["BANKNAME_KANA"], "BANKNAME_KANA", 16, 45, $extra);

        //振替口座記号
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BRANCHCD"] = knjCreateTextBox($objForm, $Row["BRANCHCD"], "BRANCHCD", 3, 3, $extra);

        //振替口座番号
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ACCOUNTNO"] = knjCreateTextBox($objForm, $Row["ACCOUNTNO"], "ACCOUNTNO", 6, 6, $extra);

        //手数料
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BANK_TRANSFER_FEE"] = knjCreateTextBox($objForm, $Row["BANK_TRANSFER_FEE"], "BANK_TRANSFER_FEE", 3, 3, $extra);

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
            $arg["reload"]  = "window.open('knjp747index.php?cmd=list','left_frame');";
        }

        //db切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp747Form2.html", $arg);
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
