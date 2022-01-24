<?php

require_once('for_php7.php');

class knjg045eForm1 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjg045eindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        $query = knjg045eQuery::getSchkind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHKIND"], "SCHKIND", $extra, 1);

        if (!isset($model->warning) || !$model->existsData) {
            $query = knjg045eQuery::getSchChrStfDiary($model);
            $diaryData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $diaryData = $model->field;
        }

        //日付データ
        $arg["DIARY_DATE"] = View::popUpCalendar2($objForm  ,"DIARY_DATE" ,str_replace("-","/",$model->diaryDate),"reload=true", "btn_submit('main');");

        //天気
        $query = knjg045eQuery::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $diaryData["WEATHER"], "WEATHER", $extra, 1, "BLANK");

        //天気2
        $query = knjg045eQuery::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $diaryData["WEATHER2"], "WEATHER2", $extra, 1, "BLANK");

        //気温
        $extra = "";
        $arg["TEMPERATURE"] = knjCreateTextBox($objForm, $diaryData["TEMPERATURE"], "TEMPERATURE", 5, 5, $extra);

        //講座を取得
        if (!isset($model->warning) || !$model->existsData) {
            $query = knjg045eQuery::selectQuery($model);
            $result = $db->query($query);
            $dataCnt = 0;
            $model->data = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //チェックエラー時用のデータを用意
                $model->data[$dataCnt] = $row;

                //授業コメント
                $row["REMARK1"] = getTextOrArea($objForm, "REMARK1", 40, 2, $row["REMARK1"], $dataCnt);
                $row["REMARK1_COMMENT"] = getTextAreaComment(100, 5);
                setInputChkHidden($objForm, "REMARK1", 40, 2, $dataCnt);

                $row["DATA_CNT"] = $dataCnt;

                $arg["data"][$dataCnt] = $row;

                $dataCnt++;
            }
        } else {
            foreach ($model->data as $key => $val) {
                $model->data[$key]["REMARK1"] = getTextOrArea($objForm, "REMARK1", 40, 2, $val["REMARK1"], $key);
                $model->data[$key]["REMARK1_COMMENT"] = getTextAreaComment(100, 5);
                setInputChkHidden($objForm, "REMARK1", 40, 2, $key);

                $model->data[$key]["DATA_CNT"] = $key;
            }
            $arg["data"] = $model->data;
        }

        //今日の出来事
        $arg["REMARK"] = getTextOrArea($objForm, "REMARK", 100, 5, $diaryData["REMARK"]);
        $arg["REMARK_COMMENT"] = getTextAreaComment(100, 5);
        setInputChkHidden($objForm, "REMARK", 100, 5);

        /********/
        /*ボタン*/
        /********/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更　新", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJG045E");
        knjCreateHidden($objForm, "DATA_MAX_CNT", get_count($arg["data"]));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knjg045eForm1.html", $arg);
    }
}

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
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $cnt="") {
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
        $extra = "style=\"height:".$height."px;\" id=\"".$name.$cnt."\"";
        $retArg = knjCreateTextArea($objForm, $name.$cnt, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name.$cnt."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name.$cnt, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, $cnt="") {
    KnjCreateHidden($objForm, $setHiddenStr.$cnt."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr.$cnt."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr.$cnt."_STAT", "statusarea_".$setHiddenStr.$cnt);
}

function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}

?>
