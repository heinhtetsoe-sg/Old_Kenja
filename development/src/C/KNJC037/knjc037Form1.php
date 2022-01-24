<?php

require_once('for_php7.php');

class knjc037Form1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc037Form1", "POST", "knjc037index.php", "", "knjc037Form1");
        $db = Query::dbCheckOut();

        //年度//////////////////////////////////////////////////////////////
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する//////////////////////////////////////
        $query = knjc037Query::getSelectSeme();
        $value = $model->field["GAKKI"];
        $extra = "Onchange=\"btn_submit('change');\"";
        makeCombo($objForm, $arg, $db, $query, $value, 'data', 'GAKKI', $extra, $size, $model);

        //年組リスト
        $query = knjc037Query::getNenKumi($model);
        $value = $model->field["NENKUMI"];
        $extra = "Onchange=\"btn_submit('change');\"";
        makeCombo($objForm, $arg, $db, $query, $value, 'data', 'NENKUMI', $extra, $size, $model);

        //開始日
        $model->field["S_DATE"] = ($model->field["S_DATE"]) ? $model->field["S_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["S_DATE"] = View::popUpCalendar2($objForm, "S_DATE", $model->field["S_DATE"], "", "", "");

        //終了日
        $model->field["E_DATE"] = ($model->field["E_DATE"]) ? $model->field["E_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["E_DATE"] = View::popUpCalendar2($objForm, "E_DATE", $model->field["E_DATE"], "", "", "");

        //一日欠席の時は欠課を出力する
        //checkbox
        if ($model->field["OUT_PUT_KEKKA"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["OUT_PUT_KEKKA"] = knjCreateCheckBox($objForm, "OUT_PUT_KEKKA", "1", $extra);


        //リスト作成する////////////////////////////////////////////////////
        makeListToList($objForm, $arg, $db, $model);

        //プレビュー／印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        $query = knjc037Query::getSemester($model->field["GAKKI"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "PRGID", "KNJC037");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "GAKKI_SDATE", str_replace("-", "/", $row["SDATE"])); //日付チェック用
        knjCreateHidden($objForm, "GAKKI_EDATE", str_replace("-", "/", $row["EDATE"])); //日付チェック用
        knjCreateHidden($objForm, "SEMESTERNAME", $row["SEMESTERNAME"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc037Form1.html", $arg); 
    }
}
/******************************************* 以下関数 *******************************************************/
//////////////
//コンボ作成//
//////////////
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $argName, $name, $extra, $size, &$model, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "NENKUMI") {
        if ($value && $value_flg) {
            $value = $value;
        } else { //一番最初の時はコンボの先頭が初期値になるため「GRADE」と「HR_CLASS」も初期値を設定する
            $value = $opt[0]["value"];
            list($model->field["GRADE"], $model->field["HR_CLASS"]) = explode(':', $opt[0]["value"]);
        }
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
////////////////////////////////
//クラス一覧リストToリスト作成//
////////////////////////////////
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $itiran   = array();
    $selected = array();
    $query = knjc037Query::getSeitoItiran($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $itiran[]= array('label' => $row["LABEL"],
                         'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["ITIRAN"] = knjCreateCombo($objForm, "ITIRAN", "", $itiran, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["SELECTED_DATA"] = knjCreateCombo($objForm, "SELECTED_DATA", "", $selected, $extra, 20);

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

?>
