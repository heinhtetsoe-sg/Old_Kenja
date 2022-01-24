<?php

require_once('for_php7.php');

class knjl373mForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjl373mForm1", "POST", "knjl373mindex.php", "", "knjl373mForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->test_year;

        /**********/
        /* コンボ */
        /**********/
        //入試制度
        $db = Query::dbCheckOut();
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl373mQuery::getApplicantdiv());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('knjl312m');\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        //合格点
        $extra = " onBlur=\"return this.value = toInteger(this.value);\"";
        $arg["data"]["PASS_SCORE"] = knjCreateTextBox($objForm, $model->field["PASS_SCORE"], "PASS_SCORE", 3, 3, $extra);
        //候補点
        $extra = " onBlur=\"return this.value = toInteger(this.value);\"";
        $arg["data"]["KOUHO_SCORE"] = knjCreateTextBox($objForm, $model->field["KOUHO_SCORE"], "KOUHO_SCORE", 3, 3, $extra);


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
        knjCreateHidden($objForm, "PRGID", "KNJL373M");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl373mForm1.html", $arg);
    }
}
?>
