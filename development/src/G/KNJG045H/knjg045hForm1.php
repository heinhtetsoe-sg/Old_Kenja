<?php

require_once('for_php7.php');


class knjg045hForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjg045hindex.php", "", "main");

        //タイトル
        if ($model->schoolKind == "P") {
            $arg["TITLE"] = "小学校";
        } else if ($model->schoolKind == "J") {
            $arg["TITLE"] = "中学校";
        } else {
            $arg["TITLE"] = "高校";
        }

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != "read1" && $model->cmd != "read2" && $model->cmd != "lesson") {
            $query = knjg045hQuery::getDiary($model);
            $diaryData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $diaryData = $model->field;
        }

        if ($model->cmd == "read1") {
            $diaryData["BUSINESS_TRIP"] = getReadData($db, $model, "1");
        }
        if ($model->cmd == "read2") {
            $diaryData["VACATION"] = getReadData($db, $model, "2");
        }

        //日付データ
        if ($model->diaryDate == "") $model->diaryDate = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DIARY_DATE"] = View::popUpCalendar($objForm  ,"DIARY_DATE" ,str_replace("-","/",$model->diaryDate),"reload=true");
        //チェック用hidden
        knjCreateHidden($objForm, "DEFOULTDATE", $model->diaryDate);

        //学期(学期マスタに存在しない場合も値を返す)
        $model->kyouseiSeme = getSemester($db, $model);

        //天気
        $query = knjg045hQuery::getWeather($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $diaryData["WEATHER"], "WEATHER", $extra, 1, "BLANK");

        //学年タイトル
        $query = knjg045hQuery::getGrade($model);
        $result = $db->query($query);
        $gradeArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["GRADE".$row["GRADE_CD"]] = $row["GRADE_NAME1"];
            $gradeArray[$row["GRADE_CD"]] = $row["GRADE"];
        }
        $result->free();
        //再計算
        $extra = "onclick=\"return btn_submit('lesson');\"";
        $arg["button"]["btn_lesson"] = knjCreateBtn($objForm, "btn_lesson", "再計算", $extra);
        $setStyle = "";
        if ($model->cmd == "lesson") {
            foreach ($gradeArray as $gKey => $gVal) {
                $diaryData["LESSON".($gKey * 1)] = getLessonData($db, $model, $gVal);
            }
            $setStyle = " background-color : #ff0099 ";
        }
        //授業日数1
        $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right; {$setStyle}\"";
        $arg["data"]["LESSON1"] = knjCreateTextBox($objForm, $diaryData["LESSON1"], "LESSON1", 3, 3, $extra);
        //授業日数2
        $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right; {$setStyle}\"";
        $arg["data"]["LESSON2"] = knjCreateTextBox($objForm, $diaryData["LESSON2"], "LESSON2", 3, 3, $extra);
        //授業日数3
        $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right; {$setStyle}\"";
        $arg["data"]["LESSON3"] = knjCreateTextBox($objForm, $diaryData["LESSON3"], "LESSON3", 3, 3, $extra);
        //授業日数4
        $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right; {$setStyle}\"";
        $arg["data"]["LESSON4"] = knjCreateTextBox($objForm, $diaryData["LESSON4"], "LESSON4", 3, 3, $extra);
        if ($model->schoolKind == "P") {
            $arg["data"]["colspanSingle"] = "6";
            $arg["data"]["colspanLeft"] = "3";
            $arg["data"]["colspanRight"] = "3";
            $arg["grade6"] = "1";
            //授業日数5
            $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right; {$setStyle}\"";
            $arg["data"]["LESSON5"] = knjCreateTextBox($objForm, $diaryData["LESSON5"], "LESSON5", 3, 3, $extra);
            //授業日数6
            $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right; {$setStyle}\"";
            $arg["data"]["LESSON6"] = knjCreateTextBox($objForm, $diaryData["LESSON6"], "LESSON6", 3, 3, $extra);
        } else {
            $arg["data"]["colspanSingle"] = "5";
            $arg["data"]["colspanLeft"] = "3";
            $arg["data"]["colspanRight"] = "2";
            $arg["grade3"] = "1";
        }


        //重要事項
        $extra = "onkeyup=\"charCount(this.value, 10, (22 * 2), true);\" oncontextmenu =\"charCount(this.value, 10, (22 * 2), true);\"";
        $arg["data"]["IMPORTANT_MATTER"] = knjCreateTextArea($objForm, "IMPORTANT_MATTER", "10", "44", "wrap", $extra, $diaryData["IMPORTANT_MATTER"]);

        //来校者・氏名・用件
        $extra = "onkeyup=\"charCount(this.value, 10, (22 * 2), true);\" oncontextmenu =\"charCount(this.value, 10, (22 * 2), true);\"";
        $arg["data"]["GUEST"] = knjCreateTextArea($objForm, "GUEST", "10", "44", "wrap", $extra, $diaryData["GUEST"]);

        //記事
        $extra = "onkeyup=\"charCount(this.value, 10, (47 * 2), true);\" oncontextmenu =\"charCount(this.value, 47, (22 * 2), true);\"";
        $arg["data"]["REPORT"] = knjCreateTextArea($objForm, "REPORT", "10", "94", "wrap", $extra, $diaryData["REPORT"]);

        //収受公文書
        $extra = "onkeyup=\"charCount(this.value, 10, (22 * 2), true);\" oncontextmenu =\"charCount(this.value, 10, (22 * 2), true);\"";
        $arg["data"]["RECEIVE_OFFICIAL_DOCUMENTS"] = knjCreateTextArea($objForm, "RECEIVE_OFFICIAL_DOCUMENTS", "10", "44", "wrap", $extra, $diaryData["RECEIVE_OFFICIAL_DOCUMENTS"]);

        //発送公文書
        $extra = "onkeyup=\"charCount(this.value, 10, (22 * 2), true);\" oncontextmenu =\"charCount(this.value, 10, (22 * 2), true);\"";
        $arg["data"]["SENDING_OFFICIAL_DOCUMENTS"] = knjCreateTextArea($objForm, "SENDING_OFFICIAL_DOCUMENTS", "10", "44", "wrap", $extra, $diaryData["SENDING_OFFICIAL_DOCUMENTS"]);

        //出張
        $extra = "onkeyup=\"charCount(this.value, 7, (22 * 2), true);\" oncontextmenu =\"charCount(this.value, 7, (22 * 2), true);\"";
        $arg["data"]["BUSINESS_TRIP"] = knjCreateTextArea($objForm, "BUSINESS_TRIP", "7", "44", "wrap", $extra, $diaryData["BUSINESS_TRIP"]);
        //出張読込
        $extra = "onclick=\"return btn_submit('read1');\"";
        $arg["button"]["btn_trip"] = knjCreateBtn($objForm, "btn_trip", "許可願いより読込", $extra);

        //休暇
        $extra = "onkeyup=\"charCount(this.value, 7, (22 * 2), true);\" oncontextmenu =\"charCount(this.value, 7, (22 * 2), true);\"";
        $arg["data"]["VACATION"] = knjCreateTextArea($objForm, "VACATION", "7", "44", "wrap", $extra, $diaryData["VACATION"]);
        //休暇読込
        $extra = "onclick=\"return btn_submit('read2');\"";
        $arg["button"]["btn_vacation"] = knjCreateBtn($objForm, "btn_vacation", "許可願いより読込", $extra);

        //慶弔・産休等
        $extra = "onkeyup=\"charCount(this.value, 7, (22 * 2), true);\" oncontextmenu =\"charCount(this.value, 7, (22 * 2), true);\"";
        $arg["data"]["SPECIAL_LEAVE"] = knjCreateTextArea($objForm, "SPECIAL_LEAVE", "7", "44", "wrap", $extra, $diaryData["SPECIAL_LEAVE"]);

        //欠勤
        $extra = "onkeyup=\"charCount(this.value, 5, (22 * 2), true);\" oncontextmenu =\"charCount(this.value, 5, (22 * 2), true);\"";
        $arg["data"]["ABSENCE"] = knjCreateTextArea($objForm, "ABSENCE", "5", "44", "wrap", $extra, $diaryData["ABSENCE"]);

        //遅刻・早退
        $extra = "onkeyup=\"charCount(this.value, 5, (22 * 2), true);\" oncontextmenu =\"charCount(this.value, 5, (22 * 2), true);\"";
        $arg["data"]["LATE_EARLY"] = knjCreateTextArea($objForm, "LATE_EARLY", "5", "44", "wrap", $extra, $diaryData["LATE_EARLY"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjg045hForm1.html", $arg); 
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

function getReadData($db, $model, $cd)
{
    $query = knjg045hQuery::getPermRequest($model, $cd);
    $result = $db->query($query);
    $setData = "";
    $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setData .= $sep.str_replace("-", "/", $row["SDATE"])."～".str_replace("-", "/", $row["EDATE"])."　".$row["STAFFNAME"];
        $sep = "、";
    }
    return $setData;
}

//学期マスタ降順ソート
//学期  開始日      終了日
// 2    2006-10-11  2007-03-15
// 1    2006-04-04  2006-10-08
// MAX学期の終了日は、その月のMAX日をセット
// それ以外の終了日は、その前の学期の開始日-1をセット
// １学期の開始日は、01日とする。
//
//上記データは変換後、以下のようになる。
//学期  開始日      終了日
// 2    2006-10-11  2007-03-31
// 1    2006-04-01  2006-10-10
function getSemester($db, $model)
{
    $query = knjg045hQuery::getSemester($model);
    $result = $db->query($query);
    $befSdate = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $edateArray = preg_split("/-/", $row["EDATE"]);
        $sdateArray = preg_split("/-/", $row["SDATE"]);

        // １学期の開始日は、01日とする。
        $row["SDATE"] = $row["SEMESTER"] == "1" ? $sdateArray[0]."-".$sdateArray[1]."-01" : $row["SDATE"];

        // MAX学期の終了日は、その月のMAX日をセット
        // それ以外の終了日は、その前の学期の開始日-1をセット
        $row["EDATE"] = $befSdate ? $befSdate : $edateArray[0]."-".$edateArray[1]."-".$row["DAY_MAX"];

        if ($row["SDATE"] <= str_replace("/", "-", $model->diaryDate) &&
            str_replace("/", "-", $model->diaryDate) <= $row["EDATE"]
        ) {
            return $row["SEMESTER"];
        }
        $befSdate = date("Y-m-d", strtotime("{$row["SDATE"]} -1 day"));
    }
    return "";
}

function getLessonData($db, $model, $grade)
{
    $attendLesson = 0;
    $eventLesson = 0;
    $query = knjg045hQuery::getMaxDate($model);
    $lastDay = $db->getOne($query);

    $dateArray = preg_split("/\//", $model->diaryDate);
    $monthIn = "('";
    $monthSep = "";
    for ($mCnt = 4; $mCnt <= 15; $mCnt++) {
        $setMonth = $mCnt < 13 ? sprintf("%02d", $mCnt) : sprintf("%02d", $mCnt - 12);
        if ($dateArray[1] == $setMonth) {
            if ($dateArray[2] == $lastDay) {
                $monthIn .= $monthSep.$setMonth;
                $monthSep = "','";
            }
            break;
        }
        $monthIn .= $monthSep.$setMonth;
        $monthSep = "','";
    }
    $monthIn .= "')";

    $query = knjg045hQuery::getAttendLesson($model, $monthIn, $grade);
    $attendLesson = $db->getOne($query);

    if ($dateArray[2] != $lastDay) {
        $fDay = $dateArray[0]."-".$dateArray[1]."-01";
        $eDay = $dateArray[0]."-".$dateArray[1]."-".$dateArray[2];
        $query = knjg045hQuery::getHoliCnt($model, $grade, $fDay, $eDay);
        $holiCnt = $db->getOne($query);
        $eventLesson = $dateArray[2] - $holiCnt;
    }

    return $attendLesson + $eventLesson;
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更　新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削　除", $extra);

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終　了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJG045H");
    knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);
    knjCreateHidden($objForm, "hibiNyuuryokuNasi", $model->Properties["hibiNyuuryokuNasi"]);
    knjCreateHidden($objForm, "KYOUSEI_SEMESTER", $model->kyouseiSeme);

}
?>
