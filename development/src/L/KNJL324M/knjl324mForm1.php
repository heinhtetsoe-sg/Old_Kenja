<?php

require_once('for_php7.php');

class knjl324mForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjl324mForm1", "POST", "knjl324mindex.php", "", "knjl324mForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->test_year;

        //作成日付
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //有効期間
        $model->field["YUKO_DATE"] = $model->field["YUKO_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["YUKO_DATE"];
        $arg["data"]["YUKO_DATE"] = View::popUpCalendar($objForm, "YUKO_DATE", $model->field["YUKO_DATE"]);

        //入学手続き期間
        $model->field["LIMIT_DATE"] = $model->field["LIMIT_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["LIMIT_DATE"];
        $arg["data"]["LIMIT_DATE"] = View::popUpCalendar($objForm, "LIMIT_DATE", $model->field["LIMIT_DATE"]);

        //時間
        $extra = " onBlur=\"return this.value = toInteger(this.value);\"";
        $arg["data"]["LIMIT_TIME"] = knjCreateTextBox($objForm, $model->field["LIMIT_TIME"], "LIMIT_TIME", 2, 2, $extra);

        /**************/
        /* ボタン作成 */
        /**************/
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //CSV出力(成績優良者)
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
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
        knjCreateHidden($objForm, "PRGID", "KNJL324M");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl324mForm1.html", $arg);
    }
}
?>
