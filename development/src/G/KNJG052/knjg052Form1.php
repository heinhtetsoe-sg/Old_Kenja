<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjg052Form1.php 58564 2018-02-14 06:05:25Z maeshiro $

class knjg052Form1 {

    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjg052Form1", "POST", "knjg052index.php", "", "knjg052Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //卒業年度
        if ($model->field["YEAR"] == "") $model->field["YEAR"] = CTRL_YEAR;
        $arg["data"]["YEAR"] = $model->field["YEAR"] . "年度";

        //学期
        if ($model->field["GAKKI"] == "") $model->field["GAKKI"] = CTRL_SEMESTER;

        //印刷種別ラジオボタン 1:修了証明書 2:卒業証明書
        $opt_kind = array(1, 2);
        $model->field["KIND"] = ($model->field["KIND"] == "") ? "1" : $model->field["KIND"];
        $extra = array("id=\"KIND1\"", "id=\"KIND2\"");
        $radioArray = knjCreateRadio($objForm, "KIND", $model->field["KIND"], $extra, $opt_kind, get_count($opt_kind));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //切替ラジオボタン 1:クラス指定 2:個人指定
        $opt = array(1, 2);
        if (!$model->output) $model->output = 1;
        $click = " onclick =\" return btn_submit('knjg052');\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->output, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        if ($model->output == 1) $arg["clsno"] = $model->output;
        if ($model->output == 2) $arg["schno"] = $model->output;

        //学年・クラスコンボ
        if ($model->output == 1) {
            $query = knjg052Query::getGrade($model);
        } else {
            $query = knjg052Query::getGradeClass($model);
        }
        $extra = "onChange=\"return btn_submit('cmbclass');\"";
        makeCmb($objForm, $arg, $db, $query, "CMBCLASS", $model->field["CMBCLASS"], $extra, 1, $model);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //記載日付
        $value = isset($model->field["NOTICEDAY"])?$model->field["NOTICEDAY"]:str_replace("-","/",$model->control["学籍処理日"]);
        $arg["el"]["NOTICEDAY"] = View::popUpCalendar($objForm, "NOTICEDAY", $value);

        //卒業年月
        $value = isset($model->field["GRADUATE_DATE"])?$model->field["GRADUATE_DATE"]:str_replace("-","/",$model->control["学校卒業日"]);
        $arg["el"]["GRADUATE_DATE"] = View::popUpCalendar($objForm, "GRADUATE_DATE", $value);

        //句点チェックボックス
        $extra  = ($model->field["POINT"] == "1") ? "checked" : "";
        $extra .= " id=\"POINT\"";
        $arg["data"]["POINT"] = knjCreateCheckBox($objForm, "POINT", $model->field["POINT"], $extra, "");

        // 印影選択チェックボックス
        if ($model->Properties["knjg010usePrintStamp"] == '1') {
            $arg["showPrintStamp"] = "1";
            if ($model->field["PRINT_STAMP"] == "1") {
                $extra = "checked='checked' ";
            } else {
                $extra = "";
            }
            $extra .= " id='PRINT_STAMP'";
            $arg["data"]["PRINT_STAMP"] = knjCreateCheckBox($objForm, "PRINT_STAMP", "1", $extra);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg052Form1.html", $arg); 
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

    if ($name == "CMBCLASS" && $model->cmd != "cmbclass") {
        $value = $opt[0]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    //リスト取得
    if ($model->output == 1) $query = knjg052Query::getGradeClassList($model);
    if ($model->output == 2) $query = knjg052Query::getStudentList($model);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //リスト作成
    $extra = "multiple style=\"width:220px\" width=\"220px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", isset($opt1)?$opt1:array(), $extra, 15);

    //対象リスト
    $extra = "multiple style=\"width:220px\" width=\"220px\" ondblclick=\"move1('right')\"";
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
    knjCreateHidden($objForm, "YEAR", $model->field["YEAR"]);
    knjCreateHidden($objForm, "GAKKI", $model->field["GAKKI"]);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJG052");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]);
    knjCreateHidden($objForm, "DOCUMENTROOT",  DOCUMENTROOT);
}
?>
