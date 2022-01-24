<?php

require_once('for_php7.php');

class knje464Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje464index.php", "", "edit");
        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knje464Query::getRow($model, $model->facility_cd);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //コード
        $extra = "id=\"SPRT_FACILITY_CD\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SPRT_FACILITY_CD"] = knjCreateTextBox($objForm, $Row["SPRT_FACILITY_CD"], "SPRT_FACILITY_CD", 3, 3, $extra);

        //名称
        $extra = "id=\"SPRT_FACILITY_NAME\"";
        $arg["data"]["SPRT_FACILITY_NAME"] = knjCreateTextBox($objForm, $Row["SPRT_FACILITY_NAME"], "SPRT_FACILITY_NAME", 20, 10, $extra);

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
            $arg["reload"]  = "window.open('knje464index.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje464Form2.html", $arg);
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
