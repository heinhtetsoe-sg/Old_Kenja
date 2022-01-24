<?php

require_once('for_php7.php');


class knjg045Form1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("main", "POST", "knjg045index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["useSchool_KindField"] == "1") {
            //校種コンボ
            $query = knjg045Query::getA023($model);
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");
        }

        if (!isset($model->warning)) {
            if ($model->cmd !== 'hr_class') {
                $query = knjg045Query::getDiary($model);
                $diaryData = $db->getRow($query, DB_FETCHMODE_ASSOC);

                if ($model->Properties["knjg045updateWeatherJH"] == "1") {
                    $query = knjg045Query::getShowWeather($model);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if (is_array($row)) {
                        $diaryData["WEATHER"] = $row["WEATHER"];
                        $diaryData["WEATHER2"] = $row["WEATHER2"];
                    }
                }

                if ($model->Properties["useNurseoffAttend"] == "1") {
                    //欠席データ取得
                    $date = ($model->diaryDate) ? str_replace("/", "-", $model->diaryDate) : CTRL_DATE;
                    $attend = array();
                    $query = knjg045Query::getNurseoffAttendDat($model, $date);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $attend[$row["DI_CD"]][$row["GRADE"]] = $row["CNT"];
                    }
                    $result->free();
                }
            } else {
                $diaryData = $model->field;
                if ($model->Properties["useNurseoffAttend"] == "1") {
                    $attend =& $model->att_field;
                }
            }
        } else {
            $diaryData = $model->field;
            if ($model->Properties["useNurseoffAttend"] == "1") {
                $attend =& $model->att_field;
            }
        }

        //日付データ
        if ($model->diaryDate == "") {
            $model->diaryDate = str_replace("-", "/", CTRL_DATE);
        }
        $arg["sel"]["DIARY_DATE"] = View::popUpCalendar2($objForm, "DIARY_DATE", str_replace("-", "/", $model->diaryDate), "reload=true", "btn_submit('main');");

        //天気
        $query = knjg045Query::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $diaryData["WEATHER"], "WEATHER", $extra, 1, "BLANK");

        //天気2
        $query = knjg045Query::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $diaryData["WEATHER2"], "WEATHER2", $extra, 1, "BLANK");

        //欠席状況表示
        if ($model->Properties["useNurseoffAttend"] == "1") {
            $arg["useNurseoffAttend"] = 1;

            //項目行
            $grade_cnt = 3;
            $grade_list = $sep = "";
            $header  = "<td width=\"110\" align=\"center\" colspan=\"2\"></td>";
            foreach ($model->grade as $gkey => $gval) {
                $header .= "<td width=\"80\" align=\"center\">".$gval."</td>";

                $grade_cnt++;

                $grade_list .= $sep.$gkey;
                $sep = ",";
            }
            $header .= "<td width=\"80\" align=\"center\">計</td>";

            $arg["header"]["GRADE"] = $header;
            $arg["header"]["COLSPAN"] = $grade_cnt;
            $arg["header"]["WIDTH"] = $grade_cnt * 80;
            knjCreateHidden($objForm, "GRADE_LIST", $grade_list);

            //欠席状況
            foreach ($model->di_cd as $dkey => $dval) {
                $setData = "";
                //出欠項目
                if ($dkey == '4') {
                    $setData .= "<td width=\"30\" class=\"no_search\" align=\"center\" rowspan=\"3\">欠<br>席</td>";
                }
                $colspan = (in_array($dkey, array('2','3'))) ? 2 : 1;
                $setData .= "<td width=\"80\" class=\"no_search\" align=\"center\" colspan=\"{$colspan}\">".$dval."</td>";

                $total = 0;
                foreach ($model->grade as $gkey => $gval) {
                    //欠席状況テキスト
                    $extra = "STYLE=\"text-align:right\" onblur=\"calc(this, '$dkey');\"";
                    $textbox = knjCreateTextBox($objForm, $attend[$dkey][$gkey], "CNT_".$dkey."_".$gkey, 3, 3, $extra);
                    $setData .= "<td width=\"80\" align=\"center\">".$textbox."</td>";

                    if ($attend[$dkey][$gkey] > 0) {
                        $total += $attend[$dkey][$gkey];
                    }
                }
                //合計
                $id = 'total_'.$dkey;
                $setData .= "<td width=\"80\" align=\"center\" id='$id'>".$total."</td>";

                $arg["attend"][] = $setData;
            }
        }

        //記事
        $extra = "style=\"height:75px;\"";
        $arg["data"]["NEWS"] = KnjCreateTextArea($objForm, "NEWS", 5, 91, "soft", $extra, $diaryData["NEWS"]);

        //欠勤者ほかを取得し、セット
        if (!isset($model->warning)) {
            if ($model->cmd !== 'hr_class') {
                $opt_show1 = $opt_value1 = array();
                $opt_show2 = $opt_value2 = array();
                $opt_show3 = $opt_value3 = array();
                $opt_show4 = $opt_value4 = array();
                $opt_show6 = $opt_value6 = array();
                $result = $db->query(knjg045Query::getStaffData($model));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["STAFF_DIV"] === '1') {
                        $opt_show1[]  = $row["STAFFNAME_SHOW"];
                        $opt_value1[] = $row["STAFFCD"];
                    } elseif ($row["STAFF_DIV"] === '2') {
                        $opt_show2[]  = $row["STAFFNAME_SHOW"];
                        $opt_value2[] = $row["STAFFCD"];
                    } elseif ($row["STAFF_DIV"] === '3') {
                        $opt_show3[]  = $row["STAFFNAME_SHOW"];
                        $opt_value3[] = $row["STAFFCD"];
                    } elseif ($row["STAFF_DIV"] === '4') {
                        $opt_show4[]  = $row["STAFFNAME_SHOW"];
                        $opt_value4[] = $row["STAFFCD"];
                    } elseif ($row["STAFF_DIV"] === '5') {
                        $opt_show5[]  = $row["STAFFNAME_SHOW"].'('.$row["COUNT"].')';
                        $opt_value5[] = $row["STAFFCD"].'-'.$row["COUNT"];
                    } elseif ($row["STAFF_DIV"] === '6') {
                        $opt_show6[]  = $row["STAFFNAME_SHOW"].'('.$row["COUNT"].')';
                        $opt_value6[] = $row["STAFFCD"].'-'.$row["COUNT"];
                    }
                }
                $result->free();
                $Row_Staff["STAFFNAME_SHOW1"]    = implode(",", $opt_show1);
                $Row_Staff["STAFFCD1"]           = implode(",", $opt_value1);
                $Row_Staff["STAFFNAME_SHOW2"]    = implode(",", $opt_show2);
                $Row_Staff["STAFFCD2"]           = implode(",", $opt_value2);
                $Row_Staff["STAFFNAME_SHOW3"]    = implode(",", $opt_show3);
                $Row_Staff["STAFFCD3"]           = implode(",", $opt_value3);
                $Row_Staff["STAFFNAME_SHOW4"]    = implode(",", $opt_show4);
                $Row_Staff["STAFFCD4"]           = implode(",", $opt_value4);
                //年組指定から変更
                $Row_Staff["STAFFNAME_SHOW6"]    = implode(",", $opt_show6);
                $Row_Staff["STAFFCD6"]           = implode(",", $opt_value6);
            } else {
                $Row_Staff = $model->field;
            }
        } else {
            $Row_Staff = $model->field;
        }
        //欠勤者
        $extra = "STYLE=\"height:65px;WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly";
        $arg["data"]["STAFFNAME_SHOW1"] = knjCreateTextArea($objForm, "STAFFNAME_SHOW1", "4", "44", "wrap", $extra, $Row_Staff["STAFFNAME_SHOW1"]);

        //遅刻・早退
        $extra = "STYLE=\"height:65px;WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly";
        $arg["data"]["STAFFNAME_SHOW2"] = knjCreateTextArea($objForm, "STAFFNAME_SHOW2", "4", "44", "wrap", $extra, $Row_Staff["STAFFNAME_SHOW2"]);

        //早退者
        $extra = "STYLE=\"height:40px;WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly";
        $arg["data"]["STAFFNAME_SHOW3"] = knjCreateTextArea($objForm, "STAFFNAME_SHOW3", "2", "44", "wrap", $extra, $Row_Staff["STAFFNAME_SHOW3"]);

        //出張者
        $extra = "STYLE=\"height:40px;WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly";
        $arg["data"]["STAFFNAME_SHOW4"] = knjCreateTextArea($objForm, "STAFFNAME_SHOW4", "2", "44", "wrap", $extra, $Row_Staff["STAFFNAME_SHOW4"]);

        //年組取得
        $query = knjg045Query::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('hr_class');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");

        //科目担任
        if (!isset($model->warning)) {
            $opt_show5 = $opt_value5 = array();
            $result = $db->query(knjg045Query::getStaffData($model, $model->field["GRADE_HR_CLASS"]));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["STAFF_DIV"] === '5') {
                    $opt_show5[]  = $row["STAFFNAME_SHOW"].'('.$row["COUNT"].')';
                    $opt_value5[] = $row["STAFFCD"].'-'.$row["COUNT"];
                }
            }
            $result->free();
            $Row_Staff["STAFFNAME_SHOW5"]    = implode(",", $opt_show5);
            $Row_Staff["STAFFCD5"]           = implode(",", $opt_value5);
        } else {
            $Row_Staff = $model->field;
        }

        //補欠授業
        $extra = "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly";
        $arg["data"]["STAFFNAME_SHOW5"] = knjCreateTextBox($objForm, $Row_Staff["STAFFNAME_SHOW5"], "STAFFNAME_SHOW5", 44, 44, $extra);

        //その他補欠
        $extra = "STYLE=\"height:40px;WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly";
        $arg["data"]["STAFFNAME_SHOW6"] = knjCreateTextArea($objForm, "STAFFNAME_SHOW6", "2", "44", "wrap", $extra, $Row_Staff["STAFFNAME_SHOW6"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $Row_Staff);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjg045Form1.html", $arg);
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
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["sel"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{

    //職員状況　選択
    //欠席者
    $extra = "onclick=\"return btn_submit('kesseki');\" STYLE=\"height:30px;WIDTH:100px;\"";
    $arg["button"]["btn_kesseki"] = knjCreateBtn($objForm, "btn_kesseki", "欠席者", $extra);
    //遅参者
    $extra = "onclick=\"return btn_submit('chikoku');\" STYLE=\"height:30px;WIDTH:100px;\"";
    $arg["button"]["btn_chikoku"] = knjCreateBtn($objForm, "btn_chikoku", "遅参者", $extra);
    //早退者
    $extra = "onclick=\"return btn_submit('soutai');\" STYLE=\"height:30px;WIDTH:100px;\"";
    $arg["button"]["btn_soutai"] = knjCreateBtn($objForm, "btn_soutai", "早退者", $extra);
    //出張者
    $extra = "onclick=\"return btn_submit('shuchou');\" STYLE=\"height:30px;WIDTH:100px;\"";
    $arg["button"]["btn_shuchou"] = knjCreateBtn($objForm, "btn_shuchou", "出張者", $extra);
    //補欠授業
    $extra = "onclick=\"return btn_submit('hoketsu');\" STYLE=\"height:20px;WIDTH:100px;\"";
    $arg["button"]["btn_hoketsu"] = knjCreateBtn($objForm, "btn_hoketsu", "補欠授業", $extra);
    //その他補欠
    $extra = "onclick=\"return btn_submit('etc_hoketsu');\" STYLE=\"height:30px;WIDTH:100px;\"";
    $arg["button"]["btn_etc_hoketsu"] = knjCreateBtn($objForm, "btn_etc_hoketsu", "その他補欠", $extra);

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
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJG045");

    knjCreateHidden($objForm, "STAFFCD1", $Row_Staff["STAFFCD1"]);
    knjCreateHidden($objForm, "STAFFCD2", $Row_Staff["STAFFCD2"]);
    knjCreateHidden($objForm, "STAFFCD3", $Row_Staff["STAFFCD3"]);
    knjCreateHidden($objForm, "STAFFCD4", $Row_Staff["STAFFCD4"]);
    knjCreateHidden($objForm, "STAFFCD5", $Row_Staff["STAFFCD5"]);
    knjCreateHidden($objForm, "STAFFCD6", $Row_Staff["STAFFCD6"]);
}
