<?php

require_once('for_php7.php');


class knjl212cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl212cForm1", "POST", "knjl212cindex.php", "", "knjl212cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl212cQuery::getApctDiv($model));

        //バス発車時刻表示
        if ($model->Properties["Pretest_bus_Not_Hyouji"] != "1") {
            $arg["Pretest_bus_Hyouji"] = 1;
        }

        //プレテスト区分コンボ
        $query = knjl212cQuery::getNameMst("L104");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PRE_TESTDIV", $model->field["PRE_TESTDIV"], $extra, 1, "");

        /******************/
        /* 登校時発車時刻 */
        /******************/
        //近鉄福神駅 初期値(9:10)
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        if (!strlen($model->field["FUKUJIN_HOUR"]))   $model->field["FUKUJIN_HOUR"]   = "9";
        if (!strlen($model->field["FUKUJIN_MINUTE"])) $model->field["FUKUJIN_MINUTE"] = "10";
        $arg["data"]["FUKUJIN_HOUR"] = knjCreateTextBox($objForm, $model->field["FUKUJIN_HOUR"], "FUKUJIN_HOUR", 2, 2, $extra);
        $arg["data"]["FUKUJIN_MINUTE"] = knjCreateTextBox($objForm, $model->field["FUKUJIN_MINUTE"], "FUKUJIN_MINUTE", 2, 2, $extra);
        //南海林間田園都市駅 初期値(8:50)
        if (!strlen($model->field["RINKAN_HOUR"]))   $model->field["RINKAN_HOUR"]   = "8";
        if (!strlen($model->field["RINKAN_MINUTE"])) $model->field["RINKAN_MINUTE"] = "50";
        $arg["data"]["RINKAN_HOUR"] = knjCreateTextBox($objForm, $model->field["RINKAN_HOUR"], "RINKAN_HOUR", 2, 2, $extra);
        $arg["data"]["RINKAN_MINUTE"] = knjCreateTextBox($objForm, $model->field["RINKAN_MINUTE"], "RINKAN_MINUTE", 2, 2, $extra);
        //JR五条駅 初期値(9:15)
        if (!strlen($model->field["GOJOU_HOUR"]))   $model->field["GOJOU_HOUR"]   = "9";
        if (!strlen($model->field["GOJOU_MINUTE"])) $model->field["GOJOU_MINUTE"] = "15";
        $arg["data"]["GOJOU_HOUR"] = knjCreateTextBox($objForm, $model->field["GOJOU_HOUR"], "GOJOU_HOUR", 2, 2, $extra);
        $arg["data"]["GOJOU_MINUTE"] = knjCreateTextBox($objForm, $model->field["GOJOU_MINUTE"], "GOJOU_MINUTE", 2, 2, $extra);
        /******************/
        /* 下校時発車時刻 */
        /******************/
        //学園前ロータリー 初期値(13:00)
        if (!strlen($model->field["GAKUEN_HOUR"]))   $model->field["GAKUEN_HOUR"]   = "13";
        if (!strlen($model->field["GAKUEN_MINUTE"])) $model->field["GAKUEN_MINUTE"] = "00";
        $arg["data"]["GAKUEN_HOUR"] = knjCreateTextBox($objForm, $model->field["GAKUEN_HOUR"], "GAKUEN_HOUR", 2, 2, $extra);
        $arg["data"]["GAKUEN_MINUTE"] = knjCreateTextBox($objForm, $model->field["GAKUEN_MINUTE"], "GAKUEN_MINUTE", 2, 2, $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL212C");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "Pretest_bus_Not_Hyouji", $model->Properties["Pretest_bus_Not_Hyouji"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl212cForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $value_flg = false;
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
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
?>
