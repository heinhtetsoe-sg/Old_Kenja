<?php

require_once('for_php7.php');

class knjh441bForm1 {

    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh441bForm1", "POST", "knjh441bindex.php", "", "knjh441bForm1");
        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = CTRL_YEAR;
        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjh441bQuery::getSchoolkind($model);
            $extra = "onchange=\"return btn_submit('init');\"";
            $model->field['SCHOOL_KIND'] = $model->field['SCHOOL_KIND'] ? $model->field['SCHOOL_KIND'] : SCHOOLKIND;
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1);
        }

        //学年リスト作成
        $query = knjh441bQuery::getGrade($model);
        $result = $db->query($query);
        $extra = " onchange=\"btn_submit('init');\" ";
        makeCmb($objForm, $arg, $db, $query, $model->field['GRADE'], "GRADE", $extra, 1);

        // 模試一覧
        makeListToList($objForm, $arg, $db, $model, "TESTINFO");
        // 科目一覧
        makeListToList($objForm, $arg, $db, $model, "SUBCLASS");

        //ボタン作成
        makeButton($objForm, $arg);
        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh441bForm1.html", $arg); 
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

// 出力対象選択＆出力対象一覧を作成
function makeListToList(&$objForm, &$arg, $db, $model, $category) {

    $selected = array();
    $opt_right = array();
    $opt_left = array();
    if ($category == "TESTINFO") {
        $query = knjh441bQuery::getTestInfo($model);
        $selected = $model->selectTestInfo;
    } else if ($category == "SUBCLASS") {
        $query = knjh441bQuery::getSubclass($model);
        $selected = $model->selectSubclass;
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $selected)) {
            $opt_right[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $opt_left[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:500px;\" ondblclick=\"move('".$category."', 'left', '')\"";
    $arg["data"][$category."_NAME"] = knjCreateCombo($objForm, $category."_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:500px;\" ondblclick=\"move('".$category."', 'right', '')\"";
    $arg["data"][$category."_SELECTED"] = knjCreateCombo($objForm, $category."_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('".$category."', 'right', 'ALL');\"";
    $arg[$category]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('".$category."', 'left', 'ALL');\"";
    $arg[$category]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('".$category."', 'right', '');\"";
    $arg[$category]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('".$category."', 'left', '');\"";
    $arg[$category]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

}

//ボタン作成
function makeButton(&$objForm, &$arg) {

    //csvボタンを作成する
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "実行", $extra);
    //ヘッダ有チェックボックス
    $extra = " checked ";
    $arg["chk_header"] = knjCreateCheckBox($objForm, "chk_header", "1", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

}

//Hidden作成
function makeHidden(&$objForm, $db, $model) {

    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectTestInfo");
    knjCreateHidden($objForm, "selectSubclass");
}

?>
