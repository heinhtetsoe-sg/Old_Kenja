<?php

require_once('for_php7.php');

class knjf072Form1 {
    function main(&$model) {
    //オブジェクト作成
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjf072Form1", "POST", "knjf072index.php", "", "knjf072Form1");

    //年度テキストボックスを作成する
    $arg["data"]["YEAR"] = CTRL_YEAR;

    //DB接続
    $db = Query::dbCheckOut();

    $model->field["SEX"] = $model->field["SEX"] ? $model->field["SEX"] : "1";
    $opt = array(1, 2);
    $extra  = array("id=\"SEX1\"", "id=\"SEX2\"");
    $radioArray = knjCreateRadio($objForm, "SEX", $model->field["SEX"], $extra, $opt, get_count($opt));
    foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

    $query = knjf072Query::getNameMst("Z002", null);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $arg["data"]["SEX".$row["NAMECD2"]."_NAME"] = $row["NAME2"];
    }
    $result->free();

    Query::dbCheckIn($db);


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
    View::toHTML($model, "knjf072Form1.html", $arg);
    }
}
?>
