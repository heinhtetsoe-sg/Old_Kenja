<?php

require_once('for_php7.php');


class knjg022Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjg022Form1", "POST", "knjg022index.php", "", "knjg022Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["CTRL_YEAR"] = CTRL_YEAR.'年度';

        //学期
        $arg["data"]["CTRL_SEMESTER"] = CTRL_SEMESTERNAME;

        //年度コンボ作成
        $query = knjg022Query::getSchoolYear();
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], "", 1);

        //出力順ラジオボタン 1:発行番号順 2:発行年月日順 3:フォームのみ出力
        $opt_sort = array(1, 2, 3);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "3" : $model->field["SORT"];
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"", "id=\"SORT3\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷開始ページテキストボックス
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $model->field["PAGE"] = ($model->field["PAGE"]) ? $model->field["PAGE"] : "1";
        $arg["data"]["PAGE"] = knjCreateTextBox($objForm, $model->field["PAGE"], "PAGE", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg022Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if($name == "YEAR"){
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJG022");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]);
}
?>
