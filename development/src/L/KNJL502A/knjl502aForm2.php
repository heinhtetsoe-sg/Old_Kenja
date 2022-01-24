<?php

require_once('for_php7.php');

class knjl502aForm2 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl502aindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjl502aQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        $model->year = CTRL_YEAR + 1;

        //入試区分コンボ
        $query = knjl502aQuery::getNameMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "BLANK");

        //志望区分コンボ
        $query = knjl502aQuery::getHopeCoursecode($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["HOPE_COURSECODE"], "HOPE_COURSECODE", $extra, 1, "BLANK");

        //各教科テキスト
        $extra = "";
        $arg["data"]["CLASS_SCORE"] = knjCreateTextBox($objForm, $Row["CLASS_SCORE"], "CLASS_SCORE", 2, 2, $extra);

        //５科合計テキスト
        $extra = "";
        $arg["data"]["SCORE5"] = knjCreateTextBox($objForm, $Row["SCORE5"], "SCORE5", 2, 2, $extra);

        //９科合計テキスト
        $extra = "";
        $arg["data"]["SCORE9"] = knjCreateTextBox($objForm, $Row["SCORE9"], "SCORE9", 2, 2, $extra);

        //保体無視チェックボックス
        $extra = "";
        $checked = ($Row["HEALTH_PE_DISREGARD"] == "1") ? " checked" : "";
        $arg["data"]["HEALTH_PE_DISREGARD"] = knjCreateCheckBox($objForm, "HEALTH_PE_DISREGARD", "1", $extra.$checked);

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
        knjCreateHidden($objForm, "year", $model->year);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjl502aindex.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl502aForm2.html", $arg);
    }
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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
