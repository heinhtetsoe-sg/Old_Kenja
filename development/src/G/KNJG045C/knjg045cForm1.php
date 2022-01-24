<?php

require_once('for_php7.php');


class knjg045cForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjg045cindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["useSchool_KindField"] == "1") {
            //校種コンボ
            $query = knjg045cQuery::getA023($model);
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");
        }

        if (!isset($model->warning)) {
            if ($model->cmd !== 'hr_class') {
                $query = knjg045cQuery::getMainData($model);
                $diaryData = $db->getRow($query, DB_FETCHMODE_ASSOC);

            } else {
                $diaryData = $model->field;
            }
        } else {
            $diaryData = $model->field;
        }

        //日付データ
        if ($model->diaryDate == "") $model->diaryDate = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DIARY_DATE"] = View::popUpCalendar2($objForm  ,"DIARY_DATE" ,str_replace("-","/",$model->diaryDate),"reload=true", "btn_submit('main');");

        //天気
        $query = knjg045cQuery::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $diaryData["WEATHER"], "WEATHER", $extra, 1, "BLANK");

        //天気2
        $query = knjg045cQuery::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $diaryData["WEATHER2"], "WEATHER2", $extra, 1, "BLANK");


        //休暇欠勤
        $arg["data"]["SEQ001_REMARK1"] = getTextOrArea($objForm, "SEQ001_REMARK1", $model->SEQ001_REMARK1_moji, $model->SEQ001_REMARK1_gyou, $diaryData["SEQ001_REMARK1"], $model);
        $arg["data"]["SEQ001_REMARK1_COMMENT"] = "(全角".$model->SEQ001_REMARK1_moji."文字X".$model->SEQ001_REMARK1_gyou."行まで)";
        
        $arg["data"]["SEQ001_REMARK2"] = getTextOrArea($objForm, "SEQ001_REMARK2", $model->SEQ001_REMARK2_moji, $model->SEQ001_REMARK2_gyou, $diaryData["SEQ001_REMARK2"], $model);
        $arg["data"]["SEQ001_REMARK2_COMMENT"] = "(全角".$model->SEQ001_REMARK2_moji."文字X".$model->SEQ001_REMARK2_gyou."行まで)";

        //出張
        $arg["data"]["SEQ001_REMARK3"] = getTextOrArea($objForm, "SEQ001_REMARK3", $model->SEQ001_REMARK3_moji, $model->SEQ001_REMARK3_gyou, $diaryData["SEQ001_REMARK3"], $model);
        $arg["data"]["SEQ001_REMARK3_COMMENT"] = "(全角".$model->SEQ001_REMARK3_moji."文字X".$model->SEQ001_REMARK3_gyou."行まで)";
        
        $arg["data"]["SEQ001_REMARK4"] = getTextOrArea($objForm, "SEQ001_REMARK4", $model->SEQ001_REMARK4_moji, $model->SEQ001_REMARK4_gyou, $diaryData["SEQ001_REMARK4"], $model);
        $arg["data"]["SEQ001_REMARK4_COMMENT"] = "(全角".$model->SEQ001_REMARK4_moji."文字X".$model->SEQ001_REMARK4_gyou."行まで)";
        
        $arg["data"]["SEQ001_REMARK5"] = getTextOrArea($objForm, "SEQ001_REMARK5", $model->SEQ001_REMARK5_moji, $model->SEQ001_REMARK5_gyou, $diaryData["SEQ001_REMARK5"], $model);
        $arg["data"]["SEQ001_REMARK5_COMMENT"] = "(全角".$model->SEQ001_REMARK5_moji."文字X".$model->SEQ001_REMARK5_gyou."行まで)";

        //日直
        $extra = "";
    	$arg["data"]['SEQ002_REMARK1'] = knjCreateCombo($objForm, "SEQ002_REMARK1", $diaryData["SEQ002_REMARK1"], array('午前','午後'), $extra, 1);
        
        $extra = " onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEQ002_REMARK2"] = knjCreateTextBox($objForm, sprintf('%02d',$diaryData["SEQ002_REMARK2"]), "SEQ002_REMARK2", 2, 2, $extra);
        
        $extra = " onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEQ002_REMARK3"] = knjCreateTextBox($objForm, sprintf('%02d',$diaryData["SEQ002_REMARK3"]), "SEQ002_REMARK3", 2, 2, $extra);
        
        $extra = "";
    	$arg["data"]['SEQ002_REMARK4'] = knjCreateCombo($objForm, "SEQ002_REMARK4", $diaryData["SEQ002_REMARK4"], array('午前','午後'), $extra, 1);
        
        $extra = " onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEQ002_REMARK5"] = knjCreateTextBox($objForm, sprintf('%02d',$diaryData["SEQ002_REMARK5"]), "SEQ002_REMARK5", 2, 2, $extra);
        
        $extra = " onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEQ002_REMARK6"] = knjCreateTextBox($objForm, sprintf('%02d',$diaryData["SEQ002_REMARK6"]), "SEQ002_REMARK6", 2, 2, $extra);

        //巡視記録
        $arg["data"]["SEQ002_REMARK7"] = getTextOrArea($objForm, "SEQ002_REMARK7", $model->SEQ002_REMARK7_moji, $model->SEQ002_REMARK7_gyou, $diaryData["SEQ002_REMARK7"], $model);
        $arg["data"]["SEQ002_REMARK7_COMMENT"] = "(全角".$model->SEQ002_REMARK7_moji."文字X".$model->SEQ002_REMARK7_gyou."行まで)";
        
        //来校者
        $arg["data"]["SEQ002_REMARK8"] = getTextOrArea($objForm, "SEQ002_REMARK8", $model->SEQ002_REMARK8_moji, $model->SEQ002_REMARK8_gyou, $diaryData["SEQ002_REMARK8"], $model);
        $arg["data"]["SEQ002_REMARK8_COMMENT"] = "(全角".$model->SEQ002_REMARK8_moji."文字X".$model->SEQ002_REMARK8_gyou."行まで)";

        //学校行事その他の記録
        $arg["data"]["SEQ003_REMARK1"] = getTextOrArea($objForm, "SEQ003_REMARK1", $model->SEQ003_REMARK1_moji, $model->SEQ003_REMARK1_gyou, $diaryData["SEQ003_REMARK1"], $model);
        $arg["data"]["SEQ003_REMARK1_COMMENT"] = "(全角".$model->SEQ003_REMARK1_moji."文字X".$model->SEQ003_REMARK1_gyou."行まで)";

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $Row_Staff);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjg045cForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
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

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"return btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更　新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削　除", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終　了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $Row_Staff)
{
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJG045C");
}
?>
