<?php

require_once('for_php7.php');


class knjmp961Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjmp961Form1", "POST", "knjmp961index.php", "", "knjmp961Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["CTRL_YEAR"] = CTRL_YEAR;

        //学年コンボボックス
        $query = knjmp961Query::getGrade();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");

        //出力指定
        $opt = array(1, 2);
        $model->field["PRINT_DIV"] = ($model->field["PRINT_DIV"] == "") ? "1" : $model->field["PRINT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PRINT_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //伝票番号
        $extra = "";
        $arg["data"]["REQUEST_NO"] = knjCreateTextBox($objForm, $model->field["REQUEST_NO"], "REQUEST_NO", 10, 10, $extra);

        //期間指定
        $opt = array(1, 2);
        $model->field["INCOME_DIV"] = ($model->field["INCOME_DIV"] == "") ? "1" : $model->field["INCOME_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"INCOME_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "INCOME_DIV", $model->field["INCOME_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;


        $model->month = array("4" => "４月", "5" => "５月", "6" => "６月", "7" => "７月", "8" => "８月", "9" => "９月",
                              "10" => "１０月", "11" => "１１月", "12" => "１２月", "1" => "１月", "2" => "２月", "3" => "３月");
        list($year, $month, $day) = preg_split("/-/", CTRL_DATE);

        //収入伺日F
        $query = knjmp961Query::getMonth($model);
        $extra = "";
        $model->field["REQUEST_MONTH_F"] = $model->field["REQUEST_MONTH_F"] ? $model->field["REQUEST_MONTH_F"] : $month;
        makeCmb($objForm, $arg, $db, $query, "REQUEST_MONTH_F", $model->field["REQUEST_MONTH_F"], $extra, 1, "");

        //収入伺日T
        $extra = "";
        $model->field["REQUEST_MONTH_T"] = $model->field["REQUEST_MONTH_T"] ? $model->field["REQUEST_MONTH_T"] : $month;
        makeCmb($objForm, $arg, $db, $query, "REQUEST_MONTH_T", $model->field["REQUEST_MONTH_T"], $extra, 1, "");

        //収入決定日F
        $extra = "";
        $model->field["INCOME_MONTH_F"] = $model->field["INCOME_MONTH_F"] ? $model->field["INCOME_MONTH_F"] : $month;
        makeCmb($objForm, $arg, $db, $query, "INCOME_MONTH_F", $model->field["INCOME_MONTH_F"], $extra, 1, "");

        //収入決定日T
        $extra = "";
        $model->field["INCOME_MONTH_T"] = $model->field["INCOME_MONTH_T"] ? $model->field["INCOME_MONTH_T"] : $month;
        makeCmb($objForm, $arg, $db, $query, "INCOME_MONTH_T", $model->field["INCOME_MONTH_T"], $extra, 1, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp961Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
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
    knjCreateHidden($objForm, "PRGID", "KNJMP961");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
}

?>
