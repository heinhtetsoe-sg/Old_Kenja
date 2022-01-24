<?php

require_once('for_php7.php');

class knjz291_staff_addressForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz291_staff_addressindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query = knjz291_staff_addressQuery::getEdboardSchool();
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
        View::toHTML($model, "knjz291_staff_addressForm1.html", $arg);
    }
}

function makeList(&$objForm, &$arg, $db, $db2, &$model) {

    $query = knjz291_staff_addressQuery::getStaffName($model);
    $staffName = $db->getOne($query);
    $arg["TOP"]["STAFFCD"] = $model->sendStaffcd;
    $arg["TOP"]["STAFFNAME"] = $staffName;

    $query = knjz291_staff_addressQuery::getList($model);
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
        $query = knjz291_staff_addressQuery::getData($model, $model->clickSdate);
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

    //郵便番号
    $extra = "";
    $arg["data2"]["STAFFZIPCD"] = View::popUpZipCode($objForm, "STAFFZIPCD", $setData["STAFFZIPCD"], "STAFFADDR1");

    //住所1
    $extra = "";
    $arg["data2"]["STAFFADDR1"] = knjCreateTextBox($objForm, $setData["STAFFADDR1"], "STAFFADDR1", 50, 90, $extra);

    //住所2
    $extra = "";
    $arg["data2"]["STAFFADDR2"] = knjCreateTextBox($objForm, $setData["STAFFADDR2"], "STAFFADDR2", 50, 90, $extra);

    //電話番号
    $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
    $arg["data2"]["STAFFTELNO"] = knjCreateTextBox($objForm, $setData["STAFFTELNO"], "STAFFTELNO", 14, 14, $extra);

    //FAX番号
    $extra = "";
    $arg["data2"]["STAFFFAXNO"] = knjCreateTextBox($objForm, $setData["STAFFFAXNO"], "STAFFFAXNO", 14, 14, $extra);

    //E-Mail
    $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
    $arg["data2"]["STAFFE_MAIL"] = knjCreateTextBox($objForm, $setData["STAFFE_MAIL"], "STAFFE_MAIL", 25, 25, $extra);

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
