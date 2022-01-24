<?php

require_once('for_php7.php');

class knjz040qForm2 {
    function main(&$model) {
        $arg["reload"] = "";

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz040qindex.php", "", "edit");
        $db           = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ($model->isWarning()) {
            $row =& $model->field;
        } else {
            $query = knjz040qQuery::getRecord($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //入試区分コンボ
        if (SCHOOLKIND == "J") {
            $query = knjz040qQuery::getNameMst($model->year, "L024", $model);
        } else {
            $query = knjz040qQuery::getNameMst($model->year, "L004", $model);
        }
        makeCmb($objForm, $arg, $db, $query, $row["TESTDIV"], "TESTDIV", "", 1, "");

        //会場名
        $extra = "";
        $arg["EXAMHALL_NAME"] = knjCreateTextBox($objForm, $row["EXAMHALL_NAME"], "EXAMHALL_NAME", 30, 30, $extra);

        //人数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["CAPA_CNT"] = knjCreateTextBox($objForm, $row["CAPA_CNT"], "CAPA_CNT", 5, 4, $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリアボタンを作成する
        $extra = "onclick=\"return Btn_reset('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz040qindex.php?cmd=list';";
        }

        View::toHTML($model, "knjz040qForm2.html", $arg); 
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
