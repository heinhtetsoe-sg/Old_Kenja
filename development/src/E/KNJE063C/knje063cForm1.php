<?php

require_once('for_php7.php');

class knje063cForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje063cForm1", "POST", "knje063cindex.php", "", "knje063cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //変換対象 年度
        $query = knje063cQuery::getHenkanMotoYear($model);
        $extra = "onchange=\"return btn_submit('knje063c')\"";
        makeCmb($objForm, $arg, $db, $query, "HENKAN_MOTO_YEAR", $model->field["HENKAN_MOTO_YEAR"], $extra, 1);

        //変換対象 変換元科目
        $query = knje063cQuery::getSubclass($model->field["HENKAN_MOTO_YEAR"]);
        $extra = "onchange=\"return btn_submit('knje063c')\"";
        makeCmb($objForm, $arg, $db, $query, "HENKAN_MOTO_SUBCLASS", $model->field["HENKAN_MOTO_SUBCLASS"], $extra, 1);

        //変換条件１ 年度
        $query = knje063cQuery::getHenkanJoken1Year($model);
        $extra = "onchange=\"return btn_submit('knje063c')\"";
        makeCmb($objForm, $arg, $db, $query, "HENKAN_JOKEN_1_YEAR", $model->field["HENKAN_JOKEN_1_YEAR"], $extra, 1, "BLANK");

        //変換条件１ 取得条件科目
        $query = knje063cQuery::getSubclass($model->field["HENKAN_JOKEN_1_YEAR"]);
        $extra = "onchange=\"return btn_submit('knje063c')\"";
        makeCmb($objForm, $arg, $db, $query, "SYUTOKU_JOKEN_SUBCLASS", $model->field["SYUTOKU_JOKEN_SUBCLASS"], $extra, 1, "BLANK");

        //変換条件１ ラジオボタン 1:受講 2:非受講
        $opt_disp = array(1, 2);
        $model->field["ATTEND_DIV"] = ($model->field["ATTEND_DIV"] == "") ? "1" : $model->field["ATTEND_DIV"];
        $extra = array("id=\"ATTEND_DIV1\" onClick=\"return btn_submit('knje063c')\"", "id=\"ATTEND_DIV2\" onClick=\"return btn_submit('knje063c')\"");
        $radioArray = knjCreateRadio($objForm, "ATTEND_DIV", $model->field["ATTEND_DIV"], $extra, $opt_disp, get_count($opt_disp));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //変換対象２ 取得単位数
        $extra = "onblur=\"return btn_submit('knje063c')\" ";
        if ($model->field["ATTEND_DIV"] == "2") {
            $extra .= "disabled ";
        }
        $arg["data"]["CREGIT_CNT"] = knjCreateTextBox($objForm, $model->field["CREGIT_CNT"], "CREGIT_CNT", 4, 2, $extra);

        //変換後 変換後科目
        $query = knje063cQuery::getSubclass($model->field["HENKAN_MOTO_YEAR"], $model->field["HENKAN_MOTO_SUBCLASS"]);
        $extra = "onchange=\"return btn_submit('knje063c')\"";
        makeCmb($objForm, $arg, $db, $query, "HENKAN_GO_SUBCLASS", $model->field["HENKAN_GO_SUBCLASS"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje063cForm1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //対象外の生徒取得
    $query = knje063cQuery::getStudyrec($model->field["HENKAN_MOTO_YEAR"], $model->field["HENKAN_GO_SUBCLASS"]);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //初期化
    $opt_right = $opt_left = array();

    //左リストで選択されたものを再セット
    $selectdata = ($model->selectdata != "") ? explode(",", $model->selectdata) : array();
    $selectdataText = ($model->selectdataText != "") ? explode(",", $model->selectdataText) : array();
    for ($i = 0; $i < get_count($selectdata); $i++) {
        $opt_left[] = array("label" => $selectdataText[$i],
                            "value" => $selectdata[$i]);
    }

    //対象者リスト
    $query = knje063cQuery::getTargetStudent($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
        if (!in_array($row["SCHREGNO"], $selectdata)) {
            $opt_right[] = array('label' => $row["HR_NAME"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME"],
                'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //実行ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJE063C");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataText");
}
?>
