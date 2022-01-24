<?php

require_once('for_php7.php');

class knjl302eForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl302eForm1", "POST", "knjl302eindex.php", "", "knjl302eForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear."年度";

        //入試制度
        $query = knjl302eQuery::getNameMst($model->examyear, "L003");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1);

        //入試区分
        $query = knjl302eQuery::getNameMst($model->examyear, "L004", "1");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1);

        //>面接点出力ありチェックボックス
        $extra  = "id=\"TESTSCORE_PRINT\"";
        $extra .= ($model->testscoreprint == "1") ? " checked": "";
        $arg["data"]["TESTSCORE_PRINT"] = knjCreateCheckBox($objForm, "TESTSCORE_PRINT", "1", $extra);

        //会場ごとに改ページするチェックボックス
        $extra  = "id=\"PERHALLCD_PRINT\"";
        $extra .= ($model->perhallcdprint == "1") ? " checked": "";
        $arg["data"]["PERHALLCD_PRINT"] = knjCreateCheckBox($objForm, "PERHALLCD_PRINT", "1", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //CSV出力
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "ＣＳＶ出力", $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJL302E");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl302eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
