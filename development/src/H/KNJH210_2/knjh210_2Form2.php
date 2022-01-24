<?php

require_once('for_php7.php');

class knjh210_2Form2
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh210_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != 'edit2') {
            $Row = $db->getRow(knjh210_2Query::getRow($model, $model->domicd), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
            $Row["DOMI_CD"] = $model->field["DOMI_CD"];
        }

        //寮コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["DOMI_CD"] = knjCreateTextBox($objForm, $Row["DOMI_CD"], "DOMI_CD", 5, 3, $extra);

        //寮名
        $extra = "";
        $arg["data"]["DOMI_NAME"] = knjCreateTextBox($objForm, $Row["DOMI_NAME"], "DOMI_NAME", 122, 60, $extra);

        //郵便番号
        $arg["data"]["DOMI_ZIPCD"] = View::popUpZipCode($objForm, "DOMI_ZIPCD", $Row["DOMI_ZIPCD"],"DOMI_ADDR1");

        //住所1
        $extra = "";
        $arg["data"]["DOMI_ADDR1"] = knjCreateTextBox($objForm, $Row["DOMI_ADDR1"], "DOMI_ADDR1", 152, 75, $extra);

        //住所2
        $extra = "";
        $arg["data"]["DOMI_ADDR2"] = knjCreateTextBox($objForm, $Row["DOMI_ADDR2"], "DOMI_ADDR2", 152, 75, $extra);

        //電話番号1
        $extra = "";
        $arg["data"]["DOMI_TELNO"] = knjCreateTextBox($objForm, $Row["DOMI_TELNO"], "DOMI_TELNO", 16, 14, $extra);

        //電話番号2
        $extra = "";
        $arg["data"]["DOMI_TELNO2"] = knjCreateTextBox($objForm, $Row["DOMI_TELNO2"], "DOMI_TELNO2", 16, 14, $extra);

        //FAX番号
        $extra = "";
        $arg["data"]["DOMI_FAXNO"] = knjCreateTextBox($objForm, $Row["DOMI_FAXNO"], "DOMI_FAXNO", 16, 14, $extra);

        //寮長名
        $extra = "";
        $arg["data"]["DOMI_LEADER"] = knjCreateTextBox($objForm, $Row["DOMI_LEADER"], "DOMI_LEADER", 122, 60, $extra);

        $arg["useCollectMoneySchool"] = "";
        if ($model->Properties["useCollectMoneySchool"] == "1") {
            $arg["useCollectMoneySchool"] = 1;

            //校種
            $query = knjh210_2Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('edit2');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $Row["SCHOOL_KIND"], $extra, 1, "BLANK");

            //入金項目
            $query = knjh210_2Query::getLMmst($Row["SCHOOL_KIND"]);
            $extra = "onchange=\"return btn_submit('edit2');\"";
            makeCmb($objForm, $arg, $db, $query, "COLLECT_LM_CD", $Row["COLLECT_LM_CD"], $extra, 1, "BLANK");

            //一時退寮費
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data"]["MONTH_MONEY"] = knjCreateTextBox($objForm, $Row["MONTH_MONEY"], "MONTH_MONEY", 8, 8, $extra);

            //一時退寮費(日)(現在は未使用2018.12.05)
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $arg["data"]["DAY_MONEY"] = knjCreateTextBox($objForm, $Row["DAY_MONEY"], "DAY_MONEY", 8, 8, $extra);
        }

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", $extra);

        //修正ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return Btn_reset('edit');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $link = REQUESTROOT."/H/KNJH210/knjh210index.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjh210_2index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh210_2Form2.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
