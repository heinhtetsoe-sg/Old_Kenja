<?php

require_once('for_php7.php');


class knjp707Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp707index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["CTRL_YEAR"] = CTRL_YEAR;

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp707Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeKind');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1);

        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $result = $db->query(knjp707Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["COLLECT_PATTERN_CD"] = View::alink("knjp707index.php", $row["COLLECT_PATTERN_CD"], "target=\"right_frame\"",
                                                array("cmd"                 => "edit",
                                                      "COLLECT_PATTERN_CD"  => $row["COLLECT_PATTERN_CD"]
                                                      ));

            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "changeKind") {
            $arg["reload"] = "window.open('knjp707index.php?cmd=edit&INTO_PATTERN_CD=','right_frame');";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp707Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
