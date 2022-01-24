<?php

require_once('for_php7.php');


class knjp965Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp965Form1", "POST", "knjp965index.php", "", "knjp965Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["CTRL_YEAR"] = CTRL_YEAR;

        //校種コンボ
        $opt = array();
        $query = knjp965Query::getSchoolKind($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "");

        $model->yearMonth = array();
        for ($j = 0; $j < 2; $j++) {
            $baseYear = CTRL_YEAR + $j;
            for ($i = 4; $i <= 15; $i++) {
                $yearVal  = ($i > 12) ? $baseYear + 1 : $baseYear;
                $monthVal = sprintf("%02d", ($i > 12) ? $i - 12 : $i);
                $model->yearMonth[$yearVal."-".$monthVal] = $yearVal."/".$monthVal;
            }
        }

        list($year, $month, $day) = preg_split("/-/", CTRL_DATE);
        //日付Fromコンボボックス
        $query = knjp965Query::getMonth($model);
        $extra = "";
        $model->field["MONTH_F"] = $model->field["MONTH_F"] ? $model->field["MONTH_F"] : $month;
        makeCmb($objForm, $arg, $db, $query, "MONTH_F", $model->field["MONTH_F"], $extra, 1, "");

        //日付Toコンボボックス
        $query = knjp965Query::getMonth($model);
        $extra = "";
        $model->field["MONTH_T"] = $model->field["MONTH_T"] ? $model->field["MONTH_T"] : $month;
        makeCmb($objForm, $arg, $db, $query, "MONTH_T", $model->field["MONTH_T"], $extra, 1, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp965Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

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
    knjCreateHidden($objForm, "PRGID", "KNJP965");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
}

?>
