<?php

require_once('for_php7.php');

class knjz291_staff_name_histForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz291_staff_name_histindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query = knjz291_staff_name_histQuery::getEdboardSchool();
        $model->edboard_schoolcd = $db->getOne($query);

        //リスト表示
        makeList($objForm, $arg, $db, $db2, $model);

        //登録画面
        makeInputScreen($objForm, $arg, $db, $db2, $model);

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz291_staff_name_histForm1.html", $arg);
    }
}

function makeList(&$objForm, &$arg, $db, $db2, &$model) {

    $query = knjz291_staff_name_histQuery::getStaffName($model);
    $staffName = $db->getOne($query);
    $arg["TOP"]["STAFFCD"] = $model->sendStaffcd;
    $arg["TOP"]["STAFFNAME"] = $staffName;

    $query = knjz291_staff_name_histQuery::getList($model);
    $result = ($model->sendStaffDiv == "2") ? $db->query($query) : $db2->query($query);
    $model->staffCd = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["SDATE"] = str_replace("-", "/", $row["SDATE"]);
        $row["EDATE"] = str_replace("-", "/", $row["EDATE"]);

        //LINK
        $extra = "onClick=\"document.forms[0].clickSdate.value = '".$row["SDATE"]."'; btn_submit('click');\"";
        $row["SDATE"] = View::alink("#", htmlspecialchars($row["SDATE"]), $extra);

        $arg["data"][] = $row;
    }
    $result->free();

}

function makeInputScreen(&$objForm, &$arg, $db, $db2, &$model) {

    if ($model->cmd == "click" && !isset($model->warning)) {
        $query = knjz291_staff_name_histQuery::getData($model, $model->clickSdate);
        $setData = ($model->sendStaffDiv == "2") ? $db->getRow($query, DB_FETCHMODE_ASSOC) : $db2->getRow($query, DB_FETCHMODE_ASSOC);
    } else {
        $setData = $model->field;
    }

    //開始日付
    $setData["SDATE"] = str_replace("-", "/", $setData["SDATE"]);
    $arg["data2"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $setData["SDATE"], "", "");

    //終了日付
    $setData["EDATE"] = str_replace("-", "/", $setData["EDATE"]);
    $extra = " readonly style=\"background-color:lightgray;\"";
    $arg["data2"]["EDATE"] = knjCreateTextBox($objForm, $setData["EDATE"], "EDATE", 12, "EDATE", $extra);

    //職員氏名
    $extra = "";
    $arg["data2"]["STAFFNAME"] = knjCreateTextBox($objForm, $setData["STAFFNAME"], "STAFFNAME", 40, 60, $extra);

    //職員氏名表示用
    $extra = "";
    $arg["data2"]["STAFFNAME_SHOW"] = knjCreateTextBox($objForm, $setData["STAFFNAME_SHOW"], "STAFFNAME_SHOW", 10, 15, $extra);

    //職員氏名かな
    $extra = "";
    $arg["data2"]["STAFFNAME_KANA"] = knjCreateTextBox($objForm, $setData["STAFFNAME_KANA"], "STAFFNAME_KANA", 80, 120, $extra);

    //職員氏名英字
    $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
    $arg["data2"]["STAFFNAME_ENG"] = knjCreateTextBox($objForm, $setData["STAFFNAME_ENG"], "STAFFNAME_ENG", 40, 60, $extra);

    //戸籍氏名
    $extra = "";
    $arg["data2"]["STAFFNAME_REAL"] = knjCreateTextBox($objForm, $setData["STAFFNAME_REAL"], "STAFFNAME_REAL", 80, 120, $extra);

    //戸籍氏名かな
    $extra = "";
    $arg["data2"]["STAFFNAME_KANA_REAL"] = knjCreateTextBox($objForm, $setData["STAFFNAME_KANA_REAL"], "STAFFNAME_KANA_REAL", 80, 240, $extra);

}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data2"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function makeButton(&$objForm, &$arg, &$model) {
    //新規ボタン
    $extra = "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "新 規", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);

    //戻るボタン
    $extra = "onclick=\"closeMethod();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "clickSdate");
}
?>
