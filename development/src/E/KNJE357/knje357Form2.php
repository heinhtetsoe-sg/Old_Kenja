<?php

require_once('for_php7.php');

class knje357Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje357index.php", "", "edit");

        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (($model->advertise_div != "") && !isset($model->warning) && $model->cmd != 'chenge_cd'&& $model->cmd != 'search'){
            $query = knje357Query::getPrgDat($model, $model->advertise_div, $model->program_cd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }else{
            $Row =& $model->field;
        }

        //募集区分
        $query = knje357Query::getAdveMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["ADVERTISE_DIV"], "ADVERTISE_DIV", $extra, 1, "BLANK");

        //日程
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PROGRAM_CD"] = knjCreateTextBox($objForm, $Row["PROGRAM_CD"], "PROGRAM_CD", 2, 2, $extra);

        //日程名
        $extra = "";
        $arg["data"]["PROGRAM_NAME"] = knjCreateTextBox($objForm, $Row["PROGRAM_NAME"], "PROGRAM_NAME", 50, 50, $extra);

        //日程略称
        $extra = "";
        $arg["data"]["PROGRAM_ABBV"] = knjCreateTextBox($objForm, $Row["PROGRAM_ABBV"], "PROGRAM_ABBV", 50, 50, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit" && $model->cmd != 'chenge_cd'){
            $arg["reload"]  = "parent.left_frame.location.href='knje357index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje357Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
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
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
