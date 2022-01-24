<?php

require_once('for_php7.php');

class knjf073Form1 {
    function main(&$model) {
    //オブジェクト作成
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjf073Form1", "POST", "knjf073index.php", "", "knjf073Form1");

    //年度テキストボックスを作成する
    $arg["data"]["YEAR"] = CTRL_YEAR;

    //csvボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_print",
                        "value"       => "ＣＳＶ出力",
                        "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );
    $arg["button"]["btn_print"] = $objForm->ge("btn_print");

    //終了ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );
    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hidden作成
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);


    //フォーム終わり
    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjf073Form1.html", $arg);
    }
}
?>
