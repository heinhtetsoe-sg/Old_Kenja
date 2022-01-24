<?php

require_once('for_php7.php');

class knjs030Form1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjs030index.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["YEAR"] = CTRL_YEAR;

        /* 学期 */
        $arg["SEMESTER"] = $model->control_data["学期名"][CTRL_SEMESTER];

        //表示種別 1:年組 2:先生
        $query = knjs030Query::getTrClass($model);
        $model->trCount = $db->getOne($query);
        $opt = array(1, 2);
        if ($model->field["HR_OR_STAFF"]) {
            $model->field["HR_OR_STAFF"] = $model->field["HR_OR_STAFF"];
        } else if ($model->trCount > 0) {
            $model->field["HR_OR_STAFF"] = "1";
        } else {
            $model->field["HR_OR_STAFF"] = "2";
        }
        $extra = array("id=\"HR_OR_STAFF1\" onClick=\"btn_submit('staffDel')\"", "id=\"HR_OR_STAFF2\" onClick=\"btn_submit('hrDel')\"");
        $radioArray = knjCreateRadio($objForm, "HR_OR_STAFF", $model->field["HR_OR_STAFF"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;

        /* 対象学級 */
        $query = knjs030Query::selectHrClass($model);
        $setDisabled = $model->field["HR_OR_STAFF"] == "1" ? "" : " disabled ";
        $extra = "onChange=\"btn_submit('change_class')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra.$setDisabled, 1, "BLANK");

        /* 対象先生 */
        $query = knjs030Query::selectStaff($model);
        $setDisabled = $model->field["HR_OR_STAFF"] == "2" ? "" : " disabled ";
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["STAFF"], "STAFF", $extra.$setDisabled, 1, "BLANK");

        //表示種別2 1:教科 2:先生またはクラス 3:教科/先生またはクラス
        $arg["DISP_LABEL1"] = "教科";
        if ($model->field["HR_OR_STAFF"] == "1") {
            $arg["DISP_LABEL2"] = "先生";
            $arg["DISP_LABEL3"] = "教科/先生";
        } else {
            $arg["DISP_LABEL2"] = "クラス";
            $arg["DISP_LABEL3"] = "教科/クラス";
        }
        $opt = array(1, 2, 3);
        $extra = array("id=\"DISP_SHOW1\" onClick=\"btn_submit('main')\"", "id=\"DISP_SHOW2\" onClick=\"btn_submit('main')\"", "id=\"DISP_SHOW3\" onClick=\"btn_submit('main')\"");
        $model->field["DISP_SHOW"] = $model->field["DISP_SHOW"] ? $model->field["DISP_SHOW"] : "1";
        $radioArray = knjCreateRadio($objForm, "DISP_SHOW", $model->field["DISP_SHOW"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;

        //時数種別 1:時数集計教科名 2:時間割上教科名
        $opt = array(1, 2);
        $extra = array("id=\"JISU_SHOW1\" onClick=\"btn_submit('main')\"", "id=\"JISU_SHOW2\" onClick=\"btn_submit('main')\"");
        $model->field["JISU_SHOW"] = $model->field["JISU_SHOW"] ? $model->field["JISU_SHOW"] : "1";
        $radioArray = knjCreateRadio($objForm, "JISU_SHOW", $model->field["JISU_SHOW"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;

        /* 月 */
        $query = knjs030Query::getMonth($model);
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["MONTH"], "MONTH", $extra, 1, "BLANK");

        //週 1:1週 ～ 6:6週
        $opt = array();
        $unWeek5 = true;
        $unWeek6 = true;
        for ($weekCnt = 1; $weekCnt <= get_count($model->weekArray[0]["DAY"]); $weekCnt++) {
            $opt[] = $weekCnt;
            $arg["WEEK".$weekCnt] = "1";
            $unWeek5 = $weekCnt == 5 ? false : $unWeek5;
            $unWeek6 = $weekCnt == 6 ? false : $unWeek6;
        }
        $arg["UNWEEK6"] = $unWeek5;
        $arg["UNWEEK6"] = $unWeek6;
        $extra = array("id=\"WEEK_SHOW1\" onClick=\"btn_submit('main')\"",
                       "id=\"WEEK_SHOW2\" onClick=\"btn_submit('main')\"",
                       "id=\"WEEK_SHOW3\" onClick=\"btn_submit('main')\"",
                       "id=\"WEEK_SHOW4\" onClick=\"btn_submit('main')\"",
                       "id=\"WEEK_SHOW5\" onClick=\"btn_submit('main')\"",
                       "id=\"WEEK_SHOW6\" onClick=\"btn_submit('main')\"");
        $model->field["WEEK_SHOW"] = $model->field["WEEK_SHOW"] ? $model->field["WEEK_SHOW"] : getWeekShowVal($model);
        $model->field["WEEK_SHOW"] = $model->field["WEEK_SHOW"] > get_count($opt) ? "1" : $model->field["WEEK_SHOW"];
        $radioArray = knjCreateRadio($objForm, "WEEK_SHOW", $model->field["WEEK_SHOW"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;

        //表示単元 1:最下位単元 2:大単元
        $opt = array(1, 2);
        $extra = array("id=\"UNIT_SHOW1\" onClick=\"btn_submit('main')\"", "id=\"UNIT_SHOW2\" onClick=\"btn_submit('main')\"");
        $model->field["UNIT_SHOW"] = $model->field["UNIT_SHOW"] ? $model->field["UNIT_SHOW"] : "1";
        $radioArray = knjCreateRadio($objForm, "UNIT_SHOW", $model->field["UNIT_SHOW"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;

        //表示備考 1:メモ 2:授業内容 3:目標
        $opt = array(1, 2, 3);
        $extra = array("id=\"REMARK_SHOW1\" onClick=\"btn_submit('main')\"", "id=\"REMARK_SHOW2\" onClick=\"btn_submit('main')\"", "id=\"REMARK_SHOW3\" onClick=\"btn_submit('main')\"");
        $model->field["REMARK_SHOW"] = $model->field["REMARK_SHOW"] ? $model->field["REMARK_SHOW"] : "1";
        $radioArray = knjCreateRadio($objForm, "REMARK_SHOW", $model->field["REMARK_SHOW"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;

        //日付指定モード
        $extra = "onClick=\"btn_submit('main')\"";
        $extra .= $model->field["testMode"] == "1" ? " checked " : "";
        $arg["testMode"] = knjCreateCheckBox($objForm, "testMode", "1", $extra);

        //日付指定
        $extra = "btn_submit('main')";
        $model->field["testday"] = $model->field["testday"] ? $model->field["testday"] : strtr(CTRL_DATE, "-", "/");;
        $arg["testday"] = View::popUpCalendar2($objForm, "testday", $model->field["testday"], "reload=true", $extra);

        /* タイトル設定 */
        $model->updDateArray = array();
        if ($model->testCalcMode) {
            setTitleData2($arg, $db, $model);
        } else {
            setTitleData($arg, $db, $model);
        }

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model);

        /* ボタン作成 */
        makeButton($objForm, $arg, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "changeVal", $model->changeVal);

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjs030Form1.html", $arg);
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
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function getWeekShowVal($model) {
    $setWeek = 1;
    foreach ($model->weekArray as $key => $val) {
        foreach ($val["DAY"] as $Dkey => $Dval) {
            if ($Dval == strtr(CTRL_DATE, "/", "-")) {
                $setWeek = $Dkey;
                break;
            }
        }
    }
    return $setWeek;
}

//タイトル設定
function setTitleData(&$arg, $db, &$model)
{
    $setCnt = 0;
    foreach ($model->weekArray as $key => $val) {
        $arg["TITLE"]["DAY".$key] = $val["WAREKI"][$model->field["WEEK_SHOW"]]."(".$val["WEEK"].")";
        $query = knjs030Query::getThisSeme($val["DAY"][$model->field["WEEK_SHOW"]]);
        $setSeme = $db->getOne($query);
        $model->updDateArray[$setCnt]["DAY"] = $val["DAY"][$model->field["WEEK_SHOW"]];
        $model->updDateArray[$setCnt]["SEME"] = $setSeme;
        $setCnt++;
    }
}

//タイトル設定
function setTitleData2(&$arg, $db, &$model)
{
    $setWeek = 1;
    foreach ($model->weekArray as $key => $val) {
        foreach ($val["DAY"] as $Dkey => $Dval) {
            if ($Dval == strtr($model->field["testday"], "/", "-")) {
                $setWeek = $Dkey;
                break;
            }
        }
    }
    $setCnt = 0;
    foreach ($model->weekArray as $key => $val) {
        $arg["TITLE"]["DAY".$key] = $val["WAREKI"][$setWeek]."(".$val["WEEK"].")";
        $query = knjs030Query::getThisSeme($val["DAY"][$setWeek]);
        $setSeme = $db->getOne($query);
        $model->updDateArray[$setCnt]["DAY"] = $val["DAY"][$setWeek];
        $model->updDateArray[$setCnt]["SEME"] = $setSeme;
        $setCnt++;
    }
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, &$model) {

    //タイトル行事予定は固定
    $period = array();
    $meisaiCnt = 0;
    $period[$meisaiCnt]["PERIOD_NAME"] = "行事予定";
    $meisaiCnt++;

    //タイトル校時
    $model->period = array();
    $query = knjs030Query::getPeriod();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $period[$meisaiCnt] = $row;
        $model->period[$row["VALUE"]] = $row["PERIOD_NAME"];
        $meisaiCnt++;
    }
    $result->free();

    /***************************/
    // データセット用配列
    // n行目(n校時)に1週間分のデータ(MEISAI1～7)をセットする。0行目は固定で行事とする。
    // $setData：key(行) => val["MEISAI1"] = 月曜のデータ
    //                      val["MEISAI2"] = 火曜のデータ
    //                      ～
    //                      val["MEISAI7"] = 日曜のデータ
    //
    // $setData[0]["MEISAI1"] = 0行目(行事予定)の列1(列1に表示するデータ)
    // $setData[0]["MEISAI2"] = 0行目(行事予定)の列2(列2に表示するデータ)
    // $setData[0]["PERIOD_HEIGHT"] = 0行目の高さ
    /***************************/
    $setData = array();

    //タイトルをセット
    foreach ($period as $key => $val) {
        $setData[$key] = $val;
        $setData[$key]["PERIOD_HEIGHT"] = 93;
    }

    //行事データセット
    $meisaiCnt = 1;
    $holiDay = array();
    foreach ($model->updDateArray as $key => $val) {
        $query = knjs030Query::getEventQuery($val["DAY"], $model);
        $event = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $holiDay[$val["DAY"]]["HOLIDAY_FLG"] = $event["HOLIDAY_FLG"];

        $extra  = $event["HOLIDAY_FLG"] == "1" ? " checked " : "";
        $extra .= $model->field["HR_OR_STAFF"] == "1" ? "" : " disabled ";
        $extra .= "onClick=\"dropUnitDate(this, '".$val["DAY"]."')\"";
        $arg["TITLE"]["HOLIDAY".$key] = knjCreateCheckBox($objForm, "HOLIDAY_FLG_".$val["DAY"], "1", $extra);

        if ($model->field["HR_OR_STAFF"] == "1") {
            //行事テキスト
            $extra = "";
            $setText = knjCreateTextBox($objForm, $event["REMARK1"], "EVENT_".$val["DAY"], 20, 20, $extra);

            $setData[0]["MEISAI".$meisaiCnt] = $setText;
        } else {
            $setData[0]["MEISAI".$meisaiCnt] = $event["REMARK1"];
        }
        $setData[0]["PERIOD_HEIGHT"] = 30;
        $meisaiCnt++;
    }

    //講座コンボ
    $optSubclassAll = array();
    $optSubclass = array();
    $optSubclass["1"][] = array('label' => "",
                                'value' => "");
    $optSubclass["2"][] = array('label' => "",
                                'value' => "");
    $optSubclass["3"][] = array('label' => "",
                                'value' => "");
    $query = knjs030Query::getChair($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setLabel = $model->field["JISU_SHOW"] == "1" ? $row["SUBCLASSNAME"] : $row["CLASSABBV"];
        if ($model->field["HR_OR_STAFF"] == "1") {
            if ($model->field["DISP_SHOW"] == "2") {
                $setLabel = $row["STAFFNAME"];
            } else if ($model->field["DISP_SHOW"] == "3") {
                $setLabel = $setLabel."/".$row["STAFFNAME"];
            }
        }
        $setLabel = strlen($setLabel) > 27 ? substr($setLabel, 0, 24) : $setLabel;
        $optSubclass[$row["SEMESTER"]][] = array('label' => $setLabel,
                                                 'value' => $row["VALUE"]);
        //教育課程対応
        $classcd = $row["CLASSCD"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"];
        }
        $setAllCd = $model->field["JISU_SHOW"] == "1" ? $row["VALUE"] : $classcd;
        $setAllName = $model->field["JISU_SHOW"] == "1" ? $row["SUBCLASSABBV"] : $row["CLASSABBV"];
        $optSubclassAll[$setAllCd] = $setAllName;
    }

    if ($model->field["HR_OR_STAFF"] == "1") {
        $query = knjs030Query::getHrUnitDat($model);
    } else {
        $query = knjs030Query::getStaffUnitDat($model);
    }
    $result = $db->query($query);
    //DB読み込み時のデータを保持
    $model->delChair = array();
    //DB登録のあるデータ
    $existData = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        list($getYear, $getMonth, $getDay) = preg_split("/-/", $row["EXECUTEDATE"]);
        $setWeek = date("w", mktime( 0, 0, 0, $getMonth, $getDay, $getYear));
        $setWeek = $setWeek >= 1 ? $setWeek : 7;
        $setName = $row["EXECUTEDATE"]."_".$row["PERIODCD"];
        if ($model->field["HR_OR_STAFF"] == "1") {

            $extra = "onChange=\"dropUnit('".$setName."');\"";
            $setSubClass = knjCreateCombo($objForm, "CHAIR_".$setName, $row["CHAIRCD"], $optSubclass[$row["SEMESTER"]], $extra, 1);

            $bunCnt = $db->getOne(knjs030Query::getBunkatu($model, $row["EXECUTEDATE"], $row["PERIODCD"], $row["CHAIRCD"], "COUNT"));
            $bungatuLinkColor = $bunCnt > 0 ? "class=\"LinkColorRed\"" : "";
            $subdata = "loadwindow('knjs030index.php?cmd=bunkatu&BUNKATU_CHAIRCD={$row["CHAIRCD"]}&BUNKATU_DATE={$row["EXECUTEDATE"]}&BUNKATU_PERIOD={$row["PERIODCD"]}&BUNKATU_SEME={$row["SEMESTER"]}',0,0,600,450)";
            $linkData = View::alink("#", htmlspecialchars("割"),"{$bungatuLinkColor} onclick=\"$subdata\"");
            $setUnit = "<span id=\"BUNKATU_".$setName."\"><font size=1>".$linkData."</font></span>";

            //リンク設定
            $row["TITLE"] = View::alink("#", htmlspecialchars($row["TITLE"]),"onclick=\"$subdata\"");

            $setUnit .= " ";

            //教育課程対応
            $subcd = $row["SUBCLASSCD"];
            if ($model->Properties["useCurriculumcd"] == '1') {
                $subcd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            }
            $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);";
            $subdata = "wopen('".REQUESTROOT."/S/KNJS010/knjs010index.php?&cmd=main&SEND_PRGID=KNJS030&SEND_GRADE_HR={$model->field["HR_CLASS"]}&SEND_SUBCLASSCD={$subcd}&SEND_DATADIV=2&SEND_SEQ={$row["RANK"]}&SEND_AUTH=".AUTHORITY.$extra;
            $linkData = View::alink("#", htmlspecialchars($row["UNIT_NAME"]),"onclick=\"$subdata\"");
            $setUnit .= "<span id=\"UNIT_".$setName."\">".$linkData."</span>";

            $extra = "onChange=\"setChangeColor('".$setName."', 'text', '#ccffcc')\"";
            $setRemark = knjCreateTextBox($objForm, $row["REMARK"], "REMARK_".$setName, 20, 60, $extra);
        } else {
            $setLabel = $model->field["JISU_SHOW"] == "1" ? $row["SUBCLASSNAME"] : $row["CLASSABBV"];
            if ($model->field["DISP_SHOW"] == "2") {
                $setLabel = $row["HR_NAME"];
            } else if ($model->field["DISP_SHOW"] == "3") {
                $setLabel = $setLabel."/".$row["HR_NAME"];
            }
            $setLabel = strlen($setLabel) > 27 ? substr($setLabel, 0, 24) : $setLabel;

            $setSubClass = $setLabel;

            $setUnit = "<span id=\"BUNKATU_".$setName."\"><font size=1>　</font></span>";
            $setUnit .= " ";

            //教育課程対応
            $subcd = $row["SUBCLASSCD"];
            if ($model->Properties["useCurriculumcd"] == '1') {
                $subcd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            }
            $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);";
            $subdata = "wopen('".REQUESTROOT."/S/KNJS010/knjs010index.php?&cmd=main&SEND_PRGID=KNJS030&SEND_GRADE_HR={$model->field["HR_CLASS"]}&SEND_SUBCLASSCD={$subcd}&SEND_DATADIV=2&SEND_SEQ={$row["RANK"]}&SEND_AUTH=".AUTHORITY.$extra;
            $linkData = View::alink("#", htmlspecialchars($row["UNIT_NAME"]),"onclick=\"$subdata\"");
            $setUnit .= "<span id=\"UNIT_".$setName."\">".$linkData."</span>";

            $setRemark = $row["REMARK"];
        }
        $setData[$row["PERIODCD"]]["MEISAI".$setWeek] = $setSubClass."<hr>".$setUnit."<hr>".$setRemark;
        $setData[$row["PERIODCD"]]["MEISAI_ID".$setWeek] = "ID_".$setName;
        $existData[$setName] = "1";
        $model->delChair[$setName] = $row["CHAIRCD"];
    }
    $result->free();

    foreach ($model->updDateArray as $dateKey => $dateVal) {
        foreach ($model->period as $pKey => $pVal) {
            $setName = $dateVal["DAY"]."_".$pKey;
            if (array_key_exists($dateVal["DAY"]."_".$pKey, $existData)) {
                continue;
            }
            list($getYear, $getMonth, $getDay) = preg_split("/-/", $dateVal["DAY"]);
            $setWeek = date("w", mktime( 0, 0, 0, $getMonth, $getDay, $getYear));
            $setWeek = $setWeek >= 1 ? $setWeek : 7;
            if ($model->field["HR_OR_STAFF"] == "1") {

                $extra = "onChange=\"dropUnit('".$setName."');\"";
                $setSubClass = knjCreateCombo($objForm, "CHAIR_".$setName, "", $optSubclass[$dateVal["SEME"]], $extra, 1);

                $setUnit = "<span id=\"BUNKATU_".$setName."\"><font size=1>　</font></span>";
                $setUnit .= " ";

                $linkData = "　";
                $setUnit .= "<span id=\"UNIT_".$setName."\">".$linkData."</span>";

                $extra = "onChange=\"setChangeColor('".$setName."', 'text', '#ccffcc')\"";
                $setRemark = knjCreateTextBox($objForm, "", "REMARK_".$setName, 20, 60, $extra);
            } else {
                $setSubClass = "　";
                $setUnit = "　";
                $setRemark = "　";
            }
            $setData[$pKey]["MEISAI".$setWeek] = $setSubClass."<hr>".$setUnit."<hr>".$setRemark;
            $setData[$pKey]["MEISAI_ID".$setWeek] = "ID_".$setName;
            $setData[$pKey]["BGCOLOR_HOLIDAY".$setWeek] = $holiDay[$dateVal["DAY"]]["HOLIDAY_FLG"] == "1" ? "bgcolor=\"#cccccc\"" : "";
        }
    }

    $arg["period1"] = $setData;
    $arg["period_data"] = $setData;

    ksort($optSubclassAll);
    $totalSoeji = 0;
    foreach ($optSubclassAll as $chairCd => $chairAbbv) {
        $arg["total_name"]["SUBCLASS".($totalSoeji + 1)] = $chairAbbv;
        //週
        if ($model->field["HR_OR_STAFF"] == "1") {
            $query = knjs030Query::getHrUnitDat($model, $chairCd, "TOTAL_WEEK");
        } else {
            $query = knjs030Query::getStaffUnitDat($model, $chairCd, "TOTAL_WEEK");
        }
        $setWeekCnt = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $setWeekShow = $setWeekCnt["BUNSI"] > 0 ? $setWeekCnt["SYOU"]." ".$setWeekCnt["BUNSI"]."/9" : $setWeekCnt["SYOU"];
        $arg["total_jisu"]["SUBCLASS".($totalSoeji + 1)] = $setWeekShow;
        //年間
        if ($model->field["HR_OR_STAFF"] == "1") {
            $query = knjs030Query::getHrUnitDat($model, $chairCd, "TOTAL_YEAR");
        } else {
            $query = knjs030Query::getStaffUnitDat($model, $chairCd, "TOTAL_YEAR");
        }
        $setYearCnt = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $setYearShow = $setYearCnt["BUNSI"] > 0 ? $setYearCnt["SYOU"]." ".$setYearCnt["BUNSI"]."/9" : $setYearCnt["SYOU"];
        $arg["total_nenkan"]["SUBCLASS".($totalSoeji + 1)] = $setYearShow;
        //年間標準
        if ($model->field["HR_OR_STAFF"] == "1") {
            $query = knjs030Query::getTotalJisu($model, $chairCd);
            $setHyoujunCnt = $db->getOne($query);
            $arg["total_hyoujun"]["SUBCLASS".($totalSoeji + 1)] = $setHyoujunCnt;
            //残時数
            $yearMinute = ($setYearCnt["SYOU"] * 45) + ($setYearCnt["BUNSI"] * 5);
            $setTotalZan = ($setHyoujunCnt * 45) - ($yearMinute);
            $isMinus = $setTotalZan < 0 ? true : false;
            $setTotalZan = $isMinus ? $setTotalZan * -1 : $setTotalZan;
            $setTotalBunsi = ($setTotalZan % 45) / 5;
            $setTotalShou = ceil($setTotalZan / 45);
            $setTotalShou = $setTotalBunsi > 0 ? $setTotalShou - 1 : $setTotalShou;
            $setTotalShow = $setTotalBunsi > 0 ? $setTotalShou." ".$setTotalBunsi."/9" : $setTotalShou;
            $arg["total_zan"]["SUBCLASS".($totalSoeji + 1)] = $setTotalShow;
            $arg["total_zan"]["SUB_COLOR".($totalSoeji + 1)] = $isMinus ? "white" : "red";
            $arg["total_zan"]["SUB_BGCOLOR".($totalSoeji + 1)] = $isMinus ? "" : "bgColor=\"white\"";
        }

        $totalSoeji++;
    }

}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    if ($model->field["HR_OR_STAFF"] == "1") {
        //保存ボタン
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"");
        //取消ボタン
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    }
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}
?>
