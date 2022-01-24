<?php

require_once('for_php7.php');

class knje388Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knje388Form1", "POST", "knje388index.php", "", "knje388Form1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学年コンボ
        $query = knje388Query::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "onChange=\"return btn_submit('knje388');\"", 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //カレンダーコントロール
        $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
        $arg["el"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);

        //出力順ラジオ
        $opt = array(1, 2); //1:ベストスコア順 2:年組番
        $model->field["SORT_ORDER"] = ($model->field["SORT_ORDER"] == "") ? "1" : $model->field["SORT_ORDER"];
        $extra = array("id=\"SORT_ORDER1\"", "id=\"SORT_ORDER2\"");
        $radioArray = knjCreateRadio($objForm, "SORT_ORDER", $model->field["SORT_ORDER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knje388Form1.html", $arg); 
    }
}
/*********************************************** 以下関数 *******************************************************/
/**********/
/* コンボ */
/**********/
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "") {
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    $value = ($value == "") ? $opt[0]["value"] : $value;

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

/******************/
/* リストToリスト */
/******************/
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $row1 = array();
    $result = $db->query(knje388Query::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px; height:350px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px; height:350px;\"  ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

/**********/
/* ボタン */
/**********/
function makeBtn(&$objForm, &$arg, $model) {
    //印刷ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "', '');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

/**********/
/* hidden */
/**********/
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
    knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
    knjCreateHidden($objForm, "PRGID",         "KNJE388");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
}
?>
