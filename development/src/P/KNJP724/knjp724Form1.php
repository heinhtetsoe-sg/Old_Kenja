<?php

require_once('for_php7.php');

class knjp724Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp724index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $model->year = ($model->year) ? $model->year : CTRL_YEAR;
        $query = knjp724Query::getYear();
        $extra = "onChange=\"btn_submit('changeYear')\"";
        makeCmb($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1, "");

        //校種コンボ
        $query = knjp724Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeKind');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $result = $db->query(knjp724Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["PREFECTURESCD"] = View::alink("knjp724index.php", $row["PREFECTURESCD"], "target=\"right_frame\"",
                                                array("cmd"            => "edit",
                                                      "PREFECTURESCD"  => $row["PREFECTURESCD"],
                                                      "GRADE"          => $row["GRADE"],
                                                      "YEAR"           => $model->year
                                                      ));

            //金額をカンマ区切りにする
            $row["STANDARD_SCHOOL_FEE"] = (strlen($row["STANDARD_SCHOOL_FEE"])) ? number_format($row["STANDARD_SCHOOL_FEE"]): "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if(!isset($model->warning) && (VARS::post("cmd") == "copy" || VARS::post("cmd") == "changeYear")) {
            $arg["reload"] = "parent.right_frame.location.href='knjp724index.php?cmd=edit"
                           . "&year=".$model->year."';";
        }
        if ($model->cmd == "changeKind") {
            $arg["jscript"] = "window.open('knjp724index.php?cmd=edit','right_frame');";
        }
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp724Form1.html", $arg);
    }
}
//makeCmb
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
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
