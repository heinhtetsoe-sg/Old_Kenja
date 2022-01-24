<?php

require_once('for_php7.php');

class knjl357fForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl357fForm1", "POST", "knjl357findex.php", "", "knjl357fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl357f');\"";
        $query = knjl357fQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl357f');\"";
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl357fQuery::getNameMst($model->ObjYear, $namecd1);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "ALL");

        //対象
        $opt = array(1, 2);
        $model->field["PRINTDIV"] = ($model->field["PRINTDIV"] == "") ? "1" : $model->field["PRINTDIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PRINTDIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "PRINTDIV", $model->field["PRINTDIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //合格者ラジオ
        $opt = array(1, 2);
        $model->field["PASSDIV"] = ($model->field["PASSDIV"] == "") ? "1" : $model->field["PASSDIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PASSDIV{$val}\" onClick=\"OptionUse(this)\"");
        }
        $radioArray = knjCreateRadio($objForm, "PASSDIV", $model->field["PASSDIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受験番号
        $extra = ($model->field["PASSDIV"] == "1") ? " disabled " : "";
        $arg["data"]["RECEPTNO"] = knjCreateTextBox($objForm, $model->field["RECEPTNO"], "RECEPTNO", 5, 5, $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJL357F");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl357fForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $all="") {
    $opt = array();
    $i = $default = 0;
    if ($all) {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
        $i++;
    }
    $value_flg = false;
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
    $value = (($value && $value_flg) || $value == "ALL") ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
