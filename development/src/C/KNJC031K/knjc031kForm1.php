<?php

require_once('for_php7.php');


class knjc031kForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjc031kindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = CTRL_YEAR;

        /* 学期 */
        $query = knjc031kQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        /* 対象学級 */
        $hrName = makeHrclassCmb($objForm, $arg, $db, $model);

        $model->school_kind = $db->getOne(knjc031kQuery::getSchoolKind($model));

        /* 対象月 */
        $monthName = makeMonthSemeCmb($objForm, $arg, $db, $model);

        //授業日数セット
        makeLessonSet($objForm, $arg, $db, $model);

        //出欠完了データ
        if (isset($model->warning)) {
            //エラーの場合
            $executed = $model->field["EXECUTED"];
        } else {
            //取得
            $executed = $db->getOne(knjc031kQuery::getExecuted($model));
        }

        //出欠完了チェックボックス
        $extra = "disabled";
        if ($model->field["hr_class"] && $model->field["month"]) {
            $extra  = ($executed == "1") ? "checked" : "";
            $extra .= " id=\"EXECUTED\" onclick=\"checkExecutedLabel(this, 'zumi');\"";
        }
        $arg["EXECUTED"] = knjCreateCheckBox($objForm, "EXECUTED", "1", $extra, "");

        //出欠完了チェックボックス（ラベル）
        $zumi = "";
        if ($model->field["hr_class"] && $model->field["month"]) {
            if ($executed == "1") {
                $zumi = "<LABEL for=\"EXECUTED\"><span id=\"zumi\" style=\"color:white;\">出欠済</span></LABEL>";
            } else {
                $zumi = "<LABEL for=\"EXECUTED\"><span id=\"zumi\" style=\"color:#ff0099;\">出欠未</span></LABEL>";
            }
        }
        $arg["EXECUTED_LABEL"] = $zumi;

        /* タイトル設定 */
        $model->titleVal = setTitleData($objForm, $arg, $db, $model);

        /* 可変サイズ */
        $allWidth = 1150;
        $otherCol = 13;
        $ruikeiCol = 8;
        $col = 2;
        $width = 70;
        /* C001 */
        $setC001 = "";
        foreach ($model->titleVal as $key => $val) {
            $allWidth = (int)$allWidth + 35;
            $otherCol = (int)$otherCol + 1;
        }
        /* ウイルスあり */
        if ($model->Properties["useVirus"] == "true") {
            $arg["useVirus"] = "1";
            $allWidth = (int)$allWidth + 80;
            $otherCol = (int)$otherCol + 1;
            $ruikeiCol = (int)$ruikeiCol + 1;
            $col = (int)$col + 1;
            $width = (int)$width + 35;
        }
        /* 交止あり */
        $arg["SUSPEND_NAME"] = "出停";
        if ($model->Properties["useKoudome"] == "true") {
            $arg["useKoudome"] = "1";
            $arg["SUSPEND_NAME"] = "法止";
            $allWidth = (int)$allWidth + 80;
            $otherCol = (int)$otherCol + 1;
            $ruikeiCol = (int)$ruikeiCol + 1;
            $col = (int)$col + 1;
            $width = (int)$width + 35;
        }
        $arg["useAllWidth"] = $allWidth;
        $arg["useOtherThanRuikeiCol"] = $otherCol;
        $arg["useRuikeiCol"] = $ruikeiCol;
        $arg["useCol"] = $col;
        $arg["usewidth"] = $width;

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $objUp, $hrName, $monthName);

        /* ボタン作成 */
        makeButton($objForm, $arg, $db, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC031K");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "unUseOffdays", $model->Properties["unUseOffdays"]);
        knjCreateHidden($objForm, "unUseAbroad", $model->Properties["unUseAbroad"]);
        knjCreateHidden($objForm, "unUseAbsent", $model->Properties["unUseAbsent"]);
        knjCreateHidden($objForm, "changeVal", 0);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "use_Attend_zero_hyoji", $model->Properties["use_Attend_zero_hyoji"]);
        knjCreateHidden($objForm, "HIDDEN_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_MONTH");
        knjCreateHidden($objForm, "HIDDEN_EXECUTED");

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjc031kForm1.html", $arg);
    }
}

//対象学級コンボ作成
function makeHrclassCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc031kQuery::selectHrClass($model);
    $result     = $db->query($query);
    $opt_hr     = array();

    /* 2004/08/27 arakaki 近大-作業依頼書20040824.doc */
    $opt_hr[] = array("label" => "",
                      "value" => "");

    while($row  = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt_hr[] = array("label" => $row["HR_NAME"],
                          "value" => $row["GRADE"]."-".$row["HR_CLASS"]);

        /* 初期データセット (起動時に先頭のクラスを対象学級とする) */
        /* 2004/08/27 arakaki 近大-作業依頼書20040824.doc */
        if($model->field["hr_class"] == "" || $model->field["hr_class"] == NULL){
            $model->field["hr_class"] = "";
            $model->field["grade"]    = "";
            $model->field["class"]    = "";
        }
    }
    $arg["hr_class"] = knjCreateCombo($objForm, "HR_CLASS", $model->field["hr_class"], $opt_hr, "onChange=\"btn_submit('change')\";", 1);
    $rtnHrname = "";
    for ($i = 0; $i < get_count($opt_hr); $i++) {
        $rtnHrname = ($opt_hr[$i]["value"] == $model->field["hr_class"]) ? $opt_hr[$i]["label"] : $rtnHrname;
    }
    return $rtnHrname;
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc031kQuery::selectSemesAll();
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month  = array();
    $opt_month[] = array("label" => "",
                         "value" => "");

    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $getdata = $db->getRow(knjc031kQuery::selectMonthQuery($month, $model), DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
            }
        }
    }
    if($model->field["month"] == "" || $model->field["month"] == NULL){
        $model->field["month"] = "";
    }
    $arg["month"] = knjCreateCombo($objForm, "MONTH", $model->field["month"], $opt_month, "onChange=\"btn_submit('change')\";", 1);

    $rtnMonth = "";
    for ($i = 0; $i < get_count($opt_month); $i++) {
        $rtnMonth = ($opt_month[$i]["value"] == $model->field["month"]) ? $opt_month[$i]["label"] : $rtnMonth;
    }
    return $rtnMonth;
}

//授業日数セット
function makeLessonSet(&$objForm, &$arg, $db, &$model) {

    if ($model->cmd == "change" || $model->cmd == "reset") {
        $model->field["LESSON_SET"] = "";
    }

    //テキスト内の背景色
    $bgcolor = "white";

    //ATTEND_SEMES_DATのMAX授業日数セット
    $query = knjc031kQuery::getMaxLesson1($model);
    $attend_semes = $db->getOne($query);
    if ($model->field["LESSON_SET"] == "") {
        $model->field["LESSON_SET"] =  $attend_semes;
    }

    //ATTEND_LESSON_MSTのMAX授業日数セット
    $query = knjc031kQuery::getMaxLesson2($model);
    $attend_lesson = $db->getOne($query);
    if ($model->field["LESSON_SET"] == "") {
        $model->field["LESSON_SET"] =  $attend_lesson;
        if ($model->field["LESSON_SET"] != "") $bgcolor = "#ff0099";
    }

    if (!$model->field["hr_class"] || !$model->field["month"]) {
        $bgcolor = "white";
    }

    //授業日数テキスト(セット用)
    $extra = " style=\"text-align: right;background-color : {$bgcolor};\" onChange=\"this.style.background='#ccffcc';\" onblur=\"this.value=toInteger(this.value);\"";
    $arg["LESSON_SET"] = knjCreateTextBox($objForm, $model->field["LESSON_SET"], "LESSON_SET", 3, 3, $extra);

    //反映ボタン
    if ($model->field["hr_class"] && $model->field["month"]) {
        $extra = "onclick=\"reflect();\"";
    } else {
        $extra = "disabled";
    }
    $arg["btn_reflect"] = knjCreateBtn($objForm, "btn_reflect", "反 映", $extra);

    return;
}

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, $model)
{
    $rtnTitle = array();
    $result = $db->query(knjc031kQuery::getSickDiv());
    $setHiddenTitle = "";
    $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["title".$row["VALUE"]] = "1";
        $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnTitle[$row["VALUE"]] = $row["VALUE"];
        knjCreateHidden($objForm, $model->c001[$row["VALUE"]]."_FLG", 1);
    }
    $result->free();

    return $rtnTitle;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $hrName, $monthName)
{
    $tukiAndGakki = explode("-", $model->field["month"]);
    $tuki  = $tukiAndGakki[0];
    $gakki = $tukiAndGakki[1];
    $query = knjc031kQuery::getAppointedDay($tuki, $gakki, $model);
    $appointed_day = $db->getOne($query);

    $schoolMst = array();
    $query = knjc031kQuery::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }

    $query      = knjc031kQuery::selectAttendQuery($model, $schoolMst);
    $result     = $db->query($query);

    $monthsem = array();
    $monthsem = preg_split("/-/", $model->field["month"]);

    knjCreateHidden($objForm, "objCntSub", get_count($db->getCol($query)));

    $counter  = 0;
    $colorFlg = false;
    $data = array();
    $schCnt = 0;
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        /* 出欠月別累積データ */
        if (isset($model->warning)) {
            if (is_array($model->reset[$row["SCHREGNO"]])) {
                foreach ($model->reset[$row["SCHREGNO"]] as $key => $val) {
                    $row[$key] = $val;
                }
            }
        }

        $setArray = array();
        $setArray["LESSON"]     =  array("SIZE" => 2, "MAXLEN" => 3);
        $setArray["OFFDAYS"]    =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseOffdays"] == "true" ? " disabled " : "");
        $setArray["ABROAD"]     =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseAbroad"] == "true" ? " disabled " : "");
        $setArray["ABSENT"]     =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseAbsent"] == "true" ? " disabled " : "");
        $setArray["SUSPEND"]    =  array("SIZE" => 2, "MAXLEN" => 3);
        if ($model->Properties["useKoudome"] == "true") {
            $setArray["KOUDOME"]    =  array("SIZE" => 2, "MAXLEN" => 3);
        }
        if ($model->Properties["useVirus"] == "true") {
            $setArray["VIRUS"]    =  array("SIZE" => 2, "MAXLEN" => 3);
        }
        $setArray["MOURNING"]   =  array("SIZE" => 2, "MAXLEN" => 3);
        /* C001 */
        foreach ($model->titleVal as $key => $val) {
            $setArray[$model->c001[$key]]       =  array("SIZE" => 2, "MAXLEN" => 3);
        }
        $setArray["LATE"]       =  array("SIZE" => 2, "MAXLEN" => 3);
        $setArray["EARLY"]      =  array("SIZE" => 2, "MAXLEN" => 3);
        $setArray["REMARK"]     =  array("SIZE" => 30, "MAXLEN" => 30);

        foreach ($setArray as $key => $val) {
            if ($key == "REMARK") {
                $extra = " onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onKeyDown=\"keyChangeEntToTab(this)\"; onPaste=\"return showPaste(this, ".$schCnt.");\" ";
                $row[$key] = knjCreateTextBox($objForm, $row[$key], $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
            } else {
                $setStyle = "";
                if ($val["DISABLED"]) {
                    $setStyle = " background-color : #999999 ";
                }
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $value = $row[$key];
                } else {
                    $value = ($row[$key] != 0) ? $row[$key] : "";
                }
                $extra = $val["DISABLED"]." STYLE=\"text-align: right; {$setStyle}\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onblur=\"this.value=toInteger(this.value)\"; onKeyDown=\"keyChangeEntToTab(this)\"; onPaste=\"return showPaste(this, ".$schCnt.");\" ";
                $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
            }
        }
        $schCnt++;

        //異動者（退学・転学・卒業）
        $idou_year = ($monthsem[0] < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
        $idou_day = ($appointed_day == "") ? getFinalDay($db, $monthsem[0], $monthsem[1]) : $appointed_day;
        $idou_date = $idou_year.'-'.$monthsem[0].'-'.$idou_day;
        $idou = $db->getOne(knjc031kQuery::getIdouData($row["SCHREGNO"], $idou_date));

        //異動月取得
        $smonth = $emonth = array();
        $smonth = $db->getCol(knjc031kQuery::getTransferData2($row["SCHREGNO"], "s"));
        $emonth = $db->getCol(knjc031kQuery::getTransferData2($row["SCHREGNO"], "e"));

        //異動者（留学・休学）
        $idou2 = $db->getOne(knjc031kQuery::getTransferData1($row["SCHREGNO"], $idou_date));
        $idou3 = 0;
        if (in_array(sprintf('%02d', $monthsem[0]), $smonth) || in_array(sprintf('%02d', $monthsem[0]), $emonth)) {
            $idou3 = 1;
        }

        //異動期間は背景色を黄色にする
        $row["BGCOLOR_IDOU"] = ($idou > 0 || $idou2 > 0 || $idou3 > 0) ? "bgcolor=yellow" : "";

        /* hidden(学籍番号) */
        $row["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$row["SCHREGNO"]."\">";

        //5行毎に色を変える
        if ($counter % 5 == 0) {
            $colorFlg = !$colorFlg;
        }
        $row["BGCOLOR_ROW"] = $colorFlg ? "#ffffff" : "#cccccc";
        $counter++;

        //データがない場合、氏名の背景色を変える
        if (strlen($row["SEM_SCHREGNO"]) == 0) {
            $row["BGCOLOR_NAME_SHOW"] = "bgcolor=#ccffcc";
        }

        $data[] = $row;
    }
    $arg["attend_data"] = $data;

    $arg["SET_APPOINTED_DAY"] = $appointed_day;
    knjCreateHidden($objForm, "SET_APPOINTED_DAY", $appointed_day);
}

//最終日取得
function getFinalDay($db, $month, $semester)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc031kQuery::selectSemesAll($semester),DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month &&
        $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }
    return $lastday;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{

    //保存ボタン
    $extra = "";
    $arg["btn_update"] =     knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $extra = "";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $extra = "";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeMsg();\"");
    //印刷
    $extra = "";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");

    //セキュリティーチェック
    $securityCnt = $db->getOne(knjc031kQuery::getSecurityHigh());
    $csvSetName = "ＣＳＶ処理";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル処理";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        //ＣＳＶ処理ボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_C031K/knjx_c031kindex.php?SEND_PRGID=KNJC031K&SEND_AUTH={$model->auth}&SEND_hr_class={$model->field["hr_class"]}&SEND_month={$model->field["month"]}&SELECT_SCHOOLKIND={$model->selectSchoolKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $csvSetName, $extra);
    }
}

?>
