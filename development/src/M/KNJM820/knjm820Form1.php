<?php

require_once('for_php7.php');


class knjm820Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm820Form1", "POST", "knjm820index.php", "", "knjm820Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度
        $arg["data"]["CTRL_YEAR"] = $model->control["年度"];

        //年度コンボ作成
        $opt = array(CTRL_YEAR + 1, CTRL_YEAR);
        $value = CTRL_YEAR + 1;
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $value, $opt, "", 1);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm820Form1.html", $arg); 
    }
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSVボタンを作成する
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJM820");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}

?>
