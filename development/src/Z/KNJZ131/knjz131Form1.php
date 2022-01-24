<?php

require_once('for_php7.php');

class knjz131Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz131index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = CTRL_YEAR;

        //校種コンボ
        $query = knjz131Query::getYear();
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1, "");

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjz131Query::getAttendDiCdDat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["DI_CD"] = View::alink("knjz131index.php", $row["DI_CD"], "target=\"right_frame\"",
                                        array("cmd"         => "edit",
                                              "SEND_DI_CD"  => $row["DI_CD"]));
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //button
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"]  = "window.open('knjz131index.php?cmd=edit','right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz131Form1.html", $arg);
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
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
