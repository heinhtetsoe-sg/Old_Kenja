<?php

require_once('for_php7.php');
class knjc031fForm1 {

    function main(&$model) {

        //フォーム作成
        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjc031findex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //年度
        $arg["year"] = CTRL_YEAR;

        //学期
        $arg["semester"] = CTRL_SEMESTERNAME;

        $arg["fep"] = $model->Properties["FEP"];

        //上段表示
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 || $model->Properties["use_school_detail_gcm_dat"] == "1") $arg["upperline"] = 1;

        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //課程学科コンボ
            $query = knjc031fQuery::getCourseMajor($model);
            $extra = "onChange=\"btn_submit('change_course')\";";
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1);
        }

        //学級コンボ内容ラジオボタン 1:HRクラス 2:複式クラス
        $opt = array(1, 2);
        $model->field["SELECT_CLASS_TYPE"] = ($model->field["SELECT_CLASS_TYPE"] == "") ? "1" : $model->field["SELECT_CLASS_TYPE"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, "onChange=\"current_cursor('SELECT_CLASS_TYPE{$val}');btn_submit('change_radio')\"; id=\"SELECT_CLASS_TYPE{$val}\"");
            knjCreateHidden($objForm, "LIST_SELECT_CLASS_TYPE" . $val, $key);
        }
        $radioArray = knjCreateRadio($objForm, "SELECT_CLASS_TYPE", $model->field["SELECT_CLASS_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //学級コンボ内容ラジオボタン表示
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1) $arg["class_type"] = 1;

        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            //複式学級コンボボックス
            $query = knjc031fQuery::getGroupHrClass($model);
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $extra = "aria-label = \"学級\" id=\"GROUP_HR_CLASS\" onChange=\"current_cursor('GROUP_HR_CLASS');btn_submit('change_group')\";";
            /* Edit by HPA for PC-talker 読み end 2020/02/20 */
            makeCmb($objForm, $arg, $db, $query, "GROUP_HR_CLASS", "GROUP_HR_CLASS", $model->field["GROUP_HR_CLASS"], $extra, 1, "BLANK");
        } else {
            //対象学級
            makeHrclassCmb($objForm, $arg, $db, $model);
        }

        //対象月コンボ作成
        makeMonthSemeCmb($objForm, $arg, $db, $model);

        //締め日
        $appointed_day = makeAppointedDay($objForm, $arg, $db, $model);

        //授業日数セット
        $btn_lesson_clear = makeLessonSet($objForm, $arg, $db, $model);

        //タイトル設定
        setTitleData($objForm, $arg, $db, $model, $btn_lesson_clear);

        //クラス備考
        if ($model->Properties["useAttendSemesHrRemark"] == "1") {
            if (!isset($model->warning)) {
                $query = knjc031fQuery::getHrRemark($model);
                $model->field["HR_REMARK"] = $db->getOne($query);
            }
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $extra = "id=\"HR_REMARK\" aria-label = \"クラス出欠の備考\"";
            /* Edit by HPA for PC-talker 読み end 2020/02/20 */
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

        if ($model->field["SELECT_CLASS_TYPE"] == 1) {
            //出欠完了データ
            if (isset($model->warning)) {
                //エラーの場合
                $executed = $model->field["EXECUTED"];
            } else {
                //取得
                $executed = $db->getOne(knjc031fQuery::getExecuted($model));
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
        }

        //コピー貼付用
        $copyHidden = $sep = "";
        $useVirus = $useKoudome = "";
        foreach ($model->item_array as $key => $val) {
            if (in_array($val["item"], array("ATTEND", "KESSEKI", "PRESENT"))) continue;
            $copyHidden .= $sep.$val["item"]."[]";
            $sep = ":";

            if ($val["item"] == "VIRUS") $useVirus = "true";
            if ($val["item"] == "KOUDOME") $useKoudome = "true";
        }
        $copyHidden .= $sep."REMARK[]";
        knjCreateHidden($objForm, "copyField", $copyHidden);

        //編集対象データリスト
        makeDataList($objForm, $arg, $db, $model, $objUp, $appointed_day);

        if (!$model->Properties["use_Attend_zero_hyoji"]) {
            $arg["ZERO_HYOJI_COMMENT"] = "※'0'データは、空白で表示します。";
        }

        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //データベース接続切断
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", PROGRAMID);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "unUseOffdays", $model->Properties["unUseOffdays"]);
        knjCreateHidden($objForm, "unUseAbroad", $model->Properties["unUseAbroad"]);
        knjCreateHidden($objForm, "unUseAbsent", $model->Properties["unUseAbsent"]);
        knjCreateHidden($objForm, "changeVal", 0);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
        knjCreateHidden($objForm, "use_Attend_zero_hyoji", $model->Properties["use_Attend_zero_hyoji"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        //コンボ変更時、MSG108表示用
        knjCreateHidden($objForm, "SELECT_COURSE", $model->field["COURSE_MAJOR"]);
        knjCreateHidden($objForm, "SELECT_SELECT_CLASS_TYPE", $model->field["SELECT_CLASS_TYPE"]);
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            knjCreateHidden($objForm, "SELECT_GROUP_HR_CLASS", $model->field["GROUP_HR_CLASS"]);
        }
        knjCreateHidden($objForm, "SELECT_HR_CLASS", $model->field["hr_class"]);
        knjCreateHidden($objForm, "SELECT_MONTH", $model->field["month"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);

        knjCreateHidden($objForm, "HIDDEN_COURSE_MAJOR");
        knjCreateHidden($objForm, "HIDDEN_SELECT_CLASS_TYPE");
        knjCreateHidden($objForm, "HIDDEN_GROUP_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_MONTH");
        knjCreateHidden($objForm, "HIDDEN_LESSON_SET");
        knjCreateHidden($objForm, "HIDDEN_MOVE_ENTER");
        knjCreateHidden($objForm, "HIDDEN_EXECUTED");

        $arg["finish"] = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML5($model, "knjc031fForm1.html", $arg);
    }
}

//対象学級コンボ作成
function makeHrclassCmb(&$objForm, &$arg, $db, &$model) {
    $opt_hr = array();
    $opt_hr[] = array("label" => "", "value" => "");
    $cnt = 1;
    $query = knjc031fQuery::selectHrClass($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_hr[] = array("label" => $row["LABEL"],
                          "value" => $row["VALUE"]);

        //初期データセット (起動時に先頭のクラスを対象学級とする)
        if ($model->field["hr_class"] == "" || $model->field["hr_class"] == NULL) {
            $model->field["hr_class"] = "";
            $model->field["grade"]    = "";
            $model->field["class"]    = "";
        }
        knjCreateHidden($objForm, "LIST_HR_CLASS" . $row["VALUE"], $cnt);
        $cnt++;
    }
    /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
    $arg["hr_class"] = knjCreateCombo($objForm, "HR_CLASS", $model->field["hr_class"], $opt_hr, " id = \"hr_class\" aria-label = \"学級\" onChange=\"current_cursor('hr_class');btn_submit('change_hrclass')\";", 1);
    /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
    return;
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model) {
    //学期・月範囲取得
    $query = knjc031fQuery::selectSemesAll();
    $result = $db->query($query);
    $data = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    //学期・月データ取得
    $opt_month = setMonth($db, $data, $objForm, $model);

    //学期・月の初期値をセット
    if ($model->field["month"] == "" || $model->field["month"] == NULL) {
        $model->field["month"] = getDefaultMonthCd($opt_month);
    }

    //対象月コンボ
    /* Edit by HPA for PC-talker 読み start 2020/02/03 */
    $extra = "aria-label = \"対象月\" id=\"month\" onChange=\"current_cursor('month');btn_submit('change_month')\";";
    /* Edit by HPA for PC-talker 読み end 2020/02/20 */
    $arg["month"] = knjCreateCombo($objForm, "MONTH", $model->field["month"], $opt_month, $extra, 1);
}

//学期・月データ取得
function setMonth($db, $data, &$objForm, $model) {
    $cnt = 0;
    $opt_month = array();
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query = knjc031fQuery::selectMonthQuery($month, $model);
            $getdata = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
                knjCreateHidden($objForm, "LIST_MONTH" . $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"], $cnt);
                $cnt++;
            }
        }
    }
    return $opt_month;
}

//学期・月の初期値を取得
function getDefaultMonthCd($opt_month) {
    $rtnMonthCd = $opt_month[0]["value"];
    $setFlg = true;
    for ($dcnt = 0; $dcnt < get_count($opt_month); $dcnt++) {
        $monthCd = preg_split("/-/", $opt_month[$dcnt]["value"]);
        $month    = (int) $monthCd[0];
        $semester = $monthCd[1];
        if ($month < 4) {
            $month += 12;
        }

        $ymd = preg_split("/-/", CTRL_DATE);
        $mm  = (int) $ymd[1];
        if ($mm < 4) {
            $mm += 12;
        }

        if ($semester == CTRL_SEMESTER && $setFlg) {
            $rtnMonthCd = $opt_month[$dcnt]["value"];
            if ($month >= $mm) {
                $setFlg = false;
            }
        }
    }
    return $rtnMonthCd;
}

//締め日
function makeAppointedDay(&$objForm, &$arg, $db, &$model) {
    //締め日
    $query = knjc031fQuery::getAppointedDay($model);
    $appointed_day = $db->getOne($query);
    $arg["SET_APPOINTED_DAY"] = $appointed_day;
    knjCreateHidden($objForm, "SET_APPOINTED_DAY", $appointed_day);

    return $appointed_day;
}

//授業日数セット
function makeLessonSet(&$objForm, &$arg, $db, &$model) {
    if (in_array($model->cmd, array("change_radio", "change_group", "change_hrclass", "change_month", "reset"))) {
        $model->field["LESSON_SET"] = "";
    }

    //テキスト内の背景色
    $bgcolor = "white";

    //ATTEND_SEMES_DATのMAX授業日数セット
    $query = knjc031fQuery::getMaxLesson1($model);
    $attend_semes = $db->getOne($query);
    if ($model->field["LESSON_SET"] == "") {
        $model->field["LESSON_SET"] =  $attend_semes;
    }

    //ATTEND_LESSON_MSTのMAX授業日数セット
    $query = knjc031fQuery::getMaxLesson2($model);
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
    /* Edit by HPA for PC-talker 読み start 2020/02/03 */
    $extra = " id = \"remark\"aria-label = \"授業日数\" style=\"text-align: right; background-color: {$bgcolor};\" onChange=\"this.style.background='#ccffcc';\" onblur=\"tmpSet(this, 'remark');this.value=toInteger(this.value);\"";
    /* Edit by HPA for PC-talker 読み end 2020/02/20 */
    $arg["LESSON_SET"] = knjCreateTextBox($objForm, $model->field["LESSON_SET"], "LESSON_SET", 3, 3, $extra);

    //授業時日数の表示項目有無チェック
    $useLesson = false;
    foreach ($model->item_array as $key => $val) {
        if ($val["item"] == "LESSON" && $val["input"] == "1") {
            $useLesson = true;
        }
    }
    knjCreateHidden($objForm, "USE_LESSON", $useLesson);

    //反映ボタン
    if ((($class_flg == 1 && $model->field["GROUP_HR_CLASS"]) || ($class_flg == 2 && $model->field["hr_class"])) && $model->field["month"] && $useLesson) {
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $extra = "aria-label = \"反映\" onclick=\"reflect();\"";
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
    } else {
        $extra = "disabled";
    }
    $arg["btn_reflect"] = knjCreateBtn($objForm, "btn_reflect", "反 映", $extra);

    //クリアボタン
    if ((($class_flg == 1 && $model->field["GROUP_HR_CLASS"]) || ($class_flg == 2 && $model->field["hr_class"])) && $model->field["month"] && $useLesson) {
        $extra = "onclick=\"lesson_clear();\"";
    } else {
        $extra = "disabled";
    }
    /* Edit by HPA for PC-talker 読み start 2020/02/03 */
    $extra .= " aria-label = \"授業日数のクリアボタン\" style=\"padding: 1px 5px;font-size: 80%;\"";
    /* Edit by HPA for PC-talker 読み end 2020/02/20 */
    $btn_lesson_clear = knjCreateBtn($objForm, "btn_lesson_clear", "ｸﾘｱ", $extra);

    return $btn_lesson_clear;
}

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, $model, $btn_lesson_clear) {
    //使用する項目セット（累積以外）
    $setTmp = "";
    $allWidth = 300;
    $other_cnt = 3;
    $width = "45";
    foreach ($model->item_array as $key => $val) {
        $label = "";
        for ($i = 0; $i < mb_strlen($val["label"], "UTF-8"); $i++) {
            if ($i % 2 == 0) {
                if ($i != 0) $label .= "<br>";
                $label .= mb_substr($val["label"], $i, 2,"UTF-8");
            }
        }
        $setTmp .= "<td width=\"".$width."\">".$label;
        if ($val["item"] == "LESSON") $setTmp .= "<br>".$btn_lesson_clear;
        $setTmp .= "</td>";
        $allWidth += 52;
        $other_cnt++;
    }
    $arg["TITLE"] = $setTmp;
    $arg["useOtherThanRuikeiCol"] = $other_cnt;

    //使用する項目セット（累積）
    $setRuisekiTmp = "";
    $ruiseki_cnt = 0;
    foreach ($model->itemR_array as $key => $val) {
        $label = "";
        for ($i = 0; $i < mb_strlen($val["label"], "UTF-8"); $i++) {
            if ($i % 2 == 0) {
                if ($i != 0) $label .= "<br>";
                $label .= mb_substr($val["label"], $i, 2,"UTF-8");
            }
        }
        $setRuisekiTmp .= "<td width=\"".$width."\">".$label."</td>";
        $allWidth += 52;
        $ruiseki_cnt++;
    }
    $arg["RUISEKI_TITLE"] = $setRuisekiTmp;
    if ($ruiseki_cnt > 0) {
        $arg["useRuikeiCol"] = $ruiseki_cnt;
    }

    //項目名セット（備考）
    $arg["REMARK_TITLE"] = "<td width=\"#\">出欠の備考</td>";
    $allWidth += 250;

    //全体のサイズ
    $arg["useAllWidth"] = $allWidth;

    return;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $appointed_day) {
    $schoolMst = array();
    $query = knjc031fQuery::getSchoolMst($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }
    if ($schoolMst["SEM_OFFDAYS"] == "1") {
        $model->kessekiItem[] = "OFFDAYS";
    }

    $monthsem = array();
    $monthsem = preg_split("/-/", $model->field["month"]);

    $idou_date = $sdate = "";
    if ($model->field["month"]) {
        //異動対象日付
        $idou_month = sprintf('%02d', $monthsem[0]);
        $idou_year = ($idou_month < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
        $idou_day = ($appointed_day == "") ? getFinalDay($db, $idou_month, $monthsem[1]) : $appointed_day;
        $idou_date = $idou_year.'-'.$idou_month.'-'.sprintf('%02d', $idou_day);

        //対象月の開始日
        $semeday = $db->getRow(knjc031fQuery::selectSemesAll($monthsem[1]), DB_FETCHMODE_ASSOC);
        if (sprintf('%02d', $semeday["S_MONTH"]) == $idou_month) {
            $sdate = $idou_year.'-'.$idou_month.'-'.sprintf('%02d', $semeday["S_DAY"]);
        } else {
            $sdate = $idou_year.'-'.$idou_month.'-01';
        }
    }

    $counter = $textLineCnt = $schCnt = 0;
    $colorFlg = false;
    $data = $useline =array();
    $query = knjc031fQuery::selectAttendQuery($model, $schoolMst);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //エラーのとき、編集データをセット
        if (isset($model->warning)) {
            foreach($model->field["SCHREGNO"] as $key => $val) {
                if ($row["SCHREGNO"] == $val) {
                    $row["LESSON"]      = $model->field["LESSON"][$key];
                    $row["OFFDAYS"]     = $model->field["OFFDAYS"][$key];
                    $row["ABROAD"]      = $model->field["ABROAD"][$key];
                    $row["ABSENT"]      = $model->field["ABSENT"][$key];
                    $row["SUSPEND"]     = $model->field["SUSPEND"][$key];
                    $row["KOUDOME"]     = $model->field["KOUDOME"][$key];
                    $row["VIRUS"]       = $model->field["VIRUS"][$key];
                    $row["MOURNING"]    = $model->field["MOURNING"][$key];
                    $row["SICK"]        = $model->field["SICK"][$key];
                    $row["NOTICE"]      = $model->field["NOTICE"][$key];
                    $row["NONOTICE"]    = $model->field["NONOTICE"][$key];
                    $row["LATE"]        = $model->field["LATE"][$key];
                    $row["EARLY"]       = $model->field["EARLY"][$key];
                    $row["DETAIL_001"]  = $model->field["DETAIL_001"][$key];
                    $row["DETAIL_002"]  = $model->field["DETAIL_002"][$key];
                    $row["DETAIL_003"]  = $model->field["DETAIL_003"][$key];
                    $row["DETAIL_004"]  = $model->field["DETAIL_004"][$key];
                    $row["DETAIL_101"]  = $model->field["DETAIL_101"][$key];
                    $row["DETAIL_102"]  = $model->field["DETAIL_102"][$key];
                    $row["REMARK"]      = $model->field["REMARK"][$key];
                }
            }
        }

        $idou = $idouText = 0;
        if ($model->field["month"]) {
            //異動者（退学・転学・卒業）
            $idouData1 = $db->getRow(knjc031fQuery::getIdouData($row["SCHREGNO"], $idou_date, $sdate), DB_FETCHMODE_ASSOC);
            $idou += $idouData1["IDOU_COLOR"];
            if ($model->Properties["useIdouInputDisabled"] == "1") {
                $idouText += $idouData1["IDOU_TEXT"];
            }

            //異動者（留学・休学）
            $idouData2 = $db->getRow(knjc031fQuery::getTransferData($row["SCHREGNO"], $idou_date, $sdate), DB_FETCHMODE_ASSOC);
            $idou += $idouData2["IDOU_COLOR"];
            if ($model->Properties["useIdouInputDisabled"] == "1") {
                $idouText += $idouData2["IDOU_TEXT"];
            }
        }

        //異動期間は背景色を黄色にする
        $bgcolor_idou = ($idou > 0) ? "bgcolor=yellow" : "";
        $row["BGCOLOR_IDOU"] = $bgcolor_idou;

        //欠席数が注意日数を超えた場合は背景色を赤色にする
        $cmcd = "";
        if (isset($model->tyuuiNissuArray[$row["COURSECD"]."-".$row["MAJORCD"]])) {
            $cmcd = $row["COURSECD"]."-".$row["MAJORCD"];
        } else if (isset($model->tyuuiNissuArray["0-000"])) {
            $cmcd = "0-000";
        }
        $tyuuiNissu = ($cmcd) ? $model->tyuuiNissuArray[$cmcd] : "" ;
        $row["BGCOLOR_TYUUI"] = ($model->Properties["useAttendLessonOverHyouji"] == "1" && $tyuuiNissu && $row["SUM_KESSEKI"] > $tyuuiNissu) ? "bgcolor=#ff0099" : "";

        //移動可能行格納
        if ($idouText == "0") $useline[] = $schCnt;

        //編集可能データの作成
        $row = makeTextData($objForm, $model, $row, $schCnt, $bgcolor_idou, $idouText);
        $textLineCnt++;

        //氏名欄に学籍番号表記
        if ($model->Properties["use_SchregNo_hyoji"] == 1) {
            $row["SCHREGNO_SHOW"] = $row["SCHREGNO"] . "　";
        }

        //hidden(学籍番号)
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

        //データがない場合、氏名の背景色を変える
        if (strlen($row["BGCOLOR_TYUUI"]) != 0) {
            $row["BGCOLOR_NAME_SHOW"] = $row["BGCOLOR_TYUUI"];
        }

        $data[] = $row;
        $schCnt++;
    }
    $arg["attend_data"] = $data;

    //件数
    knjCreateHidden($objForm, "COUNTER", $counter);
    knjCreateHidden($objForm, "objCntSub", $textLineCnt);
    knjCreateHidden($objForm, "useLine", implode(',',$useline));
}

//編集可能データの作成
function makeTextData(&$objForm, $model, $row, $schCnt, $bgcolor_idou, $idouText) {
    //テキスト設定
    $setArray = $setTextArray = array();
    foreach ($model->item_array as $key => $val) {
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        if (in_array($val["item"], array("ATTEND", "KESSEKI", "PRESENT"))) {
            $setArray[$val["item"]] =  array("SIZE" => 0, "MAXLEN" => 0 , "label" => $val["label" ]);
        } else {
            //入力可・不可
            $disable = ($val["input"] == "1" && $idouText == "0") ? "" : " disabled";
            $setArray[$val["item"]] =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $disable , "label" => $val["label" ]);
            if ($val["input"] == "1") $setTextArray[] = $val["item"];
        }
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
    }
    //備考
    $setArray["REMARK"] =  array("SIZE" => 30, "MAXLEN" => $model->remarkMaxlen);

    //ENTERキーでの移動対象項目
    $setTextField = "";
    $textSep = "";
    foreach ($setTextArray as $key) {
        $setTextField .= $textSep.$key."[]";
        $textSep = ",";
    }
    $setTextField .= $textSep."REMARK[]";

    //出欠項目
    $setTmp = "";
    foreach ($setArray as $key => $val) {
        if ($key == "REMARK") {
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $extra = " aria-label = \"".$row["NAME_SHOW"]." 出欠の備考\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$schCnt})\"; onPaste=\"return showPaste(this, {$schCnt});\" ";
            /* Edit by HPA for PC-talker 読み end 2020/02/20 */
            $remark = knjCreateTextBox($objForm, $row[$key], $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
        } else if (in_array($key, array("ATTEND","KESSEKI","PRESENT"))) {       //表示のみの項目
            //"0"表示
            if ($model->Properties["use_Attend_zero_hyoji"] != "1" && $row[$key] == 0) {
                $row[$key] = "";
            }
        } else {

            $setStyle = "";
            if ($val["DISABLED"]) {
                $setStyle = "background-color: #999999;";
            }

            //入力文字チェック
            $setEntCheck = ($key == "DETAIL_101") ? "NumCheck(this.value)" : "toInteger(this.value)";

            //"0"表示
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                $value = $row[$key];
            } else {
                $value = ($row[$key] != 0) ? $row[$key] : "";
            }
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $id = $row["ATTENDNO"];
            $checkInteger = ($key == "DETAIL_101") ? "tmpSet1(this);" : "tmpSet(this, '$id-$key');";

            $extra = $val["DISABLED"]." STYLE=\"text-align: right; {$setStyle}\" id= '\"$id-$key\"' aria-label = \"".$row["NAME_SHOW"]." ". $val["label"]."\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onblur=\"$checkInteger this.value={$setEntCheck}\"; onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$schCnt})\"; onPaste=\"return showPaste(this, ".$schCnt.");\" ";
            /* Edit by HPA for PC-talker 読み end 2020/02/20 */
            $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
        }

        if ($key != "REMARK") {
            //順にセット
            $width = "45";
            $bgcolor_kesseki =  (in_array($key, $model->kessekiItem) && strlen($row["BGCOLOR_TYUUI"]) != 0) ? $row["BGCOLOR_TYUUI"] : "";
            $setTmp .= "<td width=\"".$width."\" {$bgcolor_kesseki} {$bgcolor_idou}>".$row[$key]."</td>";
        }
    }
    $row["ATTEND_DATA"] = $setTmp;
    $row["REMARK_DATA"] = "<td width=\"#\" {$bgcolor_idou}>".$remark."</td>";

    //累積項目
    $setRuisekiTmp = "";
    foreach ($model->itemR_array as $key => $val) {
        //"0"表示
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $value = $row["SUM_".$val["item"]];
        } else {
            $value = ($row["SUM_".$val["item"]] != 0) ? $row["SUM_".$val["item"]] : "";
        }

        $width = "45";
        $bgcolor_kesseki =  (in_array($val["item"], $model->kessekiItem) && strlen($row["BGCOLOR_TYUUI"]) != 0) ? $row["BGCOLOR_TYUUI"] : "";
        $setRuisekiTmp .= "<td width=\"".$width."\" {$bgcolor_kesseki} {$bgcolor_idou}>".$value."</td>";
    }
    $row["RUISEKI_DATA"] = $setRuisekiTmp;

    return $row;
}

//最終日取得
function getFinalDay($db, $month, $semester) {
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc031fQuery::selectSemesAll($semester),DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month && $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }
    return $lastday;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model) {
    //更新ボタン
    /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
    $extra = "aria-label = \"更新\" id=\"update\" onclick=\"current_cursor('update');return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "aria-label = \"取消\" id=\"reset\" onclick=\"current_cursor('reset');btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = " aria-label = \"終了\" onclick=\"closeMsg();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //セキュリティーチェック
    $securityCnt = $db->getOne(knjc031fQuery::getSecurityHigh());
    $csvSetName = "ＣＳＶ処理";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル処理";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        //ＣＳＶ処理ボタン
        $extra = "id= \"btn_csv\" onClick=\" current_cursor('btn_csv'); wopen('".REQUESTROOT."/X/KNJX_C031F/knjx_c031findex.php?SEND_PRGID=KNJC031F&SEND_AUTH={$model->auth}&SEND_SCHOOL_KIND={$model->school_kind}&SEND_COURSE_MAJOR={$model->field["COURSE_MAJOR"]}&SEND_hr_class={$model->field["hr_class"]}&SEND_GROUP_HR_CLASS={$model->field["GROUP_HR_CLASS"]}&SEND_month={$model->field["month"]}','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $csvSetName, $extra);
    }
    /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $name2, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    $cnt = ($blank) ? 1 : 0;
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        if ($name == "COURSE_MAJOR") {
            knjCreateHidden($objForm, "LIST_COURSE" . $row["VALUE"], $cnt);
            $cnt++;
        } else if ($name == "GROUP_HR_CLASS") {
            knjCreateHidden($objForm, "LIST_GROUP_HR_CLASS" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name2, $value, $opt, $extra, $size);
}
?>
