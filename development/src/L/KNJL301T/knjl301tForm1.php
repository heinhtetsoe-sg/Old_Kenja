<?php

require_once('for_php7.php');

class knjl301tForm1 {
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjl301tForm1", "POST", "knjl301tindex.php", "", "knjl301tForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->test_year;

        //入試制度
        $query = knjl301tQuery::getApplicantdiv($model);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], "", 1);

        //checkbox
        if ($model->field["SUPPLEMENT"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= "id='SUPPLEMENT'";
        $arg["data"]["SUPPLEMENT"] = knjCreateCheckBox($objForm, "SUPPLEMENT", "1", $extra);


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
        knjCreateHidden($objForm, "PRGID", "KNJL301T");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl301tForm1.html", $arg);
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

    if ($name == 'APPLICANTDIV') {
        $opt[] = array('label' => '--全て--', 'value' => '0');
    }

    if (!strlen($opt[0]["value"])) {
        $opt[] = array('label' => '　　　　', 'value' => '');
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
