<?php

require_once('for_php7.php');

/********************************************************************/
/* 授業料納付状況                                   山城 2005/12/13 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：中学と高校の8月12月の値を変更            山城 2005/03/06 */
/********************************************************************/

class knjp400jForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjp400jForm1", "POST", "knjp400jindex.php", "", "knjp400jForm1");

    $opt=array();

    $arg["data"]["YEAR"] = CTRL_YEAR;

    //中高判定フラグを作成する
    $db = Query::dbCheckOut();
    $row = $db->getOne(knjp400jQuery::GetJorH());
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    Query::dbCheckIn($db);
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

    //月コンボを作成する
    $opt_month = array();

    $opt_month[] = array("label" =>  "4月","value" => "4-11");
    $opt_month[] = array("label" =>  "5月","value" => "5-11");
    $opt_month[] = array("label" =>  "6月","value" => "6-11");
    $opt_month[] = array("label" =>  "7月","value" => "7-11");
    if ($jhflg == 1){
        $opt_month[] = array("label" =>  "8月","value" => "8-11");  //NO001
    }else {
        $opt_month[] = array("label" =>  "8月","value" => "8-12");  //NO001
    }
    $opt_month[] = array("label" =>  "9月","value" => "9-12");
    $opt_month[] = array("label" => "10月","value" => "10-12");
    $opt_month[] = array("label" => "11月","value" => "11-12");
    if ($jhflg == 1){
        $opt_month[] = array("label" => "12月","value" => "12-12"); //NO001
    }else {
        $opt_month[] = array("label" => "12月","value" => "12-13"); //NO001
    }
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
                        "value"     => "KNJP400J"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjp400jForm1.html", $arg); 
    }
}
?>
