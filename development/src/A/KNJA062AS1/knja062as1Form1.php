<?php

require_once('for_php7.php');

class knja062as1Form1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knja062as1index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //クラス情報
        $hrInfo = $db->getRow(knja062as1query::getHrInfo($model), DB_FETCHMODE_ASSOC);

        //年度
        $arg["EXE_YEAR"] = $hrInfo["YEAR"];
        $arg["SEMESTER"] = $model->control["学期名"][$model->fields["SEMESTER"]];

        //年組
        $arg["HR_NAME"] = $hrInfo["HR_NAME"];

        //リスト表示
        makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::request("cmd") == "list_edit") {
            $arg["reload"] = "window.open('knja062as1index.php?cmd=edit','right_frame');";
        }

        View::toHTML($model, "knja062as1Form1.html", $arg);
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $result = $db->query(knja062as1query::getList($model, "", "LIST"));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");

        if ($row["SEMESTER"] == $model->getParam["SEMESTER"]) {
            $row["TR_NAME"] = View::alink("knja062as1index.php", $row["TR_NAME"], "target=right_frame",
                                     array("cmd"         => "edit",
                                           "TR_DIV"      => $row["TR_DIV"],
                                           "GRADE"       => $row["GRADE"],
                                           "HR_CLASS"    => $row["HR_CLASS"],
                                           "FROM_DATE"   => $row["FROM_DATE"],
                                           "SEMESTER"    => $row["SEMESTER"],
                                           "YEAR"        => $row["YEAR"]));
        }
        $row["FROM_DATE"] = str_replace("-", "/", $row["FROM_DATE"]);
        $row["TO_DATE"] = str_replace("-", "/", $row["TO_DATE"]);
        $arg["data"][] = $row;
    }
    $result->free();
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $array_value = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        $array_value[] = $row["VALUE"];
    }
    $result->free();

    $value = in_array($value, $array_value) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {
    //コピーボタンを作成する
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の年度からコピー", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
}
?>
