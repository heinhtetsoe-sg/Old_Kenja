<?php

require_once('for_php7.php');

class knjz291_staff_requestformForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz291_staff_requestformindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query = knjz291_staff_requestformQuery::getEdboardSchool();
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
        View::toHTML($model, "knjz291_staff_requestformForm1.html", $arg);
    }
}

function makeList(&$objForm, &$arg, $db, $db2, &$model) {

    $query = knjz291_staff_requestformQuery::getStaffName($model);
    $staffName = $db->getOne($query);
    $arg["TOP"]["STAFFCD"] = $model->sendStaffcd;
    $arg["TOP"]["STAFFNAME"] = $staffName;

    $query = knjz291_staff_requestformQuery::getList($model);
    $result = $db2->query($query);
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
        $query = knjz291_staff_requestformQuery::getData($model);
        $setData = $db2->getRow($query, DB_FETCHMODE_ASSOC);
    } else {
        $setData = $model->field;
    }

    //開始日付
    $setData["SDATE"] = str_replace("-", "/", $setData["SDATE"]);
    $arg["data2"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $setData["SDATE"], "", "");

    //終了日付
    $setData["EDATE"] = str_replace("-", "/", $setData["EDATE"]);
    $arg["data2"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $setData["EDATE"], "", "");

    //所属区分
    $query = knjz291_staff_requestformQuery::getNameMst("Z028", "SITEI");
    $extra = "";
    makeCmb($objForm, $arg, $db, $query, $setData["WORK_DIV"], "WORK_DIV", $extra, 1, "BLANK");

    //理由
    $extra = "";
    $arg["data2"]["REASON"] = knjCreateTextBox($objForm, $setData["REASON"], "REASON", 100, 100, $extra);

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

    $disable = ($model->sendAuth == DEF_UPDATABLE) ? "" : " disabled";

    //新規ボタン
    $extra = "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "新 規", $extra.$disable);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "更 新", $extra.$disable);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disable);

    //戻るボタン
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "clickSdate");
}
?>
