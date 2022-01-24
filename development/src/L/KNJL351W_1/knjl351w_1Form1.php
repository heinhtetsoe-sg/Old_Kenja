<?php

require_once('for_php7.php');

class knjl351w_1Form1
{
    function main(&$model)
    {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl351w_1index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut2();
        $requestroot = REQUESTROOT;

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"] = $model->entexamYear;

        //radio
        $opt = array(1, 2, 3);
        $model->field["OUTPUT_DIV"] = ($model->field["OUTPUT_DIV"] == "") ? "2" : $model->field["OUTPUT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT_DIV{$val}\" onclick=\"Page_jumper('{$requestroot}');\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //radio
        $opt = array(1);
        $model->field["CSV_DIV"] = ($model->field["CSV_DIV"] == "") ? "1" : $model->field["CSV_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"CSV_DIV{$val}\" onclick=\"Page_jumper('{$requestroot}');\"");
        }
        $radioArray = knjCreateRadio($objForm, "CSV_DIV", $model->field["CSV_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl351w_1Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //実行ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
