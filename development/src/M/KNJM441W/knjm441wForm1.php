<?php

require_once('for_php7.php');

class knjm441wForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm441wForm1", "POST", "knjm441windex.php", "", "knjm441wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期ラジオボタン
        $semester = "";
        $model->field["SEMESTER"] = ($model->field["SEMESTER"] == "") ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query = knjm441wQuery::getSemesterMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($semester != "") $semester .= "　　";
            $extra  = ($model->field["SEMESTER"] == $row["VALUE"]) ? "checked" : "";
            $extra .= " onclick =\" return btn_submit('main');\"";
            $semester .= "<input type='radio' name='SEMESTER' value={$row["VALUE"]} {$extra} id='SEMESTER{$row["VALUE"]}'><label for='SEMESTER{$row["VALUE"]}'> {$row["LABEL"]}</label>";
        }
        $arg["data"]["SEMESTER"] = $semester;

        //テスト種別
        $opt = array();
        $query = knjm441wQuery::getTestcd($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["TESTCD"] = $model->field["TESTCD"] ? $model->field["TESTCD"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjm441w'), AllClearList();\"";
        $arg["data"]["TESTCD"] = knjCreateCombo($objForm, "TESTCD", $model->field["TESTCD"], $opt, $extra, 1);

        //科目コンボ
        $query = knjm441wQuery::getSubclassList($model);
        $extra = "onchange=\"return btn_submit('knjm441w'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASS", $model->field["SUBCLASS"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        $arg["TOP"]["CTRL_YEAR"]        = knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        $arg["TOP"]["CTRL_DATE"]        = knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        $arg["TOP"]["DBNAME"]           = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        $arg["TOP"]["PRGID"]            = knjCreateHidden($objForm, "PRGID", "KNJM441W");
        $arg["TOP"]["USE_CURRICULUMCD"] = knjCreateHidden($objForm, "USE_CURRICULUMCD", $model->Properties["useCurriculumcd"]);

        //更新->印刷であれば、ここで印刷指定
        if ($model->cmd == 'print') {
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm441wForm1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //対象生徒
    $opt_l = $list = array();
    $query = knjm441wQuery::getPassStudentList($model);

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_l[] = array('label' => $row["LABEL"],
                         'value' => $row["VALUE"]);
        $list[] = $row["VALUE"];
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_l, $extra, 20);

    //生徒一覧
    $opt_r = array();
    $query = knjm441wQuery::getStudentList($model, $list);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_r[] = array('label' => $row["LABEL"],
                         'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_r, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onClick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //受験票発行ボタン
    $extra = "onclick=\"return btn_submit('updateprint');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "受験票 発行", $extra);
}
?>
