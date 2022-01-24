<?php

require_once('for_php7.php');

class knjl301mForm1 {
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjl301mForm1", "POST", "knjl301mindex.php", "", "knjl301mForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->test_year;

        //入試制度
        $query = knjl301mQuery::getApplicantdiv($model);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], "", 1);

        //対象者
        $opt = array(1, 2); //(1:志願者全員 2:志願者指定)
        $model->field["TAISHOU"] = ($model->field["TAISHOU"] == "") ? "1" : $model->field["TAISHOU"];
        $disabled = " onclick=\"OptionUse('this');\"";
        $extra = array("id=\"TAISHOU1\"".$disabled, "id=\"TAISHOU2\"".$disabled);
        $radioArray = knjCreateRadio($objForm, "TAISHOU", $model->field["TAISHOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //extra
        $extra  = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= (($row["TAISHOU"] == "") || ($row["TAISHOU"] == "1")) ? " disabled" : "";

        //受付番号開始テキストボックスを作成する
        $model->field["S_EXAMNO"] = (isset($model->field["S_EXAMNO"])) ? $model->field["S_EXAMNO"] : "";
        $arg["data"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->field["S_EXAMNO"], "S_EXAMNO", 5, 5, $extra);

        //受付番号開始テキストボックスを作成する
        $model->field["E_EXAMNO"] = (isset($model->field["E_EXAMNO"])) ? $model->field["E_EXAMNO"] : "";
        $arg["data"]["E_EXAMNO"] = knjCreateTextBox($objForm, $model->field["E_EXAMNO"], "E_EXAMNO", 5, 5, $extra);

        //出力順
        $opt = array(1, 2); //(1:受験番号 2:出身学校)
        $model->field["SYUTURYOKUJUN"] = ($model->field["SYUTURYOKUJUN"] == "") ? "1" : $model->field["SYUTURYOKUJUN"];
        $extra = array("id=\"SYUTURYOKUJUN1\"", "id=\"SYUTURYOKUJUN2\"");
        $radioArray = knjCreateRadio($objForm, "SYUTURYOKUJUN", $model->field["SYUTURYOKUJUN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /**********/
        /* ボタン */
        /**********/
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //ＣＳＶボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "YEAR", $model->test_year);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJL301M");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl301mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if (!strlen($opt[0]["value"])) {
        $opt[] = array('label' => '　　　　', 'value' => '');
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
