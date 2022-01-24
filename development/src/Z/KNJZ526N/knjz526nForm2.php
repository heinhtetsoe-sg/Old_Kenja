<?php

require_once('for_php7.php');

class knjz526nForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz526nindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合、「読込」ボタン押下時以外の場合
        if (!isset($model->warning) && $model->cmd != "read") {
            $Row = $db->getRow(knjz526nQuery::getRow($model->selfDiv), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //区分
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SELF_DIV"] = knjCreateTextBox($objForm, $Row["SELF_DIV"], "SELF_DIV", 1, 1, $extra);

        //区分（内容）
        $extra = "";
        $arg["data"]["SELF_TITLE"] = knjCreateTextBox($objForm, $Row["SELF_TITLE"], "SELF_TITLE", 40, 20, $extra);

        //項目数
        $Row["ITEM_CNT"] = $Row["ITEM_CNT"] ? $Row["ITEM_CNT"] : $model->itemCnt;
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ITEM_CNT"] = knjCreateTextBox($objForm, $Row["ITEM_CNT"], "ITEM_CNT", 1, 1, $extra);

        //読込ボタン
        $extra = "onclick=\"return btn_submit('read')\"";
        $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);

        //項目の取得
        if (!isset($model->warning)) {
            for ($i = 1; $i <= $Row["ITEM_CNT"]; $i++) {
                $itemRow = $db->getrow(knjz526nQuery::getRow($model->selfDiv, $i), DB_FETCHMODE_ASSOC);
                $itemRow["SELF_ITEM"] = "(".$i.")";
                $extra = "";
                $itemRow["SELF_CONTENT"] = knjCreateTextBox($objForm, $itemRow["SELF_CONTENT"], "SELF_CONTENT_".$i, 80, 50, $extra);

                $arg["list"][] = $itemRow;
            }
        } else {
            for ($i = 1; $i <= $Row["ITEM_CNT"]; $i++) {
                $itemRow["SELF_ITEM"] = "(".$i.")";
                $extra = "";
                $itemRow["SELF_CONTENT"] = knjCreateTextBox($objForm, $Row["SELF_CONTENT_".$i], "SELF_CONTENT_".$i, 80, 150, $extra);

                $arg["list"][] = $itemRow;
            }
        }

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

        if (VARS::get("cmd") != "edit" && $model->cmd != "read"){
            $arg["reload"]  = "window.open('knjz526nindex.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz526nForm2.html", $arg);
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
