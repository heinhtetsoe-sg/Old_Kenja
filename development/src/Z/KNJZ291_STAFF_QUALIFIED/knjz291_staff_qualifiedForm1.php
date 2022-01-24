<?php

require_once('for_php7.php');

class knjz291_staff_qualifiedForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz291_staff_qualifiedindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query = knjz291_staff_qualifiedQuery::getEdboardSchool();
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
        View::toHTML($model, "knjz291_staff_qualifiedForm1.html", $arg);
    }
}

function makeList(&$objForm, &$arg, $db, $db2, &$model) {

    $query = knjz291_staff_qualifiedQuery::getStaffName($model);
    $staffName = $db->getOne($query);
    $arg["TOP"]["STAFFCD"] = $model->sendStaffcd;
    $arg["TOP"]["STAFFNAME"] = $staffName;

    $query = knjz291_staff_qualifiedQuery::getList($model);
    $result = $db2->query($query);
    $model->staffCd = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["GET_DATE"] = str_replace("-", "/", $row["GET_DATE"]);

        //LINK
        $extra = "onClick=\"document.forms[0].clickSeq.value = '".$row["SEQ"]."'; btn_submit('click');\"";
        $row["SEQ"] = View::alink("#", htmlspecialchars($row["SEQ"]), $extra);

        $arg["data"][] = $row;
    }
    $result->free();

}

function makeInputScreen(&$objForm, &$arg, $db, $db2, &$model) {

    if ($model->cmd == "click" && !isset($model->warning)) {
        $query = knjz291_staff_qualifiedQuery::getData($model);
        $setData = $db2->getRow($query, DB_FETCHMODE_ASSOC);
    } else {
        $setData = $model->field;
    }

    //SEQ
    $arg["data2"]["SEQ"] = $setData["SEQ"];
    knjCreateHidden($objForm, "SEQ", $setData["SEQ"]);

    //免許・資格選択
    $query = knjz291_staff_qualifiedQuery::getNameMst("Z031");
    $extra = "";
    makeCmb($objForm, $arg, $db, $query, $setData["QUALIFIED_CD"], "QUALIFIED_CD", $extra, 1, "BLANK");

    //免許・資格入力
    $extra = "";
    $arg["data2"]["QUALIFIED_NAME"] = knjCreateTextBox($objForm, $setData["QUALIFIED_NAME"], "QUALIFIED_NAME", 80, 100, $extra);

    //取得日
    $setData["GET_DATE"] = str_replace("-", "/", $setData["GET_DATE"]);
    $arg["data2"]["GET_DATE"] = View::popUpCalendar($objForm, "GET_DATE", $setData["GET_DATE"], "", "");

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

    //終了ボタン
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "clickSeq");
}
?>
