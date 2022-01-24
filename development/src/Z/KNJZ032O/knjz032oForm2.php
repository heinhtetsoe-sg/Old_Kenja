<?php

require_once('for_php7.php');

class knjz032oForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz032oindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz032oQuery::getRow($model->year,$model->testdiv);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //入試区分テキスト
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["TESTDIV"] = knjCreateTextBox($objForm, $Row["TESTDIV"], "TESTDIV", 2, 1, $extra);

        //名称テキスト
        $extra = "";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 40, $extra);

        //略称テキスト
        $extra = "";
        $arg["data"]["ABBV"] = knjCreateTextBox($objForm, $Row["ABBV"], "ABBV", 20, 20, $extra);

        //クラス区分コンボ
        $query = knjz032oQuery::getClassdiv($model->year);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "CLASSDIV", $Row["CLASSDIV"], $extra, 1, "BLANK");

        //表示区分チェック
        $checked = strlen($Row["SHOWDIV"]) ? "checked" : "";
        $extra = "id=\"SHOWDIV\" " .$checked;
        $arg["data"]["SHOWDIV"] = knjCreateCheckBox($objForm, "SHOWDIV", "1", $extra);

        //定員テキスト
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CAPACITY"] = knjCreateTextBox($objForm, $Row["CAPACITY"], "CAPACITY", 3, 3, $extra);

        //入試日
        $arg["data"]["TESTDAY"] = View::popUpCalendar($objForm, "TESTDAY", str_replace("-", "/", $Row["TESTDAY"]), "");

        //表示順テキスト
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER"] = knjCreateTextBox($objForm, $Row["SHOWORDER"], "SHOWORDER", 2, 1, $extra);

        //加点対象区分コンボ
        $query = knjz032oQuery::getKatendiv($model->year);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "KATENDIV", $Row["KATENDIV"], $extra, 1, "BLANK");

        //ボタン
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        knjCreateHidden($objForm, "year", $model->year);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz032oindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz032oForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
