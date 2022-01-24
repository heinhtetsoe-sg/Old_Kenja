<?php

require_once('for_php7.php');

class knjz064Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz064index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //対象学部表示
        $arg["SCHOOL_KIND"] = $db->getOne(knjz064Query::getSchoolKind($model->school_kind));

        //状態区分コンボ作成
        $query = knjz064Query::getCondition($model);
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "CONDITION", $model->condition, $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        if (VARS::post("cmd") == "update") {
            $arg["jscript"] = "window.open('knjz064index.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz064Form2.html", $arg);
    }
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    $opt_right = $opt_left = array();
    $selected = array();

    //対象科目一覧取得
    $query = knjz064Query::getGradeKindCompSubclassDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        $selected[] = $row["VALUE"];
    }
    $result->free();

    //対象科目一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', 'CATEGORY_SELECTED', 'CATEGORY_NAME', 1);\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 25);

    //科目一覧取得
    $query = knjz064Query::getSubclassMst($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selected)) continue;
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();

    //科目一覧作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left','CATEGORY_SELECTED','CATEGORY_NAME',1);\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 25);

    //選択ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_add_all','CATEGORY_SELECTED','CATEGORY_NAME',1);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //選択ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('left','CATEGORY_SELECTED','CATEGORY_NAME',1);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //取消ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('right','CATEGORY_SELECTED','CATEGORY_NAME',1);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //取消ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_del_all','CATEGORY_SELECTED','CATEGORY_NAME',1);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //追加ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
