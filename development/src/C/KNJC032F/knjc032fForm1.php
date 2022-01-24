<?php

require_once('for_php7.php');

class knjc032fForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc032findex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["year"] = CTRL_YEAR;

        //処理学期
        $arg["semester"] = CTRL_SEMESTERNAME;

        //事前チェック用フラグ
        $check_flg = false;

        //上段表示
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 || $model->Properties["use_school_detail_gcm_dat"] == "1") $arg["upperline"] = 1;

        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //課程学科コンボ
            $query = knjc032fQuery::getCourseMajor($model);
            $extra = "onChange=\"btn_submit('change_course')\";";
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1);
        }

        //学級コンボ内容ラジオボタン 1:HRクラス 2:複式クラス
        $opt = array(1, 2);
        $model->field["SELECT_CLASS_TYPE"] = ($model->field["SELECT_CLASS_TYPE"] == "") ? "1" : $model->field["SELECT_CLASS_TYPE"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, "onChange=\"btn_submit('change_radio')\"; id=\"SELECT_CLASS_TYPE{$val}\"");
            knjCreateHidden($objForm, "LIST_SELECT_CLASS_TYPE" . $val, $key);
        }
        $radioArray = knjCreateRadio($objForm, "SELECT_CLASS_TYPE", $model->field["SELECT_CLASS_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //学級コンボ内容ラジオボタン表示
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1) $arg["class_type"] = 1;

        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            //複式学級コンボボックス
            $query = knjc032fQuery::getGroupHrClass($model);
            $extra = "onChange=\"btn_submit('change_class')\";";
            makeCmb($objForm, $arg, $db, $query, "GROUP_HR_CLASS", "GROUP_HR_CLASS", $model->field["GROUP_HR_CLASS"], $extra, 1, "BLANK");
        } else {
            //学級コンボボックス
            $query = knjc032fQuery::getHrClass($model);
            $extra = "onChange=\"btn_submit('change_class')\";";
            makeCmb($objForm, $arg, $db, $query, "hr_class", "HR_CLASS", $model->field["hr_class"], $extra, 1, "BLANK");

            //HRクラスは学級が選択されたときに事前チェックをする
            if ($model->field["hr_class"]) $check_flg = true;
        }

        //生徒コンボボックス
        $query = knjc032fQuery::getStudent($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, "schregno", "SCHREGNO", $model->field["schregno"], $extra, 1, "BLANK");

        //複式クラスは生徒が選択されたときに事前チェックをする
        if ($model->field["SELECT_CLASS_TYPE"] == 2 && $model->field["schregno"]) $check_flg = true;

        //事前チェック（出欠管理者コントロール）
        if (get_count($model->item_array) == 0 && $check_flg) {
            $arg["jscript"] = "preCheck();";
        }

        //タイトル設定
        setTitleData($objForm, $arg, $db, $model);

        //エンター押下時の移動方向ラジオボタン 1:縦 2:横
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //コピー貼付用
        $copyHidden = $sep = "";
        $useVirus = $useKoudome = "";
        foreach ($model->item_array as $key => $val) {
            if (in_array($val["item"], array("ATTEND", "PRESENT"))) continue;
            $copyHidden .= $sep.$val["item"]."[]";
            $sep = ":";

            if ($val["item"] == "VIRUS") $useVirus = "true";
            if ($val["item"] == "KOUDOME") $useKoudome = "true";
        }
        $copyHidden .= $sep."REMARK[]";
        knjCreateHidden($objForm, "copyField", $copyHidden);

        //可変サイズ
        $gedanCol  = 3;
        $gedanCol += get_count($model->item_array);
        $arg["useGedanCol"] = $gedanCol;

        //編集対象データリスト
        makeDataList($objForm, $arg, $db, $model);

        if (!$model->Properties["use_Attend_zero_hyoji"]) {
            $arg["ZERO_HYOJI_COMMENT"] = "※'0'データは、空白で表示します。";
        }

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useVirus", $model->virus);
        knjCreateHidden($objForm, "useKoudome", $model->koudome);
        knjCreateHidden($objForm, "unUseOffdays", $model->Properties["unUseOffdays"]);
        knjCreateHidden($objForm, "unUseAbroad", $model->Properties["unUseAbroad"]);
        knjCreateHidden($objForm, "unUseAbsent", $model->Properties["unUseAbsent"]);
        knjCreateHidden($objForm, "changeVal", 0);
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        //コンボ変更時、MSG108表示用
        knjCreateHidden($objForm, "SELECT_COURSE", $model->field["COURSE_MAJOR"]);
        knjCreateHidden($objForm, "SELECT_SELECT_CLASS_TYPE", $model->field["SELECT_CLASS_TYPE"]);
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            knjCreateHidden($objForm, "SELECT_GROUP_HR_CLASS", $model->field["GROUP_HR_CLASS"]);
        }
        knjCreateHidden($objForm, "SELECT_HR_CLASS", $model->field["hr_class"]);
        knjCreateHidden($objForm, "SELECT_SCHREGNO", $model->field["schregno"]);

        knjCreateHidden($objForm, "HIDDEN_COURSE_MAJOR");
        knjCreateHidden($objForm, "HIDDEN_SELECT_CLASS_TYPE");
        knjCreateHidden($objForm, "HIDDEN_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_GROUP_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_SCHREGNO");
        knjCreateHidden($objForm, "HIDDEN_MOVE_ENTER");

        $arg["finish"] = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML($model, "knjc032fForm1.html", $arg);
    }
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
        } else if ($name == "HR_CLASS") {
            knjCreateHidden($objForm, "LIST_HR_CLASS" . $row["VALUE"], $cnt);
            $cnt++;
        } else if ($name == "SCHREGNO") {
            knjCreateHidden($objForm, "LIST_SCHREGNO" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name2, $value, $opt, $extra, $size);
}

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, $model) {
    //使用する項目セット
    $setTmp = "";
    $allWidth = 200;
    $width = "45";
    foreach ($model->item_array as $key => $val) {
        $label = "";
        for ($i = 0; $i < mb_strlen($val["label"], "UTF-8"); $i++) {
            if ($i % 2 == 0) {
                if ($i != 0) $label .= "<br>";
                $label .= mb_substr($val["label"], $i, 2,"UTF-8");
            }
        }
        $setTmp .= "<td width=\"".$width."\">".$label."</td>";
        $allWidth += 52;
    }
    $arg["TITLE"] = $setTmp;

    //項目名セット（備考）
    $arg["REMARK_TITLE"] = "<td width=\"#\">出欠の備考</td>";
    $allWidth += 250;

    //全体のサイズ
    $arg["useAllWidth"] = $allWidth;

    return;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model) {
    //学校マスタ取得
    $schoolMst = array();
    $query = knjc032fQuery::getSchoolMst($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }
    $result->free();

    $textLineCnt = 0;
    $rowMeisai = $useline = array();
    $query  = knjc032fQuery::selectAttendQuery($model, $schoolMst);
    $result = $db->query($query);
    while ($rowMeisai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //未入力月は背景色を変える
        if (strlen($rowMeisai["SEM_SCHREGNO"]) == 0 && $rowMeisai["MONTH"] != 'ZZ') {
            $rowMeisai["BGCOLOR_MONTH_NAME"] = "bgcolor=#ccffcc";
        }

        $idou_date = $sdate = "";
        $idou = $idouText = 0;
        if ($rowMeisai["MONTH"] && $rowMeisai["MONTH"] != 'ZZ') {
            //異動対象日付
            $idou_month = sprintf('%02d', $rowMeisai["MONTH"]);
            $idou_year = ($idou_month < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
            $idou_day = ($rowMeisai["APPOINTED_DAY"] == "") ? getFinalDay($db, $idou_month, $rowMeisai["SEMESTER"]) : $rowMeisai["APPOINTED_DAY"];
            $idou_date = $idou_year.'-'.$idou_month.'-'.sprintf('%02d', $idou_day);

            //対象月の開始日
            if (sprintf('%02d', $model->semeday[$rowMeisai["SEMESTER"]]["S_MONTH"]) == $idou_month) {
                $sdate = $idou_year.'-'.$idou_month.'-'.sprintf('%02d', $model->semeday[$rowMeisai["SEMESTER"]]["S_DAY"]);
            } else {
                $sdate = $idou_year.'-'.$idou_month.'-01';
            }

            //異動者（退学・転学・卒業）
            $idouData1 = $db->getRow(knjc032fQuery::getIdouData($model->field["schregno"], $idou_date, $sdate), DB_FETCHMODE_ASSOC);
            $idou += (int)$idouData1["IDOU_COLOR"];
            // $idouText += $idouData1["IDOU_TEXT"];
            if ($model->Properties["useIdouInputDisabled"] == "1") {
                $idouText += (int)$idouData1["IDOU_TEXT"];
            }

            //異動者（留学・休学）
            $idouData2 = $db->getRow(knjc032fQuery::getTransferData($model->field["schregno"], $idou_date, $sdate), DB_FETCHMODE_ASSOC);
            $idou += (int)$idouData2["IDOU_COLOR"];
            // $idouText += $idouData2["IDOU_TEXT"];
            if ($model->Properties["useIdouInputDisabled"] == "1") {
                $idouText += (int)$idouData2["IDOU_TEXT"];
            }

        }

        //異動期間は背景色を黄色にする
        $bgcolor_idou = ($idou > 0) ? "bgcolor=yellow" : "";
        $rowMeisai["BGCOLOR_IDOU"] = $bgcolor_idou;

        //背景色を変える
        if ($rowMeisai["SEMESTER"] == '9') {
            $rowMeisai["STYLE_ROW"] = "class=\"no_search\"";
        } else if ($rowMeisai["MONTH"] == 'ZZ') {
            $rowMeisai["STYLE_ROW"] = "style=\"background-color:deepskyblue; color:white;\"";
        } else {
            $rowMeisai["STYLE_ROW"] = "bgcolor=\"#ffffff\"";
        }

        $cntUpFlg = true;
        if ($rowMeisai["CONTROL_CODE"] != $rowMeisai["MONTH"]) {
            $cntUpFlg = false;
        } else {
            //移動可能行格納
            if ($idouText == "0") $useline[] = $textLineCnt;
        }

        //編集可能データの作成
        $rowMeisai = makeTextData($objForm, $model, $rowMeisai, $textLineCnt, $bgcolor_idou, $idouText);

        if ($cntUpFlg) $textLineCnt++;

        //データセット
        $arg["attend_data"][] = $rowMeisai;
    }
    $result->free();

    knjCreateHidden($objForm, "objCntSub", $textLineCnt);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    knjCreateHidden($objForm, "useLine", implode(',',$useline));

}

//編集可能データの作成
function makeTextData(&$objForm, $model, $row, $textLineCnt, $bgcolor_idou, $idouText) {
    //テキスト設定
    $setArray = $setTextArray = array();
    foreach ($model->item_array as $key => $val) {
        if (in_array($val["item"], array("ATTEND", "PRESENT"))) {
            $setArray[$val["item"]] =  array("SIZE" => 0, "MAXLEN" => 0);
        } else if ($row["MONTH"] == 'ZZ') {
            $setArray[$val["item"]] =  array("SIZE" => 0, "MAXLEN" => 0);
        } else {
            //入力可・不可
            $disable = ($val["input"] == "1" && $idouText == "0") ? "" : " disabled";
            $setArray[$val["item"]] =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $disable);
            if ($val["input"] == "1") $setTextArray[] = $val["item"];
        }
    }
    //備考
    $setArray["REMARK"] =  array("SIZE" => 30, "MAXLEN" => 30);

    //ENTERキーでの移動対象項目
    $setTextField = "";
    $textSep = "";
    foreach ($setTextArray as $key) {
        $setTextField .= $textSep.$key."[]";
        $textSep = ",";
    }
    $setTextField .= $textSep."REMARK[]";

    //エラーのとき、編集データをセット
    if (isset($model->warning)) {
        foreach((array)$model->field["MONTH"] as $key => $val) {
            //$monthAr[0] = 月、$monthAr[1] = 学期
            $monthAr = preg_split("/-/", $val);
            if ($row["MONTH"] == $monthAr[0] && $row["SEMESTER"] == $monthAr[1]) {
                foreach ($setTextArray as $itemname) {
                    $row[$itemname] = $model->field[$itemname][$key];
                }
            }
        }
    }

    //出欠項目
    $setTmp = "";
    foreach ($setArray as $key => $val) {
        if ($row["MONTH"] == 'ZZ') {    //学期計・累計
            //"0"表示
            if ($model->Properties["use_Attend_zero_hyoji"] != "1" && $row[$key] == 0) {
                $row[$key] = "";
            }
        } else if ($row["CONTROL_CODE"] != $row["MONTH"]) {     //管理者コントロール（出欠）入力不可
            //"0"表示
            if ($model->Properties["use_Attend_zero_hyoji"] != "1" && $row[$key] == 0) {
                $row[$key] = "";
            }
        } else if (in_array($key, array("ATTEND","PRESENT"))) {       //表示のみの項目
            //"0"表示
            if ($model->Properties["use_Attend_zero_hyoji"] != "1" && $row[$key] == 0) {
                $row[$key] = "";
            }
        } else if ($key != "REMARK") {
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
            $extra = $val["DISABLED"]." STYLE=\"text-align: right; {$setStyle}\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onblur=\"this.value={$setEntCheck}\"; onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$textLineCnt})\"; onPaste=\"return showPaste(this, ".$textLineCnt.");\" ";
            $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
        }

        if ($key == "REMARK") {
            if ($row["MONTH"] != 'ZZ' && $row["CONTROL_CODE"] == $row["MONTH"]) {
                $extra = " onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$textLineCnt})\"; onPaste=\"return showPaste(this, {$textLineCnt});\" ";
                $remark = knjCreateTextBox($objForm, $row[$key], $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
            } else {
                $remark = $row[$key];
            }
        } else {
            //順にセット
            $width = "45";
            $setTmp .= "<td width=\"".$width."\" {$bgcolor_idou}>".$row[$key]."</td>";
        }
    }

    $tmpMonth = $tmpAppDay = "";
    if ($row["MONTH"] != 'ZZ' && $row["CONTROL_CODE"] == $row["MONTH"] && $idouText == "0") {
        $tmpMonth = "<input type='hidden' name='MONTH[]' value='{$row["MONTH"]}-{$row["SEMESTER"]}'>";
        $tmpAppDay = "<input type='hidden' name='APPOINTED_DAY[]' value='{$row["APPOINTED_DAY"]}'>";
    }

    $row["MONTHNAME"]    = $row["MONTHNAME"].$tmpMonth;
    $row["APPOINTED_DAY"] = $row["APPOINTED_DAY"].$tmpAppDay;

    $row["ATTEND_DATA"] = $setTmp;
    $row["REMARK_DATA"] = "<td align=\"left\" width=\"#\" {$bgcolor_idou}>&nbsp;&nbsp;".$remark."</td>";

    return $row;
}

//最終日取得
function getFinalDay($db, $month, $semester) {
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $model->semeday[$semester]["E_MONTH"]) == $month && $model->semeday[$semester]["E_DAY"] < $lastday) {
        $lastday = $model->semeday[$semester]["E_DAY"];
    }
    return $lastday;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = (!$model->field["schregno"] || AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeMsg();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
