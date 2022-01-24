<?php

require_once('for_php7.php');

class knja227bForm1 {
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja227bForm1", "POST", "knja227bindex.php", "", "knja227bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //塾コード
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["SEARCH_PRISCHOOLCD"] = knjCreateTextBox($objForm, $model->field["SEARCH_PRISCHOOLCD"], "SEARCH_PRISCHOOLCD", 7, 7, $extra);

        //塾名
        $extra = "";
        $arg["data"]["SEARCH_PRISCHOOL_NAME"] = knjCreateTextBox($objForm, $model->field["SEARCH_PRISCHOOL_NAME"], "SEARCH_PRISCHOOL_NAME", 40, 40, $extra);

        //教室名
        $extra = "";
        $arg["data"]["SEARCH_PRISCHOOL_CLASS_NAME"] = knjCreateTextBox($objForm, $model->field["SEARCH_PRISCHOOL_CLASS_NAME"], "SEARCH_PRISCHOOL_CLASS_NAME", 40, 40, $extra);

        //読込ボタン
        $extra = "onclick=\"return btn_submit('search');\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "読 込", $extra);

        //塾一覧リスト作成する
        makeListToList($objForm, $arg, $db, $model);

        //説明会日時
        //日
        if (!strlen($model->field["BRIEFING_DATE"]) && $model->cmd == "") $model->field["BRIEFING_DATE"] = str_replace("-","/", CTRL_DATE);
        $arg["data"]["BRIEFING_DATE"] = View::popUpCalendar($objForm, "BRIEFING_DATE", $model->field["BRIEFING_DATE"]);
        //午前、午後
        $extra = "";
        $query = knja227bQuery::getBriefingAmPm();
        if (!strlen($model->field["BRIEFING_AMPM"]) && $model->cmd == "") $model->field["BRIEFING_AMPM"] = "1";
        makeCmb($objForm, $arg, $db, $query, "BRIEFING_AMPM", $model->field["BRIEFING_AMPM"], $extra, 1, "BLANK");
        //時
        $extra = "";
        $query = knja227bQuery::getBriefingHour();
        if (!strlen($model->field["BRIEFING_HOUR"]) && $model->cmd == "") $model->field["BRIEFING_HOUR"] = "11";
        makeCmb($objForm, $arg, $db, $query, "BRIEFING_HOUR", $model->field["BRIEFING_HOUR"], $extra, 1, "BLANK");
        //分
        $extra = "";
        $query = knja227bQuery::getBriefingMinute();
        makeCmb($objForm, $arg, $db, $query, "BRIEFING_MINUTE", $model->field["BRIEFING_MINUTE"], $extra, 1, "BLANK");

        //資料送付日
        if (!strlen($model->field["SEND_DATE"]) && $model->cmd == "") $model->field["SEND_DATE"] = str_replace("-","/", CTRL_DATE);
        $arg["data"]["SEND_DATE"] = View::popUpCalendar($objForm, "SEND_DATE", $model->field["SEND_DATE"]);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja227bForm1.html", $arg); 
    }
}

//塾一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //塾一覧
    $row1 = array();
    $result = $db->query(knja227bQuery::selectPriSchoolQuery($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    $arg["data"]["NAME_LIST"] = '塾';

    //塾一覧作成
    $extra = "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"movec('left','SCHOOL_SELECTED','SCHOOL_NAME',1)\"";
    $arg["data"]["SCHOOL_NAME"] = knjCreateCombo($objForm, "SCHOOL_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"movec('right','SCHOOL_SELECTED','SCHOOL_NAME',1)\"";
    $arg["data"]["SCHOOL_SELECTED"] = knjCreateCombo($objForm, "SCHOOL_SELECTED", "", array(), $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"movec('sel_add_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"movec('left','SCHOOL_SELECTED','SCHOOL_NAME',1);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"movec('right','SCHOOL_SELECTED','SCHOOL_NAME',1);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"movec('sel_del_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJA227B");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "ENTEXAMYEAR", CTRL_YEAR + 1);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
?>
