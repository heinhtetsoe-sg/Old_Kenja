<?php

require_once('for_php7.php');

class knje364cForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje364cForm1", "POST", "knje364cindex.php", "", "knje364cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //登録日
        $model->field["ENTRYDATE"] = ($model->field["ENTRYDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $model->field["ENTRYDATE"]);
        $arg["data"]["ENTRYDATE"] = View::popUpCalendar($objForm, "ENTRYDATE", $model->field["ENTRYDATE"]);

        //進路種別
        $coursekind = array(1 => '1：進学');
        foreach ($coursekind as $key => $val) {
            $key = ($key == "0") ? "" : $key;
            $opt_course[] = array('label' => $val, 'value' => $key);
        }
        $extra = "onchange=\"return btn_submit('knje364c');\"";
        $arg["data"]["COURSE_KIND"] = knjCreateCombo($objForm, "COURSE_KIND", $model->field["COURSE_KIND"], $opt_course, $extra, 1);

        //調査名
        $extra = "";
        $query = knje364cQuery::getQuestionnaireList($model);
        makeCmb($objForm, $arg, $db, $query, "QUESTIONNAIRECD", $model->field["QUESTIONNAIRECD"], $extra, 1);

        //校種コンボ
        $extra = "onChange=\"return btn_submit('knje364c')\"";
        $query = knje364cQuery::getVNameMstA023($model);
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje364cForm1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //初期化
    $opt_right = $opt_left = array();

    //左リスト
    $query = knje364cQuery::getListToList($model, true);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array(
            "label"    =>     $row["LABEL"],
            "value"    =>     $row["VALUE"],
        );
    }

    //右リスト
    $query = knje364cQuery::getListToList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array(
            "label"    =>     $row["LABEL"],
            "value"    =>     $row["VALUE"],
        );
    }

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
    //CSV出力ボタン
    $extra = "onclick=\"return btn_submit('csvOutput');\"";
    $arg["button"]["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db) {
    knjCreateHidden($objForm, "PRGID", "KNJE364C");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataText");

    $semes = $db->getRow(knje364cQuery::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}
?>
