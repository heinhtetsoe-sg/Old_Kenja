<?php

require_once('for_php7.php');

class knjl309tForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjl309tForm1", "POST", "knjl309tindex.php", "", "knjl309tForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->test_year;

        //作成日付
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //通知日付
        $model->field["T_DATE"] = $model->field["T_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["T_DATE"];
        $arg["data"]["T_DATE"] = View::popUpCalendar($objForm, "T_DATE", $model->field["T_DATE"]);

        //午前・午後選択ラジオボタン 1:午前 2:午後
        $opt_timeDiv = array(1, 2);
        $model->field["TIME_DIV"] = ($model->field["TIME_DIV"] == "") ? "2" : $model->field["TIME_DIV"];
        $extra = array("id=\"TIME_DIV1\" onclick =\" return btn_submit('');\"", "id=\"TIME_DIV2\" onclick =\" return btn_submit('');\"");
        $radioArray = knjCreateRadio($objForm, "TIME_DIV", $model->field["TIME_DIV"], $extra, $opt_timeDiv, get_count($opt_timeDiv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //通知時間コンボボックス
        for($i = 0; $i <= 12; $i++){
            $opt_hour[] = array('label' => $i.'時', 'value' => $i);
        }
        $model->field["HOUR"] = ($model->field["TIME_DIV"] == "1") ? "10" : "1";
        $arg["data"]["HOUR"] = knjCreateCombo($objForm, "HOUR", $model->field["HOUR"], $opt_hour, "", 1);

        //送付日付
        $model->field["S_DATE"] = $model->field["S_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["S_DATE"];
        $arg["data"]["S_DATE"] = View::popUpCalendar($objForm, "S_DATE", $model->field["S_DATE"]);

        /**************/
        /* ボタン作成 */
        /**************/
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
        knjCreateHidden($objForm, "PRGID", "KNJL309T");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl309tForm1.html", $arg);
    }
}
?>
