<?php

require_once('for_php7.php');

class knjl327mForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjl327mForm1", "POST", "knjl327mindex.php", "", "knjl327mForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->test_year;

        //対象日作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        /****************/
        /* ラジオボタン */
        /****************/
        //帳票種類
        $opt = array(1, 2); //1:合格証明書 2:補員通知書
        $model->field["PRINT_TYPE"] = ($model->field["PRINT_TYPE"] == "") ? "1" : $model->field["PRINT_TYPE"];
        $extra = array("id=\"PRINT_TYPE1\"", "id=\"PRINT_TYPE2\"");
        $radioArray = knjCreateRadio($objForm, "PRINT_TYPE", $model->field["PRINT_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力範囲
        $opt = array(1, 2); //(1:受験者指定 2:合格者全員)
        $model->field["PRINT_RANGE"] = ($model->field["PRINT_RANGE"] == "") ? "1" : $model->field["PRINT_RANGE"];
        $extra = array("id=\"PRINT_RANGE1\"", "id=\"PRINT_RANGE2\"");
        $radioArray = knjCreateRadio($objForm, "PRINT_RANGE", $model->field["PRINT_RANGE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* テキストボックス */
        /********************/
        //受験番号開始
        $extra = "onBlur=\"this.value = toInteger(this.value); zeroUme(5, this);\"";
        $arg["data"]["SUB_EXAMNO_FROM"] = knjCreateTextBox($objForm, $model->field["SUB_EXAMNO_FROM"], "SUB_EXAMNO_FROM", 5, 5, $extra);
        //受験番号終了
        $extra = "onBlur=\"this.value = toInteger(this.value); zeroUme(5, this);\"";
        $arg["data"]["SUB_EXAMNO_TO"] = knjCreateTextBox($objForm, $model->field["SUB_EXAMNO_TO"], "SUB_EXAMNO_TO", 5, 5, $extra);
        //有効期間
        $model->field["YUKO_DATE"] = $model->field["YUKO_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["YUKO_DATE"];
        $arg["data"]["YUKO_DATE"] = View::popUpCalendar($objForm, "YUKO_DATE", $model->field["YUKO_DATE"]);
        //有効期間
        $model->field["RENRAKU_DATE"] = $model->field["RENRAKU_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["RENRAKU_DATE"];
        $arg["data"]["RENRAKU_DATE"] = View::popUpCalendar($objForm, "RENRAKU_DATE", $model->field["RENRAKU_DATE"]);
        //時間
        $extra = " onBlur=\"return this.value = toInteger(this.value);\"";
        $arg["data"]["YUKO_TIME"] = knjCreateTextBox($objForm, $model->field["YUKO_TIME"], "YUKO_TIME", 2, 2, $extra);

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
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "EXAMNO_FROM");
        knjCreateHidden($objForm, "EXAMNO_TO");
        knjCreateHidden($objForm, "PRGID", "KNJL327M");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl327mForm1.html", $arg);
    }
}
?>
