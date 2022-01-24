<?php

require_once('for_php7.php');

class knjg045dForm1 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjg045dindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();
        if (!isset($model->warning)) {
            $query = knjg045dQuery::getDiary($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $row = $model->field;
        }

        //日付データ
        $arg["sel"]["DIARY_DATE"] = View::popUpCalendar2($objForm  ,"DIARY_DATE" ,str_replace("-","/",$model->diaryDate),"reload=true", "btn_submit('main');");

        //天気
        $query = knjg045dQuery::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $row["WEATHER"], "WEATHER", $extra, 1, "BLANK");

        //天気2
        $query = knjg045dQuery::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $row["WEATHER2"], "WEATHER2", $extra, 1, "BLANK");

        //記事
        $extra = " id=\"NEWS\"";
        $arg["data"]["NEWS"] = KnjCreateTextArea($objForm, "NEWS", 11, 100, "soft", $extra, $row["NEWS"]);
        //行事予定ボタン
        $extra = "onclick=\"return btn_submit2('yotei')\"";
        $arg["button"]["btn_yotei"] = knjCreateBtn($objForm, "btn_yotei", "行事予定", $extra);
        //学校行事取得
        $query      = knjg045dQuery::getEventMst($model);
        $result = $db->query($query);
        $eventYotei = "";
        $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eventYotei .= $sep.$row["REMARK1"];
            $sep = "\n";
        }
        knjCreateHidden($objForm, "EVENT_NAME", $eventYotei);
        //クラス区分セット
        $model->hr_class_div = ($model->Properties["useFi_Hrclass"] == '1') ? "2" : "1";
        knjCreateHidden($objForm, "HR_CLASS_DIV", $model->hr_class_div);

        //来校者
        $extra = "";
        $arg["data"]["RAIKOU"] = KnjCreateTextArea($objForm, "RAIKOU", 4, 50, "soft", $extra, $row["RAIKOU"]);

        //用件
        $extra = "";
        $arg["data"]["MATTER"] = KnjCreateTextArea($objForm, "MATTER", 4, 50, "soft", $extra, $row["MATTER"]);

        //その他
        $extra = "";
        $arg["data"]["REMARK"] = KnjCreateTextArea($objForm, "REMARK", 4, 100, "soft", $extra, $row["REMARK"]);
        
        
        //出張者ほかを取得し、セット
        if (!isset($model->warning)) {
            if ($model->cmd !== 'yotei') {
                $opt_show1 = $opt_value1 = array();
                $opt_show2 = $opt_value2 = array();
                $query  = knjg045dQuery::getStaffData($model);
                $result = $db->query($query);

                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["STAFF_DIV"] === '1') {
                        $opt_show1[]  = $row["STAFFNAME_SHOW"];
                        $opt_value1[] = $row["REMARK1"];
                    } else if ($row["STAFF_DIV"] === '2') {
                        $opt_show2[]  = $row["STAFFNAME_SHOW"];
                        $opt_value2[] = $row["REMARK1"];
                    }
                }
                $result->free();
                $Row_Staff["STAFFNAME_SHOW1"]    = implode(",",$opt_show1);
                $Row_Staff["STAFFCD1"]           = implode(",",$opt_value1);
                $Row_Staff["STAFFNAME_SHOW2"]    = implode(",",$opt_show2);
                $Row_Staff["STAFFCD2"]           = implode(",",$opt_value2);
            } else {
                $Row_Staff = $model->field;
            }
        } else {
            $Row_Staff = $model->field;
        }

        //出張者ボタン
        $extra = "onclick=\"return btn_submit('shutcho');\" STYLE=\"height:30px;WIDTH:100px;\"";
        $arg["button"]["btn_shutcho"] = knjCreateBtn($objForm, "btn_shutcho", "出張者", $extra);

        //出張者
        $extra = "STYLE=\"height:65px;WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly";
        $arg["data"]["STAFFNAME_SHOW1"] = knjCreateTextArea($objForm, "STAFFNAME_SHOW1", "4", "44", "wrap", $extra, $Row_Staff["STAFFNAME_SHOW1"]);

        //休暇等ボタン
        $extra = "onclick=\"return btn_submit('kyuka');\" STYLE=\"height:30px;WIDTH:100px;\"";
        $arg["button"]["btn_kyuka"] = knjCreateBtn($objForm, "btn_kyuka", "休暇等", $extra);

        //休暇等
        $extra = "STYLE=\"height:65px;WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly";
        $arg["data"]["STAFFNAME_SHOW2"] = knjCreateTextArea($objForm, "STAFFNAME_SHOW2", "4", "44", "wrap", $extra, $Row_Staff["STAFFNAME_SHOW2"]);

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
        knjCreateHidden($objForm, "PRGID", "KNJG045D");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjg045dForm1.html", $arg); 
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
?>
