<?php

require_once('for_php7.php');

class knjz220eForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz220eindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = $db->getRow(knjz220eQuery::getRow($model, $model->grade), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //学年コンボ
        $extra = "";
        $query = knjz220eQuery::getGrade($model);
        $model->field["GRADE"] = $model->field["GRADE"] ? $model->field["GRADE"] : $model->grade;
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "BLANK");

        //算出比率（％）
        $extra = " style=\"text-align:center;\" onblur=\"this.value=calc(this);\" ";
        $arg["data"]["MIKOMI_RATE"] = knjCreateTextBox($objForm, $Row["MIKOMI_RATE"], "MIKOMI_RATE", 2, 2, $extra);

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

        if ($model->cmd == "update_end"){
            $arg["reload"]  = "window.open('knjz220eindex.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz220eForm2.html", $arg);
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
