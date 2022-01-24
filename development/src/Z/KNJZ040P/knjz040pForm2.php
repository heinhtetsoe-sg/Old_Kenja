<?php

require_once('for_php7.php');

class knjz040pForm2 {
    function main(&$model) {
        $arg["reload"] = "";

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz040pindex.php", "", "edit");
        $db           = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ($model->isWarning()) {
            $row =& $model->field;
        } else {
            if (VARS::get("cmd") == "edit2") {
                $row = array();
            } else {
                $query = knjz040pQuery::getRecord($model);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
        }

        //入試制度（初期値取得）
        $query = knjz040pQuery::getNameMst($model->year, "L003", $model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $firstNameCd1, "APPLICANTDIV", $extra, 1, "DUMMY", "");

        //入試区分コンボ
        $model->fields["APPLICANTDIV"] = ($model->fields["APPLICANTDIV"] == "") ? $firstNameCd1: $model->fields["APPLICANTDIV"];
        $namecd1 = ($model->fields["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjz040pQuery::getNameMst($model->year, $namecd1, $model);
        makeCmb($objForm, $arg, $db, $query, $row["TESTDIV"], "TESTDIV", "", 1, "");

        //会場名
        $extra = "";
        $arg["EXAMHALL_NAME"] = knjCreateTextBox($objForm, $row["EXAMHALL_NAME"], "EXAMHALL_NAME", 30, 30, $extra);

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

        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "edit2") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz040pindex.php?cmd=list';";
        }

        View::toHTML($model, "knjz040pForm2.html", $arg); 
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
