<?php

require_once('for_php7.php');

class knjl502aForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl502aindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $model->year = CTRL_YEAR + 1;
        $arg["YEAR"] = $model->year;

        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $result = $db->query(knjl502aQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["TESTDIV"] = View::alink("knjl502aindex.php", $row["TESTDIV"]. ":" .$row["NAME1"], "target=\"right_frame\"",
                                                array("cmd"        => "edit",
                                                      "TESTDIV"    => $row["TESTDIV"],
                                                      "HOPE_COURSECODE" => $row["HOPE_COURSECODE"],
                                                      ));
            $row["HOPE_COURSECODE"] = $row["HOPE_COURSECODE"]. ":" . $row["HOPE_NAME"];
            $row["HEALTH_PE_DISREGARD"] = ($row["HEALTH_PE_DISREGARD"] == "1") ? "レ" : "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        // hidden
        knjCreateHidden($objForm, "cmd");

        if(!isset($model->warning) && (VARS::post("cmd") == "copy")) {
            $arg["reload"] = "parent.right_frame.location.href='knjl502aindex.php?cmd=edit"
                           . "&year=".$model->year."'; ";
        }
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl502aForm1.html", $arg);
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
