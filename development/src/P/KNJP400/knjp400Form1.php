<?php

require_once('for_php7.php');


class knjp400Form1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjp400Form1", "POST", "knjp400index.php", "", "knjp400Form1");

    $opt=array();

    $arg["data"]["YEAR"] = CTRL_YEAR;

    //中高判定フラグを作成する
    $db = Query::dbCheckOut();
    $row = $db->getOne(knjp400Query::GetJorH());
    if ($row == 1) {
        $jhflg = 1;
    } else {
        $jhflg = 2;
    }
    Query::dbCheckIn($db);
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

    //月コンボを作成する
    $opt_month = array();

    $opt_month[] = array("label" => "12月","value" => "12-13");
    $opt_month[] = array("label" =>  "1月","value" => "1-13");
    $opt_month[] = array("label" =>  "2月","value" => "2-13");
    $opt_month[] = array("label" =>  "3月","value" => "3-13");

    if (!$model->month) $model->month = $opt_month[0]["value"];

    $objForm->ae( array("type"      => "select",
                        "name"      => "MONTH",
                        "size"      => 1,
                        "value"     => $model->month,
                        "extrahtml" => "",
                        "options"   => $opt_month ) );

    $arg["data"]["MONTH"] = $objForm->ge("MONTH");

    //印刷ボタンを作成する
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_print",
                        "value"     => "プレビュー／印刷",
                        "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

    $arg["button"]["btn_print"] = $objForm->ge("btn_print");

    //終了ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "YEAR",
                        "value"     => CTRL_YEAR
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "SEMESTER",
                        "value"     => CTRL_SEMESTER
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "DBNAME",
                        "value"     => DB_DATABASE
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "PRGID",
                        "value"     => "KNJP400"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjp400Form1.html", $arg); 
    }
}
?>
