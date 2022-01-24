<?php

require_once('for_php7.php');
class knjc110bForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc110bForm1", "POST", "knjc110bindex.php", "", "knjc110bForm1");

        $db = Query::dbCheckOut();

        //年度テキスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボを作成する
        $query = knjc110bQuery::getSemester();
        $extra = "onChange=\"return btn_submit('knjc110b');\"";
        $row = makeCombo($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //クラス選択コンボボックスを作成する
        $query = knjc110bQuery::getAuth(CTRL_YEAR, $model->field["SEMESTER"]);
        $extra = "onChange=\"return btn_submit('knjc110b');\"";
        $row = makeCombo($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        //カレンダーコントロール１
        $value = isset($model->field["DATE1"]) ? $model->field["DATE1"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$value);

        //カレンダーコントロール２
        $value2 = isset($model->field["DATE2"]) ? $model->field["DATE2"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm,"DATE2",$value2);

        //生徒一覧リストを作成する/
        $query = knjc110bQuery::getStudent(CTRL_YEAR, $model->field["GRADE_HR_CLASS"], $model->field["SEMESTER"]);
        $extra = "multiple style=\"width:240px\" width:\"240px\" ondblclick=\"move1('left')\"";
        $opt1 = makeCombo($objForm, $arg, $db, $query, $hohoge, "CATEGORY_NAME", $extra, 20);

        //対象者一覧リストを作成する
        $name = "category_selected";
        $value = '';
        $options = array();
        $extra = "multiple style=\"width:240px\" width:\"240px\" ondblclick=\"move1('right')\"";
        $size = "20";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, $name, $value, $options, $extra, $size);

        //対象選択ボタンを作成する（全部）/
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, 'btn_rights', '>>', $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, 'btn_lefts', '<<', $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, 'btn_right1', '＞', $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, 'btn_left1', '＜', $extra);

        //帳票ラジオ
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷と終了ボタンの作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        $schooldiv = $db->getOne(knjc110bQuery::getSchooldiv());
        $query = knjc110bQuery::getSEdate($model->field["SEMESTER"], $schooldiv);
        $seme_se = str_replace("-","/",$db->getOne($query));
        makeHidden($objForm, $model, $seme_se);

        Query::dbCheckIn($db);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc110bForm1.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size) {
    $opt = array();
    $flag = false;
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        $flag = ($row["VALUE"] == $value) ? true : $flag;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $flag) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, 'btn_print', 'プレビュー／印刷', $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);
}
//hidden作成
function makeHidden(&$objForm, $model, $seme_se) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJC110B");
    knjCreateHidden($objForm, "cmd", "");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);//年度データ
    knjCreateHidden($objForm, "SEME_SE", $seme_se);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
    knjCreateHidden($objForm, "knjc110bUsePeriod12Form", $model->Properties["knjc110bUsePeriod12Form"]);
}

?>
