<?php

require_once('for_php7.php');
class knjg048Form1 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjg048index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
        }
        $query = knjg048Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb2($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);

        if (!isset($model->warning)) {
            $query = knjg048Query::getDiary($model);
            $diaryData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $diaryData = $model->field;
        }

        //日付データ
        if ($model->diaryDate == "") $model->diaryDate = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DIARY_DATE"] = View::popUpCalendar2($objForm  ,"DIARY_DATE" ,str_replace("-","/",$model->diaryDate),"reload=true", "btn_submit('main');");

        //天気
        $query = knjg048Query::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $diaryData["WEATHER"], "WEATHER", $extra, 1, "BLANK");

        //天気2
        $query = knjg048Query::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $diaryData["WEATHER2"], "WEATHER2", $extra, 1, "BLANK");

        //記事
        $extra = " id=\"NEWS\"";
        $arg["data"]["NEWS"] = KnjCreateTextArea($objForm, "NEWS", 8, 93, "soft", $extra, $diaryData["NEWS"]);
        //行事予定ボタン
        $extra = "onclick=\"return btn_submit2('yotei')\"";
        $arg["button"]["btn_yotei"] = knjCreateBtn($objForm, "btn_yotei", "行事予定", $extra);
        //学校行事取得
        $query      = knjg048Query::getEventMst($model);
        $eventYotei = $db->getOne($query);
        knjCreateHidden($objForm, "EVENT_NAME", $eventYotei);
        //クラス区分セット
        $model->hr_class_div = ($model->Properties["useFi_Hrclass"] == '1') ? "2" : "1";
        knjCreateHidden($objForm, "HR_CLASS_DIV", $model->hr_class_div);

        //職員事項
        $extra = "";
        $arg["data"]["STAFFNEWS"] = KnjCreateTextArea($objForm, "STAFFNEWS", 12, 93, "soft", $extra, $diaryData["STAFFNEWS"]);

        /********/
        /*ボタン*/
        /********/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更　新", $extra);
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削　除", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終　了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJG048");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjg048Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["sel"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
