<?php

require_once('for_php7.php');

class knjh566bForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh566bForm1", "POST", "knjh566bindex.php", "", "knjh566bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjh566bQuery::getSemesterMst();
        $extra = "onchange=\"return btn_submit('knjh566b')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $query = knjh566bQuery::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('knjh566b')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //出力順位
        $opt = array(1, 2);
        $model->field["JUNI"] = ($model->field["JUNI"] == "") ? "2" : $model->field["JUNI"];
        $extra = array("id=\"JUNI1\"", "id=\"JUNI2\"", "id=\"JUNI3\"");
        $radioArray = knjCreateRadio($objForm, "JUNI", $model->field["JUNI"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位の基準点
        $opt = array(1, 2);
        $model->field["KIJUNTEN"] = ($model->field["KIJUNTEN"] == "") ? "2" : $model->field["KIJUNTEN"];
        $extra = array("id=\"KIJUNTEN1\"", "id=\"KIJUNTEN2\"");
        $radioArray = knjCreateRadio($objForm, "KIJUNTEN", $model->field["KIJUNTEN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh566bForm1.html", $arg); 
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    //クラス一覧リスト作成する
    $row1 = array();
    $query = knjh566bQuery::getStudent($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //出力対象クラスリストを作成する
    $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJH566B");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}
?>
