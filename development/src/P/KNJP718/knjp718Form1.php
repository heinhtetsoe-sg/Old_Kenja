<?php

require_once('for_php7.php');

class knjp718Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp718index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        //校種コンボ
        $query = knjp718Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeKind');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //button
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度コピー", $extra);

        //リスト表示
        $result = $db->query(knjp718Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["REDUCTION_ADD_MONEY1"] = (strlen($row["REDUCTION_ADD_MONEY1"])) ? number_format($row["REDUCTION_ADD_MONEY1"]): "";
            $row["REDUCTION_ADD_MONEY1"] = View::alink("knjp718index.php", $row["REDUCTION_ADD_MONEY1"], "target=\"right_frame\"",
                                                array("cmd"                 => "edit",
                                                      "SCHOOLDIV"           => $row["SCHOOLDIV"],
                                                      "GRADE"               => $row["GRADE"],
                                                      "REDUCTION_SEQ"       => $row["REDUCTION_SEQ"]
                                                      ));

            //金額をカンマ区切りにする
            for ($i = 1; $i <= 2; $i++) {
                $row["INCOME_LOW".$i]     = (strlen($row["INCOME_LOW".$i])) ?     number_format($row["INCOME_LOW".$i]): "";
                $row["INCOME_HIGH".$i]    = (strlen($row["INCOME_HIGH".$i])) ?    number_format($row["INCOME_HIGH".$i]): "";
            }
            $row["REDUCTION_ADD_MONEY2"] = (strlen($row["REDUCTION_ADD_MONEY2"])) ? number_format($row["REDUCTION_ADD_MONEY2"]): "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"] = "parent.right_frame.location.href='knjp718index.php?cmd=edit"
                           . "&year=".$model->year."';";
        }
        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeKind") {
            $arg["jscript"] = "window.open('knjp718index.php?cmd=edit','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp718Form1.html", $arg);
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
