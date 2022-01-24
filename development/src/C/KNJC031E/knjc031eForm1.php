<?php

require_once('for_php7.php');


class knjc031eForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjc031eindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        $arg["fep"] = $model->Properties["FEP"];

        /* 処理年度 */
        $arg["year"] = CTRL_YEAR;

        /* 学期 */
        $query = knjc031eQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        //学級コンボ内容ラジオボタン 1:HRクラス 2:複式クラス
        $opt = array(1, 2);
        $model->field["SELECT_CLASS_TYPE"] = ($model->field["SELECT_CLASS_TYPE"] == "") ? "1" : $model->field["SELECT_CLASS_TYPE"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, "onChange=\"btn_submit('change_radio')\"; id=\"SELECT_CLASS_TYPE{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SELECT_CLASS_TYPE", $model->field["SELECT_CLASS_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //学級コンボ内容ラジオボタン表示
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1) $arg["class_type"] = 1;

        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            //複式学級コンボボックス
            $query = knjc031eQuery::getGroupHrClass($model);
            $extra = "onChange=\"btn_submit('change_group')\";";
            makeCmb($objForm, $arg, $db, $query, "GROUP_HR_CLASS", "GROUP_HR_CLASS", $model->field["GROUP_HR_CLASS"], $extra, 1, "BLANK");
        } else {
            /* 対象学級 */
            $hrName = makeHrclassCmb($objForm, $arg, $db, $model);
        }

        /* 対象月 */
        $monthName = makeMonthSemeCmb($objForm, $arg, $db, $model);

        //授業日数セット
        makeLessonSet($objForm, $arg, $db, $model);

        /* タイトル設定 */
        list($model->titleValC001, $model->titleValC002) = setTitleData($objForm, $arg, $db, $model);

        //クラス備考
        if ($model->Properties["useAttendSemesHrRemark"] == "1") {
            if (!isset($model->warning)) {
                $query = knjc031eQuery::getHrRemark($model);
                $model->field["HR_REMARK"] = $db->getOne($query);
            }
            $extra = "id=\"HR_REMARK\"";
            $arg["HR_REMARK"] = knjCreateTextArea($objForm, "HR_REMARK", "3", "30", "soft", $extra, $model->field["HR_REMARK"]);
            knjCreateHidden($objForm, "HR_REMARK_KETA", 30);//桁数は半角文字数文(全角10文字なら×2で20と設定)
            knjCreateHidden($objForm, "HR_REMARK_GYO", 3);
            KnjCreateHidden($objForm, "HR_REMARK_STAT", "statusarea1");
            $arg["USE_HR_REMARK"] = "1";
        } else {
            $arg["UN_USE_HR_REMARK"] = "1";
        }

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //コピー貼付用
        $copyArray = array();
        $copyHidden = "";

        /* 可変サイズ */
        $ruikeiCol = 6;
        $otherCol = 6;

        //A004
        foreach ($model->a004 as $keyA004 => $valA004) {
            $arg["A004_".$keyA004] = "1";
            if ($model->a004Field[$keyA004]) {
                $copyArray[$model->a004FieldSort[$keyA004]] = $model->a004Field[$keyA004];
                $otherCol = $otherCol + 1;
            }
        }

        //C001
        foreach ($model->c001 as $keyC001 => $valC001) {
            $arg["C001_".$keyC001] = "1";
            if ($model->c001Field[$keyC001]) {
                $copyArray[$model->c001FieldSort[$keyC001]] = $model->c001Field[$keyC001];
                $otherCol = $otherCol + 1;
            }
            if ($model->c001Ruikei[$keyC001]) {
                $ruikeiCol = $ruikeiCol + 1;
            }
        }

        //C002
        foreach ($model->c002 as $keyC002 => $valC002) {
            $arg["C002_".$keyC002] = "1";
            if ($model->c002Field[$keyC002]) {
                $copyArray[$model->c002FieldSort[$keyC002]] = $model->c002Field[$keyC002];
                $otherCol = $otherCol + 1;
            }
            if ($model->c002Ruikei[$keyC002]) {
                $ruikeiCol = $ruikeiCol + 1;
            }
        }

        //コピー貼付用hidden
        ksort($copyArray);
        $sep = "";
        foreach ($copyArray as $key => $val) {
            $copyHidden .= $sep.$val."[]";
            $sep = ":";
        }
        knjCreateHidden($objForm, "copyField", $copyHidden);

        /* 可変サイズ */
        $allWidth = 1250;
        $col = 2;
        $width = 70;
        foreach ($model->titleValC001 as $key => $val) {
            $allWidth = $allWidth + 35;
        }
        $setWidth = 100;
        if (get_count($model->titleValC002) == 1) {
            $setWidth = 160;
        } else if (get_count($model->titleValC002) == 2) {
            $setWidth = 120;
        } else if (get_count($model->titleValC002) == 3) {
            $setWidth = 105;
        } else if (get_count($model->titleValC002) == 4) {
            $setWidth = 100;
        }
        foreach ($model->titleValC002 as $key => $val) {
            $allWidth = $allWidth + 80;
        }
        /* ウイルスあり */
        if ($model->Properties["useVirus"] == "true" && $model->c001["19"]) {
            $arg["useVirus"] = "1";
            $allWidth = $allWidth + 80;
            $col = $col + 1;
            $width = $width + 35;
        }
        /* 交止あり */
        if ($model->Properties["useKoudome"] == "true" && $model->c001["25"]) {
            $arg["useKoudome"] = "1";
            $allWidth = $allWidth + 80;
            $col = $col + 1;
            $width = $width + 35;
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
        knjCreateHidden($objForm, "PRGID", "KNJC031E");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "unUseOffdays", $model->Properties["unUseOffdays"]);
        knjCreateHidden($objForm, "unUseAbroad", $model->Properties["unUseAbroad"]);
        knjCreateHidden($objForm, "unUseAbsent", $model->Properties["unUseAbsent"]);
        knjCreateHidden($objForm, "changeVal", 0);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
        knjCreateHidden($objForm, "use_Attend_zero_hyoji", $model->Properties["use_Attend_zero_hyoji"]);
        //コンボ変更時、MSG108表示用
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            knjCreateHidden($objForm, "SELECT_GROUP_HR_CLASS", $model->field["GROUP_HR_CLASS"]);
        }
        knjCreateHidden($objForm, "SELECT_HR_CLASS", $model->field["hr_class"]);
        knjCreateHidden($objForm, "SELECT_MONTH", $model->field["month"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);

        knjCreateHidden($objForm, "HIDDEN_SELECT_CLASS_TYPE");
        knjCreateHidden($objForm, "HIDDEN_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_GROUP_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_MONTH");
        knjCreateHidden($objForm, "HIDDEN_LESSON_SET");
        knjCreateHidden($objForm, "HIDDEN_MOVE_ENTER");

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML5($model, "knjc031eForm1.html", $arg);
    }
}

//対象学級コンボ作成
function makeHrclassCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc031eQuery::selectHrClass($model);
    $result     = $db->query($query);
    $opt_hr     = array();

    /* 2004/08/27 arakaki 近大-作業依頼書20040824.doc */
    $opt_hr[] = array("label" => "",
                      "value" => "");
    $cnt = 1;
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
        knjCreateHidden($objForm, "LIST_HR_CLASS" . $row["GRADE"]."-".$row["HR_CLASS"], $cnt);
        $cnt++;
    }
    $arg["hr_class"] = knjCreateCombo($objForm, "HR_CLASS", $model->field["hr_class"], $opt_hr, "onChange=\"btn_submit('change_hrclass')\";", 1);
    $rtnHrname = "";
    for ($i = 0; $i < get_count($opt_hr); $i++) {
        $rtnHrname = ($opt_hr[$i]["value"] == $model->field["hr_class"]) ? $opt_hr[$i]["label"] : $rtnHrname;
    }
    return $rtnHrname;
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc031eQuery::selectSemesAll();
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    //校種取得
    if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
        $schoolKind = "";
    } else if ($model->Properties["useSchool_KindField"] == "1" && strlen($model->field["hr_class"]) > 0) {
        $schoolKind = $db->getOne(knjc031eQuery::getSchoolKind($model));
    } else {
        $schoolKind = "";
    }

    $opt_month  = array();
    $opt_month[] = array("label" => "",
                         "value" => "");
    $cnt = 1;
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }

            if (!($model->Properties["useSchool_KindField"] == "1" && strlen($model->field["hr_class"]) == 0 && $model->field["SELECT_CLASS_TYPE"] == 1)) {
                $getdata = $db->getRow(knjc031eQuery::selectMonthQuery($month, $model, $schoolKind), DB_FETCHMODE_ASSOC);
                if (is_array($getdata)) {
                    $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                         "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
                    knjCreateHidden($objForm, "LIST_MONTH" . $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"], $cnt);
                    $cnt++;
                }
            }
        }
    }
    if($model->field["month"] == "" || $model->field["month"] == NULL){
        $model->field["month"] = "";
    }
    $arg["month"] = knjCreateCombo($objForm, "MONTH", $model->field["month"], $opt_month, "onChange=\"btn_submit('change_month')\";", 1);

    $rtnMonth = "";
    for ($i = 0; $i < get_count($opt_month); $i++) {
        $rtnMonth = ($opt_month[$i]["value"] == $model->field["month"]) ? $opt_month[$i]["label"] : $rtnMonth;
    }
    return $rtnMonth;
}

//授業日数セット
function makeLessonSet(&$objForm, &$arg, $db, &$model) {

    if (in_array($model->cmd, array("change_radio", "change_group", "change_hrclass", "change_month", "reset"))) {
        $model->field["LESSON_SET"] = "";
    }

    //テキスト内の背景色
    $bgcolor = "white";

    //ATTEND_SEMES_DATのMAX授業日数セット
    $query = knjc031eQuery::getMaxLesson1($model);
    $attend_semes = $db->getOne($query);
    if ($model->field["LESSON_SET"] == "") {
        $model->field["LESSON_SET"] =  $attend_semes;
    }

    //ATTEND_LESSON_MSTのMAX授業日数セット
    $query = knjc031eQuery::getMaxLesson2($model);
    $attend_lesson = $db->getOne($query);
    if ($model->field["LESSON_SET"] == "") {
        $model->field["LESSON_SET"] =  $attend_lesson;
        if ($model->field["LESSON_SET"] != "") $bgcolor = "#ff0099";
    }

    //クラス種別
    $class_flg = ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) ? 1 : 2;

    if (($class_flg == 1 && !$model->field["GROUP_HR_CLASS"]) || ($class_flg == 2 && !$model->field["hr_class"]) || !$model->field["month"]) {
        $bgcolor = "white";
    }

    //授業日数テキスト(セット用)
    $extra = " style=\"text-align: right; background-color: {$bgcolor};\" onChange=\"this.style.background='#ccffcc';\" onblur=\"this.value=toInteger(this.value);\"";
    $arg["LESSON_SET"] = knjCreateTextBox($objForm, $model->field["LESSON_SET"], "LESSON_SET", 3, 3, $extra);

    //反映ボタン
    if ((($class_flg == 1 && $model->field["GROUP_HR_CLASS"]) || ($class_flg == 2 && $model->field["hr_class"])) && $model->field["month"]) {
        $extra = "onclick=\"reflect();\"";
    } else {
        $extra = "disabled";
    }
    $arg["btn_reflect"] = knjCreateBtn($objForm, "btn_reflect", "反 映", $extra);

    //クリアボタン
    if ((($class_flg == 1 && $model->field["GROUP_HR_CLASS"]) || ($class_flg == 2 && $model->field["hr_class"])) && $model->field["month"]) {
        $extra = "onclick=\"lesson_clear();\"";
    } else {
        $extra = "disabled";
    }
    $extra .= " style=\"padding: 1px 5px;font-size: 80%;\"";
    $arg["btn_lesson_clear"] = knjCreateBtn($objForm, "btn_lesson_clear", "ｸﾘｱ", $extra);

    return;
}

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, $model)
{
    $rtnTitleC001 = array();
    $result = $db->query(knjc031eQuery::getSickDiv());
    $setHiddenTitle = "";
    $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["title".$row["VALUE"]] = "1";
        $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnTitleC001[$row["VALUE"]] = $row["VALUE"];
        knjCreateHidden($objForm, $model->c001[$row["VALUE"]]."_FLG", 1);
    }
    $result->free();

    $rtnTitleC002 = array();
    $result = $db->query(knjc031eQuery::getDetailDiv());
    $detailCnt = 0;
    $setFieldData = "";
    $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["DETAIL_TITLE_".$row["VALUE"]] = $row["LABEL"];
        if ($model->c002Field[$row["VALUE"]]) {
            $rtnTitleC002[$row["VALUE"]] = $row["VALUE"];
        }
    }
    $result->free();


    //hidden
    knjCreateHidden($objForm, "SET_DETAIL_FIELD", $setFieldData);

    //名称セット
    $query = knjc031eQuery::getC001();
    $result = $db->query($query);
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["TITLE_C001_{$row["NAMECD2"]}"] = $row["NAME1"];
    }
    $result->free();

    return array($rtnTitleC001, $rtnTitleC002);
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $hrName, $monthName)
{
    $tukiAndGakki = explode("-", $model->field["month"]);
    $tuki  = $tukiAndGakki[0];
    $gakki = $tukiAndGakki[1];
    $query = knjc031eQuery::getAppointedDay($tuki, $gakki, $model);
    $appointed_day = $db->getOne($query);

    $schoolMst = array();
    $query = knjc031eQuery::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }

    $query      = knjc031eQuery::selectAttendQuery($model, $schoolMst);
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
        if ($model->a004["2"]) {
            $setArray["OFFDAYS"]    =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseOffdays"] == "true" ? " disabled " : "");
        }
        if ($model->a004["1"]) {
            $setArray["ABROAD"]     =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseAbroad"] == "true" ? " disabled " : "");
        }
        if ($model->c001["1"]) {
            $setArray["ABSENT"]     =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseAbsent"] == "true" ? " disabled " : "");
        }
        if ($model->c001["2"]) {
            $setArray["SUSPEND"]    =  array("SIZE" => 2, "MAXLEN" => 3);
        }
        if ($model->Properties["useKoudome"] == "true" && $model->c001["25"]) {
            $setArray["KOUDOME"]    =  array("SIZE" => 2, "MAXLEN" => 3);
        }
        if ($model->Properties["useVirus"] == "true" && $model->c001["19"]) {
            $setArray["VIRUS"]    =  array("SIZE" => 2, "MAXLEN" => 3);
        }
        $setArray["MOURNING"]   =  array("SIZE" => 2, "MAXLEN" => 3);
        /* C001 */
        foreach ($model->titleValC001 as $key => $val) {
            $setArray[$model->c001[$key]]       =  array("SIZE" => 2, "MAXLEN" => 3);
        }
        $setArray["LATE"]       =  array("SIZE" => 2, "MAXLEN" => 3);
        $setArray["EARLY"]      =  array("SIZE" => 2, "MAXLEN" => 3);
        /* C002 */
        foreach ($model->titleValC002 as $key => $val) {
            if ($key == "101") {
                $setArray[$model->c002[$key]]       =  array("SIZE" => 4, "MAXLEN" => 5);
            } else {
                $setArray[$model->c002[$key]]       =  array("SIZE" => 2, "MAXLEN" => 3);
            }
        }
        $setArray["REMARK"]     =  array("SIZE" => 30, "MAXLEN" => 30);

        $setTextField = "";
        $textSep = "";
        foreach ($setArray as $key => $val) {
            $setTextField .= $textSep.$key."[]";
            $textSep = ",";
        }

        foreach ($setArray as $key => $val) {
            if ($key == "REMARK") {
                $extra = " onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$schCnt})\"; onPaste=\"return showPaste(this, ".$schCnt.");\" ";
                $row[$key] = knjCreateTextBox($objForm, $row[$key], $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
            } else {
                $setStyle = "";
                if ($val["DISABLED"]) {
                    $setStyle = " background-color : #999999 ";
                }

                //入力文字チェック
                $setEntCheck = ($key == "DETAIL_101") ? "NumCheck(this.value)" : "toInteger(this.value)";

                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $value = $row[$key];
                } else {
                    $value = ($row[$key] != 0) ? $row[$key] : "";
                }
                $extra = $val["DISABLED"]." STYLE=\"text-align: right; {$setStyle}\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onblur=\"this.value={$setEntCheck}\"; onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$schCnt})\"; onPaste=\"return showPaste(this, ".$schCnt.");\" ";
                $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
            }
        }
        $schCnt++;

        //異動者（退学・転学・卒業）
        $idou_year = ($monthsem[0] < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
        $idou_day = ($appointed_day == "") ? getFinalDay($db, $monthsem[0], $monthsem[1]) : $appointed_day;
        $idou_date = $idou_year.'-'.$monthsem[0].'-'.$idou_day;
        $idou = $db->getOne(knjc031eQuery::getIdouData($row["SCHREGNO"], $idou_date));

        //異動月取得
        $smonth = $emonth = array();
        $smonth = $db->getCol(knjc031eQuery::getTransferData2($row["SCHREGNO"], "s"));
        $emonth = $db->getCol(knjc031eQuery::getTransferData2($row["SCHREGNO"], "e"));

        //異動者（留学・休学）
        $idou2 = $db->getOne(knjc031eQuery::getTransferData1($row["SCHREGNO"], $idou_date));
        $idou3 = 0;
        if (in_array(sprintf('%02d', $monthsem[0]), $smonth) || in_array(sprintf('%02d', $monthsem[0]), $emonth)) {
            $idou3 = 1;
        }

        //異動期間は背景色を黄色にする
        $row["BGCOLOR_IDOU"] = ($idou > 0 || $idou2 > 0 || $idou3 > 0) ? "bgcolor=yellow" : "";

        //氏名欄に学籍番号表記
        if ($model->Properties["use_SchregNo_hyoji"] == 1) {
            $row["SCHREGNO_SHOW"] = $row["SCHREGNO"] . "　";
        }

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
    //件数
    knjCreateHidden($objForm, "COUNTER", $counter);
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
    $semeday = $db->getRow(knjc031eQuery::selectSemesAll($semester),DB_FETCHMODE_ASSOC);
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
    $securityCnt = $db->getOne(knjc031eQuery::getSecurityHigh());
    $csvSetName = "ＣＳＶ処理";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル処理";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        //ＣＳＶ処理ボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_C031E/knjx_c031eindex.php?SEND_PRGID=KNJC031E&SEND_AUTH={$model->auth}&SEND_hr_class={$model->field["hr_class"]}&SEND_GROUP_HR_CLASS={$model->field["GROUP_HR_CLASS"]}&SEND_month={$model->field["month"]}&selectSchoolKind={$model->selectSchoolKind}','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $csvSetName, $extra);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $name2, $value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    $cnt = 1;
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        if ($name == "GROUP_HR_CLASS") {
            knjCreateHidden($objForm, "LIST_GROUP_HR_CLASS" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name2, $value, $opt, $extra, $size);
}
?>
