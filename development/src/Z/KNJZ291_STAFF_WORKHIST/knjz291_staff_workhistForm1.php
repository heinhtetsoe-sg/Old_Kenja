<?php

require_once('for_php7.php');

class knjz291_staff_workhistForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz291_staff_workhistindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query = knjz291_staff_workhistQuery::getEdboardSchool();
        $model->edboard_schoolcd = $db->getOne($query);

        $query = knjz291_staff_workhistQuery::getKyouikuIinkai();
        $model->kyouikuIinkai = $db->getOne($query);

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
        View::toHTML($model, "knjz291_staff_workhistForm1.html", $arg);
    }
}

function makeList(&$objForm, &$arg, $db, $db2, &$model) {

    $query = knjz291_staff_workhistQuery::getStaffName($model);
    $staffName = $db->getOne($query);
    $arg["TOP"]["STAFFCD"] = $model->sendStaffcd;
    $arg["TOP"]["STAFFNAME"] = $staffName;

    $query = knjz291_staff_workhistQuery::getList($model);
    $result = $db2->query($query);
    $model->staffCd = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["FROM_NAME"] = $row["FROM_NAME"];
        $row["FROM_DATE"] = str_replace("-", "/", $row["FROM_DATE"]);
        if ($model->edboard_schoolcd == $row["FROM_SCHOOLCD"] || $row["FROM_SCHOOLCD"] == "" || $model->kyouikuIinkai > 0) {
            //LINK
            $extra = "onClick=\"document.forms[0].clickFdate.value = '".$row["FROM_DATE"]."'; btn_submit('click');\"";
            $row["FROM_DATE"] = View::alink("#", htmlspecialchars($row["FROM_DATE"]), $extra);
        }
        $row["FROM_SCHOOLCD"] = $row["FROM_SCHOOLCD"].':'.$row["FROM_SCHOOLNAME"];
        $row["TO_NAME"] = $row["TO_NAME"];
        $row["TO_DATE"] = str_replace("-", "/", $row["TO_DATE"]);
        $row["TO_SCHOOLCD"] = $row["TO_SCHOOLCD"].':'.$row["TO_SCHOOLNAME"];
        $arg["data"][] = $row;
    }
    $result->free();

}

function makeInputScreen(&$objForm, &$arg, $db, $db2, &$model) {

    if ($model->cmd == "click" && !isset($model->warning)) {
        $query = knjz291_staff_workhistQuery::getData($model);
        $setData = $db2->getRow($query, DB_FETCHMODE_ASSOC);
    } else {
        $setData = $model->field;
    }

    //開始日付
    $setData["FROM_DATE"] = str_replace("-", "/", $setData["FROM_DATE"]);
    $arg["data2"]["FROM_DATE"] = View::popUpCalendar($objForm, "FROM_DATE", $setData["FROM_DATE"], "", "");

    //学校コード
    $query = knjz291_staff_workhistQuery::getEdSchool($model, $model->kyouikuIinkai);
    $extra = "";
    makeCmb($objForm, $arg, $db2, $query, $setData["FROM_SCHOOLCD"], "FROM_SCHOOLCD", $extra, 1, "BLANK");

    //所属区分
    $query = knjz291_staff_workhistQuery::getNameMst("Z041", "FROM");
    $extra = "";
    makeCmb($objForm, $arg, $db2, $query, $setData["FROM_DIV"], "FROM_DIV", $extra, 1, "BLANK");

    //課程
    $query = knjz291_staff_workhistQuery::getCouser();
    $extra = "";
    if ($model->kyouikuIinkai == 0) {
        makeCmb($objForm, $arg, $db, $query, $setData["FROM_COURSECD"], "FROM_COURSECD", $extra, 1, "BLANK");
    } else {
        makeCmb($objForm, $arg, $db2, $query, $setData["FROM_COURSECD"], "FROM_COURSECD", $extra, 1, "BLANK");
    }

    //開始日付
    $setData["TO_DATE"] = str_replace("-", "/", $setData["TO_DATE"]);
    $arg["data2"]["TO_DATE"] = View::popUpCalendar($objForm, "TO_DATE", $setData["TO_DATE"], "", "");

    //学校コード
    $query = knjz291_staff_workhistQuery::getEdSchool($model, 1);
    $extra = $setData["TO_DIV"] == "03" ? " disabled " : "";
    makeCmb($objForm, $arg, $db2, $query, $setData["TO_SCHOOLCD"], "TO_SCHOOLCD", $extra, 1, "BLANK");

    //所属区分
    $query = knjz291_staff_workhistQuery::getNameMst("Z041", "TO");
    $extra = "onChange=\"setDisabled(this)\"";
    makeCmb($objForm, $arg, $db2, $query, $setData["TO_DIV"], "TO_DIV", $extra, 1, "BLANK");

    //課程
    $query = knjz291_staff_workhistQuery::getCouser();
    $extra = $setData["TO_DIV"] == "03" ? " disabled " : "";
    makeCmb($objForm, $arg, $db2, $query, $setData["TO_COURSECD"], "TO_COURSECD", $extra, 1, "BLANK");

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
    knjCreateHidden($objForm, "clickFdate");
}
?>
