<?php

require_once('for_php7.php');


class knjm560Form1
{
    function main(&$model) {

    //オブジェクト作成
    $objForm = new form;

    //フォーム作成
    $arg["start"] = $objForm->get_start("knjm560Form1", "POST", "knjm560index.php", "", "knjm560Form1");

    //DB接続
    $db = Query::dbCheckOut();

    //年度
    $arg["top"]["YEAR"] = CTRL_YEAR;
    //学期
    $arg["top"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

    //講座コンボを作成する
    $query = knjm560Query::GetChr($model);
    makeCombo($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", "", 1);

    //並び順ラジオ 1:クラス順 2:あいうえお順 3:学籍番号順
    $opt_sort = array(1, 2, 3);
    $model->field["SORT"] = $model->field["SORT"] ? $model->field["SORT"] : "1";
    $extra = array("id=\"SORT1\"", "id=\"SORT2\"", "id=\"SORT3\"");
    $sortArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt_sort, get_count($opt_sort));
    foreach ($sortArray as $key => $val) $arg["data"][$key] = $val;

    //ボタンを作成する
    makeBtn($objForm, $arg);

    //hiddenを作成する
    makeHidden($objForm, $arg);

    //DB切断
    Query::dbCheckIn($db);

    //フォーム終わり
    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjm560Form1.html", $arg); 

    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, &$arg) {
    $arg["TOP"]["YEAR"] = knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    $arg["TOP"]["GAKKI"] = knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    $arg["TOP"]["DBNAME"] = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    $arg["TOP"]["PRGID"] = knjCreateHidden($objForm, "PRGID", "KNJM560");
    knjCreateHidden($objForm, "cmd");
}
?>
