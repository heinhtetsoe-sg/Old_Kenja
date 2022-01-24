<?php

require_once('for_php7.php');

class knjl338fForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl338fForm1", "POST", "knjl338findex.php", "", "knjl338fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('knjl338f');\"";
        $query = knjl338fQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //合格者ラジオ
        $opt = array(1, 2, 3, 4, 5);
        $model->field["PASSDIV"] = ($model->field["PASSDIV"] == "") ? "1" : $model->field["PASSDIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PASSDIV{$val}\" onClick=\"OptionUse(this)\"");
        }
        $radioArray = knjCreateRadio($objForm, "PASSDIV", $model->field["PASSDIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受験番号
        $extra = ($model->field["PASSDIV"] != "2") ? " disabled " : "";
        $arg["data"]["RECEPTNO"] = knjCreateTextBox($objForm, $model->field["RECEPTNO"], "RECEPTNO", 5, 5, $extra);

        //受験番号
        $extra = ($model->field["PASSDIV"] != "3") ? " disabled " : "";
        $arg["data"]["RECEPTNO2S"] = knjCreateTextBox($objForm, $model->field["RECEPTNO2S"], "RECEPTNO2S", 5, 5, $extra);
        $arg["data"]["RECEPTNO2E"] = knjCreateTextBox($objForm, $model->field["RECEPTNO2E"], "RECEPTNO2E", 5, 5, $extra);

        //受験番号
        $extra = ($model->field["PASSDIV"] != "4") ? " disabled " : "";
        $arg["data"]["RECEPTNO4S"] = knjCreateTextBox($objForm, $model->field["RECEPTNO4S"], "RECEPTNO4S", 5, 5, $extra);
        $arg["data"]["RECEPTNO4E"] = knjCreateTextBox($objForm, $model->field["RECEPTNO4E"], "RECEPTNO4E", 5, 5, $extra);

        //行コンボボックス
        $setOpt = array("1" => "１行", "2" => "２行", "3" => "３行", "4" => "４行", "5" => "５行", "6" => "６行", "7" => "７行", "8" => "８行");
        $opt = array();
        foreach ($setOpt as $key => $val) {
            $opt[] = array('label' => $val,
                           'value' => $key);
        }
        $model->field["GYOU"] = ($model->field["GYOU"]) ? $model->field["GYOU"] : "1";
        $extra = "";
        $arg["data"]["GYOU"] = knjCreateCombo($objForm, "GYOU", $model->field["GYOU"], $opt, $extra, 1);

        //列コンボボックス
        $setOpt = array("1" => "１列", "2" => "２列", "3" => "３列");
        $opt = array();
        foreach ($setOpt as $key => $val) {
            $opt[] = array('label' => $val,
                           'value' => $key);
        }
        $model->field["RETU"] = ($model->field["RETU"]) ? $model->field["RETU"] : "1";
        $extra = "";
        $arg["data"]["RETU"] = knjCreateCombo($objForm, "RETU", $model->field["RETU"], $opt, $extra, 1);

        //住所印刷ラジオ
        if ($model->field["APPLICANTDIV"] == "2") {
            $arg["PRTADDR"] = 1;
            $opt = array(1, 2);
            $model->field["PRINTADDRESS"] = ($model->field["PRINTADDRESS"] == "") ? "1" : $model->field["PRINTADDRESS"];
            $extra = array();
            foreach($opt as $key => $val) {
                array_push($extra, " id=\"PRINTADDRESS{$val}\"");
            }
            $radioArray = knjCreateRadio($objForm, "PRINTADDRESS", $model->field["PRINTADDRESS"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        } else {
            $arg["PRTADDR"] = 0;
        }

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL338F");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl338fForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="", $all="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    if ($all) $opt[] = array("label" => "-- 全て --", "value" => "ALL");
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
