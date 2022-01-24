<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjg052aForm1.php 56814 2017-10-27 09:33:53Z maeshiro $

class knjg052aForm1 {

    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjg052aForm1", "POST", "knjg052aindex.php", "", "knjg052aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //卒業年度
        if ($model->field["YEAR"] == "") $model->field["YEAR"] = CTRL_YEAR;
        $arg["data"]["YEAR"] = $model->field["YEAR"] . "年度";

        //切替ラジオボタン 1:クラス指定 2:個人指定
//        $opt = array(1, 2);
//        if (!$model->output) $model->output = 1;
//        $click = " onclick =\" return btn_submit('knjg052a');\"";
//        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click);
//        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->output, $extra, $opt, get_count($opt));
//        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
//        if ($model->output == 1) $arg["clsno"] = $model->output;
//        if ($model->output == 2) $arg["schno"] = $model->output;
        $model->output = 2;

        //学期
        $model->field["SEMESTER"] = $model->field["SEMESTER"] == '' ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query = knjg052aQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('knjg052a');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, $model);
 
        //学年・クラスコンボ
        if ($model->output == 1) {
            $query = knjg052aQuery::getGrade($model);
        } else {
            $query = knjg052aQuery::getGradeClass($model);
        }
        $extra = "onChange=\"return btn_submit('cmbclass');\"";
        makeCmb($objForm, $arg, $db, $query, "CMBCLASS", $model->field["CMBCLASS"], $extra, 1, $model);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //記載日付
        $value = isset($model->field["NOTICEDAY"])?$model->field["NOTICEDAY"]:str_replace("-","/",$model->control["学籍処理日"]);
        $arg["el"]["NOTICEDAY"] = View::popUpCalendar($objForm, "NOTICEDAY", $value);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg052aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
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

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    //リスト取得
    if ($model->output == 1) $query = knjg052aQuery::getGradeClassList($model);
    if ($model->output == 2) $query = knjg052aQuery::getStudentList($model);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //リスト作成
    $extra = "multiple style=\"width:220px; height:280px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", isset($opt1)?$opt1:array(), $extra, 15);

    //対象リスト
    $extra = "multiple style=\"width:220px; height:280px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

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
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJG052A");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]);
    knjCreateHidden($objForm, "certifPrintRealName", $model->Properties["certifPrintRealName"]);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
?>
