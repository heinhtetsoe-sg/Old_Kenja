<?php

require_once('for_php7.php');

class knjl015mForm1 {
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjl015mForm1", "POST", "knjl015mindex.php", "", "knjl015mForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->test_year;

        /********************/
        /* チェックボックス */
        /********************/
        //合格
        $extra = " id=\"JUDGEMENT1\" checked onclick=\"check_check(this);\"";
        $arg["data"]["JUDGEMENT1"] = knjCreateCheckBox($objForm, "JUDGEMENT1", '1', $extra);
        $arg["data"]["JUDGEMENT_NAME1"] = knjl015mQuery::getJudgementName('1', $db);
        //補員合格
        $extra = " id=\"JUDGEMENT2\" checked onclick=\"check_check(this);\"";
        $arg["data"]["JUDGEMENT2"] = knjCreateCheckBox($objForm, "JUDGEMENT2", '1', $extra);
        $arg["data"]["JUDGEMENT_NAME2"] = knjl015mQuery::getJudgementName('4', $db);
        //補員合格
        $extra = " id=\"JUDGEMENT3\" onclick=\"check_check(this);\"";
        $arg["data"]["JUDGEMENT3"] = knjCreateCheckBox($objForm, "JUDGEMENT3", '1', $extra);
        $arg["data"]["JUDGEMENT_NAME3"] = '志願者全員';

        /**********/
        /* ボタン */
        /**********/
        //ＣＳＶボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl015mForm1.html", $arg);
    }
}
//チェックボックスを作成する
function makeCheckBox(&$objForm, &$arg, $model, $name) {
    $extra = " id=\"$name\" checked";
    $arg["data"][$name] = createCheckBox($objForm, $name, '1', $check.$extra);
}
?>
