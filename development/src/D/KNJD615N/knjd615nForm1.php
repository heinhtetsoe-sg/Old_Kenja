<?php

require_once('for_php7.php');


class knjd615nForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd615nForm1", "POST", "knjd615nindex.php", "", "knjd615nForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd615nQuery::getSemester(0);
        $extra = "onchange=\"return btn_submit('knjd615n'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //学年コンボ作成
        $query = knjd615nQuery::getSchoolName();
        $schoolName = $db->getOne($query);
        $query = knjd615nQuery::getGrade($seme);
        $extra = "onchange=\"return btn_submit('knjd615n'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //テストコンボ作成
        $query = knjd615nQuery::getTest($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", 1);

        //出欠集計日付作成
        $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace('-', '/', $db->getOne(knjd615nQuery::getSemesterSdate())) : $model->field["SDATE"];
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);

        //出欠集計日付作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //クラスリストToリスト作成
        makeClassList($objForm, $arg, $db, $model, $seme);
        
        //総合的な学習の時間チェックボックス
        $extra = ($model->field["OUTPUT5"] == "1") ? "checked" : "";
        $extra .= " id=\"OUTPUT5\"";
        $arg["data"]["OUTPUT5"] = knjCreateCheckBox($objForm, "OUTPUT5", "1", $extra, "");
        
        //ＬＨＲチェックボックス
        $extra = ($model->field["OUTPUT6"] == "1") ? "checked" : "";
        $extra .= " id=\"OUTPUT6\"";
        $arg["data"]["OUTPUT6"] = knjCreateCheckBox($objForm, "OUTPUT6", "1", $extra, "");
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $seme, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd615nForm1.html", $arg); 
    }
}

function makeClassList(&$objForm, &$arg, $db, $model, $seme) {

    //クラス一覧リストを作成する
    $query = knjd615nQuery::getHrClass($seme, $model->field["GRADE"]);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"height:230px;width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt1, $extra, 20);

    //対象クラスリストを作成する
    $extra = "multiple style=\"height:230px;width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
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

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model, $seme, $db) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJD615N");
    knjCreateHidden($objForm, "cmd");

    //年度開始日付
    knjCreateHidden($objForm, "YEAR_SDATE", str_replace('-', '/', $db->getOne(knjd615nQuery::getSemesterSdate())));
    //日付範囲チェック用
    if($model->field["SEMESTER"] == '9'){
        $sseme = $model->control["学期開始日付"][9];
        $eseme = $model->control["学期終了日付"][9];
        $semeflg = CTRL_SEMESTER;
    } else {
        $sseme = $model->control["学期開始日付"][$seme];
        $eseme = $model->control["学期終了日付"][$seme];
        $semeflg = $model->field["SEMESTER"];
    }
    knjCreateHidden($objForm, "SEME_SDATE", $sseme);
    knjCreateHidden($objForm, "SEME_EDATE", $eseme);
    knjCreateHidden($objForm, "SEME_FLG", $semeflg);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

}

?>