<?php

require_once('for_php7.php');


class knjl515jForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear;

        //学校種別コンボボックス
        $extra = "onChange=\"return btn_submit('main')\"";
        $query = knjl515jQuery::getNameMst($model->examyear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試種別コンボボックス
        $extra = "";
        $query = knjl515jQuery::getTestDiv($model->examyear, $model->field["APPLICANTDIV"]);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //処理ラジオボタン（1:変換、2:未変換リストCSV）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl515jindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl515jForm1.html", $arg);
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "変換実行", $extra);
    //CSV出力ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "未変換リストCSV出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
}
?>
