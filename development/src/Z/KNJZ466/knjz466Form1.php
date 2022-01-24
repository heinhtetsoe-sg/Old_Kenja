<?php

require_once('for_php7.php');

class knjz466Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz466index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = CTRL_YEAR;

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjz466Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjz466Query::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["SCHOLARSHIP"] = View::alink("knjz466index.php", $row["SCHOLARSHIP"], "target=\"right_frame\"",
                                         array("cmd"            => "edit",
                                               "SCHOLARSHIP"    => $row["SCHOLARSHIP"]));

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"]  = "window.open('knjz466index.php?cmd=edit','right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz466Form1.html", $arg);
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
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
