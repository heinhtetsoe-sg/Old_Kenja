<?php

require_once('for_php7.php');

class knjh141aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh141aForm1", "POST", "knjh141aindex.php", "", "knjh141aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //エリアコンボ
        $query = knjh141aQuery::getArea();
        $extra = "onchange=\"return btn_submit('knjh141a'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "AREA", $model->field["AREA"], $extra, 1);
        
        //路線コンボ
        $query = knjh141aQuery::getRosen($model->field["AREA"]);
        $extra = "onchange=\"return btn_submit('knjh141a'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "ROSEN", $model->field["ROSEN"], $extra, 1);
        
        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $seme);
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh141aForm1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $seme) {
    //対象者リスト
    $query = knjh141aQuery::getStudent($model, $seme);
    $result = $db->query($query);
    $opt_right = $opt_left = array();
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selectdata)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        } else {
            $opt_right[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //生徒一覧リスト
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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
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
    //csvボタンを作成する
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_print", "ＣＳＶ出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJH141A");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H", $model->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_SIZE_H", $model->Properties["HREPORTREMARK_DAT_SIZE_H"]);
}
?>
