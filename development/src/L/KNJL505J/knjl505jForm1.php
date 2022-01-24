<?php

require_once('for_php7.php');

class knjl505jForm1 {

    function main(&$model) {

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear;

        //学校種別コンボ
        $query = knjl505jQuery::getNameMst($model->examyear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試種別コンボ
        $query = knjl505jQuery::getTestDiv($model->examyear, $model->field["APPLICANTDIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "");

        //入試方式コンボ
        $query = knjl505jQuery::getExamType($model->examyear, $model->field["APPLICANTDIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->field["EXAM_TYPE"], $extra, 1, "BLANK");

        //受験番号テキスト
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["RECEPTNO_FROM"] = knjCreateTextBox($objForm, $model->field["RECEPTNO_FROM"], "RECEPTNO_FROM", 10, 10, $extra);
        $arg["data"]["RECEPTNO_TO"]   = knjCreateTextBox($objForm, $model->field["RECEPTNO_TO"],   "RECEPTNO_TO",   10, 10, $extra);

        //表示順(受験番号順・整理番号順)
        $opt = array(1, 2);
        $model->field["OUTPUT_SORT"] = ($model->field["OUTPUT_SORT"] == "") ? "1" : $model->field["OUTPUT_SORT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT_SORT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_SORT", $model->field["OUTPUT_SORT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //帳票種類ラジオボタン（1:入試基礎資料、2:受験者名簿、3:出欠者リスト、4:机上タックシール）
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //CSV出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJL505J");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl505jindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl505jForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

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
?>
