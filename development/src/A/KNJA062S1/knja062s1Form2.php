<?php

require_once('for_php7.php');

class knja062s1Form2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja062s1index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning)) {
            $Row = $db->getRow(knja062s1Query::getData($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->fields;
        }

        //担任区分コンボ
        $query = knja062s1Query::getTrDiv();
        makeCombo($objForm, $arg, $db, $query, $Row["TR_DIV"], "TR_DIV", $extra, 1, "BLANK");

        //開始日付
        $arg["data"]["FROM_DATE"] = View::popUpCalendar($objForm, "FROM_DATE",str_replace("-","/",$Row["FROM_DATE"]),"");

        //終了日付
        $arg["data"]["TO_DATE"] = View::popUpCalendar($objForm, "TO_DATE",str_replace("-","/",$Row["TO_DATE"]),"");

        //担任
        $query = knja062s1Query::getStaff($model);
        makeCombo($objForm, $arg, $db, $query, $Row["STAFFCD"], "STAFFCD", $extra, 1, "BLANK");

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::post("cmd") != "edit" && VARS::post("cmd") != "" && !isset($model->warning)){
            $arg["reload"]  = "window.open('knja062s1index.php?cmd=updlist','left_frame');";
        }

        View::toHTML($model, "knja062s1Form2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, $value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"closeMethod();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
}

?>
