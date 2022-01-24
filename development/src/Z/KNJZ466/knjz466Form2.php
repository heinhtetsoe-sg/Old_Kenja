<?php

require_once('for_php7.php');

class knjz466Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz466index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz466Query::getRow($model, $model->scholarship_cd);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            //校種コンボ
            $query = knjz466Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('list');\"";
            makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");
        }

        //コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SCHOLARSHIP"] = knjCreateTextBox($objForm, $Row["SCHOLARSHIP"], "SCHOLARSHIP", 2, 2, $extra);

        //名称
        $extra = "";
        $arg["data"]["SCHOLARSHIP_NAME"] = knjCreateTextBox($objForm, $Row["SCHOLARSHIP_NAME"], "SCHOLARSHIP_NAME", 60, 60, $extra);

        //略称
        $extra = "";
        $arg["data"]["SCHOLARSHIP_ABBV"] = knjCreateTextBox($objForm, $Row["SCHOLARSHIP_ABBV"], "SCHOLARSHIP_ABBV", 10, 10, $extra);

        //追加
        $extra = "onclick=\"return btn_submit('add')\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
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
            $arg["reload"]  = "window.open('knjz466index.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz466Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
