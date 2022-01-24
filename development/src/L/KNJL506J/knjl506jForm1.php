<?php

require_once('for_php7.php');

class knjl506jForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear;

        //学校種別コンボ
        $query = knjl506jQuery::getNameMst($model->examyear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試種別コンボ
        $query = knjl506jQuery::getTestDiv($model->examyear, $model->field["APPLICANTDIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "");

        //入試方式コンボ
        $query = knjl506jQuery::getExamType($model->examyear, $model->field["APPLICANTDIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->field["EXAM_TYPE"], $extra, 1, "BLANK");

        //帳票種類ラジオボタン（1:得点チェックリスト、2:成績一覧表）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ソート(得点チェックリスト用)ラジオボタン（1:整理番号順、2:受験番号順）
        $opt = array(1, 2);
        $model->field["SORT1"] = ($model->field["SORT1"] == "") ? "1" : $model->field["SORT1"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT1{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT1", $model->field["SORT1"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ソート(成績一覧表用)ラジオボタン（1:成績順、2:受験番号順）
        $opt = array(1, 2);
        $model->field["SORT2"] = ($model->field["SORT2"] == "") ? "1" : $model->field["SORT2"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SORT2{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT2", $model->field["SORT2"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //成績順(小計・合計)コンボ
        $opt = array();
        if ($model->field["APPLICANTDIV"] == "1") {
            $opt[] = array("label" => "小計", "value" => "1");
        }
        $opt[] = array("label" => "合計", "value" => "2");
        $extra = "";
        $arg["data"]["SUBSORT"] = knjCreateCombo($objForm, "SUBSORT", $model->field["SUBSORT"], $opt, $extra, 1);

        //csvボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL506J");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl506jindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl506jForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
