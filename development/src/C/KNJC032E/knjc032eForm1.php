<?php

require_once('for_php7.php');

class knjc032eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc032eindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["year"] = CTRL_YEAR;

        //処理学期
        $arg["semester"] = CTRL_SEMESTERNAME;

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
            $query = knjc032eQuery::getGroupHrClass($model);
            $extra = "onChange=\"btn_submit('change_class')\";";
            makeCmb($objForm, $arg, $db, $query, "GROUP_HR_CLASS", "GROUP_HR_CLASS", $model->field["GROUP_HR_CLASS"], $extra, 1, "BLANK");
        } else {
            //学級コンボボックス
            $query = knjc032eQuery::getHrClass($model);
            $extra = "onChange=\"btn_submit('change_class')\";";
            makeCmb($objForm, $arg, $db, $query, "hr_class", "HR_CLASS", $model->field["hr_class"], $extra, 1, "BLANK");
        }

        //生徒コンボボックス
        $query = knjc032eQuery::getStudent($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, "schregno", "SCHREGNO", $model->field["schregno"], $extra, 1, "BLANK");

        //タイトル設定
        list($model->titleValC001, $model->titleValC002) = setTitleData($objForm, $arg, $db, $model);

        //コピー貼付用
        $copyArray = array();
        $copyHidden = "";

        //A004（異動区分）
        $query = knjc032eQuery::getNameMst("A004");
        $result = $db->query($query);
        $model->a004 = $model->a004Show = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $copy = true;
            if ($model->a004Field[$row["NAMECD2"]] == "OFFDAYS") {
                if ($model->Properties["unUseOffdays"] == "true") {
                    $copy = false;
                }
            } else if ($model->a004Field[$row["NAMECD2"]] == "ABROAD") {
                if ($model->Properties["unUseAbroad"] == "true") {
                    $copy = false;
                }
            }

            $arg["A004_".$row["NAMECD2"]] = "1";
            if ($model->a004Field[$row["NAMECD2"]]) {
                $model->a004Show[$row["NAMECD2"]] = $model->a004Field[$row["NAMECD2"]];
                if ($copy == true) {
                    $model->a004[$row["NAMECD2"]] = $model->a004Field[$row["NAMECD2"]];
                    $copyArray[$model->a004FieldSort[$row["NAMECD2"]]] = $model->a004Field[$row["NAMECD2"]];
                }
            }
        }
        $result->free();

        //C001
        $model->virus = $model->koudome = false;
        $model->c001 = $model->c001Show = array();
        $query = knjc032eQuery::getNameMst("C001");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $copy = true;
            if ($model->c001Field[$row["NAMECD2"]] == "ABSENT") {
                if ($model->Properties["unUseAbsent"] == "true") {
                    $copy = false;
                }
            } else if ($model->c001Field[$row["NAMECD2"]] == "VIRUS") {
                if ($model->Properties["useVirus"] != "true") {
                    $copy = false;
                } else {
                    $model->virus = true;
                }
            } else if ($model->c001Field[$row["NAMECD2"]] == "KOUDOME") {
                if ($model->Properties["useKoudome"] != "true") {
                    $copy = false;
                } else {
                    $model->koudome = true;
                }
            }

            $arg["C001_".$row["NAMECD2"]] = "1";
            if ($model->c001Field[$row["NAMECD2"]]) {
                $model->c001Show[$row["NAMECD2"]] = $model->c001Field[$row["NAMECD2"]];
                if ($copy == true) {
                    $model->c001[$row["NAMECD2"]] = $model->c001Field[$row["NAMECD2"]];
                    $copyArray[$model->c001FieldSort[$row["NAMECD2"]]] = $model->c001Field[$row["NAMECD2"]];
                }
            }
        }
        $result->free();

        //C002
        $model->c002 = $model->c002Show = array();
        $query = knjc032eQuery::getNameMst("C002");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["C002_".$row["NAMECD2"]] = "1";
            if ($model->c002Field[$row["NAMECD2"]]) {
                $model->c002[$row["NAMECD2"]] = $model->c002Field[$row["NAMECD2"]];
                $model->c002Show[$row["NAMECD2"]] = $model->c002Field[$row["NAMECD2"]];
                $copyArray[$model->c002FieldSort[$row["NAMECD2"]]] = $model->c002Field[$row["NAMECD2"]];
            }
        }
        $result->free();

        //エンター押下時の移動方向ラジオボタン 1:縦 2:横
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //コピー貼付用hidden
        ksort($copyArray);
        $sep = "";
        foreach ($copyArray as $key => $val) {
            $copyHidden .= $sep.$val."[]";
            $sep = ":";
        }
        knjCreateHidden($objForm, "copyField", $copyHidden);

        //可変サイズ
        $gedanCol = 6;
        $gedanCol = $gedanCol + get_count($model->a004Show);
        $gedanCol = $gedanCol + get_count($model->c001Show);
        $gedanCol = $gedanCol + get_count($model->c002Show);
        $arg["useGedanCol"] = $gedanCol;

        //出停名称
        $arg["SUSPEND_NAME"] = "";
        if ($model->koudome) {
            $arg["SUSPEND_NAME"] = "法止";
        }

        //学校マスタ取得
        $schoolMst = array();
        $query = knjc032eQuery::getSchoolMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $schoolMst[$key] = $val;
            }
        }
        $result->free();

        //編集対象データリスト
        makeDataList($objForm, $arg, $db, $model, $schoolMst);

        //累積
        if (!$model->field["schregno"]) {
            for($i=0; $i<$gedanCol-1; $i++) {
                $arg["sum_attend"][$i] = '';
            }
        } else {
            $sum_row = array();
            $kotei = array("APPOINTED_DAY", "LESSON", "CLASSDAYS2", "CLASSDAYS3", "REMARK");
            $query = knjc032eQuery::selectAttendSemester($model, 0);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sum_row = $row;
            }
            $result->free();

            //出席すべき日数・出席日数の計算
            $sum_attend = array();
            $exclude2 = $exclude3 = 0;
            foreach ($sum_row as $key => $val) {
                if (in_array($key, $kotei) || in_array($key, $model->a004) || in_array($key, $model->c001) || in_array($key, $model->c002)) {
                    if (in_array($key, array("SUSPEND", "MOURNING", "OFFDAYS", "ABROAD", "KOUDOME", "VIRUS"))) {
                        $exclude2 += $val;
                    }
                    if ($key == "OFFDAYS" && $schoolMst["SEM_OFFDAYS"] == "1") {
                        $exclude2 -= $val;
                    }
                    if (in_array($key, array("SUSPEND", "MOURNING", "OFFDAYS", "ABROAD", "KOUDOME", "VIRUS", "SICK", "NOTICE", "NONOTICE"))) {
                        $exclude3 += $val;
                    }
                    //値をセット
                    if ($key == "CLASSDAYS2") {
                        $sum_attend[] =  $sum_row["LESSON"] - $exclude2;
                    } else if ($key == "CLASSDAYS3") {
                        $sum_attend[] = $sum_row["LESSON"] - $exclude3;
                    } else {
                        $sum_attend[] = $val;
                    }
                } else if (in_array($key, $model->a004Show) || in_array($key, $model->c001Show) || in_array($key, $model->c002Show)) {
                    if (in_array($key, $model->a004Show)) {
                        $sum_attend[] = $val;
                    } else if ($key == "ABSENT") {
                        $sum_attend[] = $val;
                    } else {
                        $sum_attend[] = "";
                    }
                }
            }
            $arg["sum_attend"] = $sum_attend;
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

        knjCreateHidden($objForm, "HIDDEN_SELECT_CLASS_TYPE");
        knjCreateHidden($objForm, "HIDDEN_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_GROUP_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_SCHREGNO");
        knjCreateHidden($objForm, "HIDDEN_MOVE_ENTER");

        $arg["finish"] = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML($model, "knjc032eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $name2, $value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name2, $value, $opt, $extra, $size);
}

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, $model) {
    //C001
    $rtnTitleC001 = array();
    $result = $db->query(knjc032eQuery::getSickDiv());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnTitleC001[$row["VALUE"]] = $row["VALUE"];
        knjCreateHidden($objForm, $model->c001[$row["VALUE"]]."_FLG", 1);
    }
    $result->free();

    //C002
    $rtnTitleC002 = array();
    $result = $db->query(knjc032eQuery::getNameMst("C002"));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->c002Field[$row["NAMECD2"]]) {
            $arg["DETAIL_TITLE_".$row["NAMECD2"]] = $row["NAME1"];
            $rtnTitleC002[$row["NAMECD2"]] = $row["NAMECD2"];
        }
    }
    $result->free();

    return array($rtnTitleC001, $rtnTitleC002);
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, $schoolMst) {
    //学期・月範囲取得
    $query = knjc032eQuery::selectSemesAll();
    $result = $db->query($query);
    $data = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    //異動月取得
    $smonth = $emonth = array();
    $smonth = $db->getCol(knjc032eQuery::getTransferData2($model->field["schregno"], "s"));
    $emonth = $db->getCol(knjc032eQuery::getTransferData2($model->field["schregno"], "e"));

    $monthCnt = 0;
    $textLineCnt = 0;
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query  = knjc032eQuery::selectAttendQuery($model, $month, $data[$dcnt]["SEMESTER"], $schoolMst);
            $result = $db->query($query);
            $rowMeisai = array();
            while ($rowMeisai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //出席すべき日数・出席日数の計算
                $exclude2 = $exclude3 = 0;
                foreach ($rowMeisai as $key => $val) {
                    if (in_array($key, $model->a004) || in_array($key, $model->c001)) {
                        if (in_array($key, array("SUSPEND", "MOURNING", "OFFDAYS", "ABROAD", "KOUDOME", "VIRUS"))) {
                            $exclude2 += $val;
                        }
                        if ($key == "OFFDAYS" && $schoolMst["SEM_OFFDAYS"] == "1") {
                            $exclude2 -= $val;
                        }
                        if (in_array($key, array("SUSPEND", "MOURNING", "OFFDAYS", "ABROAD", "KOUDOME", "VIRUS", "SICK", "NOTICE", "NONOTICE"))) {
                            $exclude3 += $val;
                        }
                    }
                }

                //未入力月は背景色を変える
                if (strlen($rowMeisai["SEM_SCHREGNO"]) == 0) {
                    $rowMeisai["BGCOLOR_MONTH_NAME"] = "bgcolor=#ccffcc";
                }

                $rowMeisai["CLASSDAYS2"] = ($rowMeisai["LESSON"] == "") ? "" : $rowMeisai["LESSON"] - $exclude2;
                $rowMeisai["CLASSDAYS3"] = ($rowMeisai["LESSON"] == "") ? "" : $rowMeisai["LESSON"] - $exclude3;

                if ($rowMeisai["CONTROL_CODE"] == $rowMeisai["MONTH"]) {
                    if (!isset($model->warning)) {
                        $rowMeisai = makeTextData($objForm, $model, $rowMeisai, $data[$dcnt]["SEMESTER"], $textLineCnt);
                    } else {
                        $rowMeisai = makeErrTextData($objForm, $model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"], $rowMeisai, $textLineCnt);
                    }
                    $textLineCnt++;
                } else {
                    $rowMeisai["MONTH"] = "";
                }

                //異動者（退学・転学・卒業）
                $idou_year = ($month < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
                $appointed_day = $db->getOne(knjc032eQuery::getAppointedDay(sprintf('%02d', $month), $data[$dcnt]["SEMESTER"] , $model));
                $idou_day = ($appointed_day == "") ? getFinalDay($db, sprintf('%02d', $month), $data[$dcnt]["SEMESTER"]) : $appointed_day;
                $idou_date = $idou_year.'-'.sprintf('%02d', $month).'-'.$idou_day;
                $idou = $db->getOne(knjc032eQuery::getIdouData($model->field["schregno"], $idou_date));

                //異動者（留学・休学）
                $idou2 = $db->getOne(knjc032eQuery::getTransferData1($model->field["schregno"], $idou_date));
                $idou3 = 0;
                if (in_array(sprintf('%02d', $month), $smonth) || in_array(sprintf('%02d', $month), $emonth)) {
                    $idou3 = 1;
                }

                //異動期間は背景色を黄色にする
                $rowMeisai["BGCOLOR_IDOU"] = ($idou > 0 || $idou2 > 0 || $idou3 > 0) ? "bgcolor=yellow" : "";

                //データセット
                $arg["attend_data".$data[$dcnt]["SEMESTER"]][] = $rowMeisai;
            }
            $result->free();
            $monthCnt++;
        }
        //学期計
        $query = knjc032eQuery::selectAttendSemester($model, $data[$dcnt]["SEMESTER"]);
        $result2 = $db->query($query);
        $rowsemes = array();
        while ($rowsemes = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$model->field["schregno"]) {
                $rowsemes["MONTH_NAME"]    = $db->getOne($query);
            }

            //出席すべき日数・出席日数の計算
            $exclude2 = $exclude3 = 0;
            foreach ($rowsemes as $key => $val) {
                if (in_array($key, $model->a004) || in_array($key, $model->c001)) {
                    if (in_array($key, array("SUSPEND", "MOURNING", "OFFDAYS", "ABROAD", "KOUDOME", "VIRUS"))) {
                        $exclude2 += $val;
                    }
                    if ($key == "OFFDAYS" && $schoolMst["SEM_OFFDAYS"] == "1") {
                        $exclude2 -= $val;
                    }
                    if (in_array($key, array("SUSPEND", "MOURNING", "OFFDAYS", "ABROAD", "KOUDOME", "VIRUS", "SICK", "NOTICE", "NONOTICE"))) {
                        $exclude3 += $val;
                    }
                }
                if ($key == "VIRUS" && !$model->virus)      $rowsemes["VIRUS"] = "";
                if ($key == "KOUDOME" && !$model->koudome)  $rowsemes["KOUDOME"] = "";
            }

            if ($model->field["schregno"]) {
                $rowsemes["CLASSDAYS2"] = ($rowsemes["LESSON"] == "") ? "" : $rowsemes["LESSON"] - $exclude2;
                $rowsemes["CLASSDAYS3"] = ($rowsemes["LESSON"] == "") ? "" : $rowsemes["LESSON"] - $exclude3;
            }

            $arg["sum_semester".$data[$dcnt]["SEMESTER"]][] = $rowsemes;
        }
        $result2->free();
    }

    knjCreateHidden($objForm, "objCntSub", $textLineCnt);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//エラー時の編集可能データ
function makeErrTextData(&$objForm, $model, $month, $seme, $meisai, $textLineCnt) {
    foreach($model->field["MONTH"] as $key => $val){
        //$monthAr[0] = 月、$monthAr[1] = 学期
        $monthAr = preg_split("/-/", $val);
        if ($month == $monthAr[0] && $seme == $monthAr[1]) {
            $meisai["APPOINTED_DAY"] = $model->field["APPOINTED_DAY"][$key]; //締め日
            $meisai["LESSON"]        = $model->field["LESSON"][$key];        //授業日数
            $meisai["OFFDAYS"]       = $model->field["OFFDAYS"][$key];       //休学日数
            $meisai["ABROAD"]        = $model->field["ABROAD"][$key];        //留学日数
            $meisai["ABSENT"]        = $model->field["ABSENT"][$key];        //公欠日数
            $meisai["SUSPEND"]       = $model->field["SUSPEND"][$key];       //出停日数
            if ($model->koudome) {
                $meisai["KOUDOME"]       = $model->field["KOUDOME"][$key];   //交止
            }
            if ($model->virus) {
                $meisai["VIRUS"]       = $model->field["VIRUS"][$key];       //伝染病
            }
            $meisai["MOURNING"]      = $model->field["MOURNING"][$key];      //忌引日数
            $meisai["SICK"]          = $model->field["SICK"][$key];          //病欠
            $meisai["NOTICE"]        = $model->field["NOTICE"][$key];        //事故欠届
            $meisai["NONOTICE"]      = $model->field["NONOTICE"][$key];      //事故欠無
            $meisai["LATE"]          = $model->field["LATE"][$key];          //遅刻回数
            $meisai["EARLY"]         = $model->field["EARLY"][$key];         //早退回数
        }

    }
    $row = makeTextData($objForm, $model, $meisai, $seme, $textLineCnt);
    return $row;
}

//編集可能データの作成
function makeTextData(&$objForm, $model, $row, $seme, $textLineCnt) {

    $row["MONTH"]         = "<input type=\"hidden\" name=\"MONTH[]\" value=\"".$row["MONTH"]."-".$seme."\">";
    $row["MONTH_NAME"]    = $row["MONTH_NAME"];
    $row["APPOINTED_DAY"] = "<input type='hidden' name='APPOINTED_DAY[]' value='{$row["APPOINTED_DAY"]}'>{$row["APPOINTED_DAY"]}"; //締め日

    $setArray = array();
    $setArray["LESSON"]     =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["OFFDAYS"]    =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseOffdays"] == "true" ? " disabled " : "");
    $setArray["ABROAD"]     =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseAbroad"] == "true" ? " disabled " : "");
    $setArray["ABSENT"]     =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseAbsent"] == "true" ? " disabled " : "");
    $setArray["SUSPEND"]    =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["KOUDOME"]    =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["VIRUS"]      =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["MOURNING"]   =  array("SIZE" => 2, "MAXLEN" => 3);
    //C001
    foreach ($model->titleValC001 as $key => $val) {
        $setArray[$model->c001[$key]]   =  array("SIZE" => 2, "MAXLEN" => 3);
    }
    $setArray["LATE"]       =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["EARLY"]      =  array("SIZE" => 2, "MAXLEN" => 3);
    //C002
    foreach ($model->titleValC002 as $key => $val) {
        $setArray[$model->c002[$key]]   =  array("SIZE" => 2, "MAXLEN" => 3);
    }
    $setArray["REMARK"]     =  array("SIZE" => 30, "MAXLEN" => 30);

    $setTextField = "";
    $textSep = "";
    $kotei = array("LESSON", "REMARK");
    foreach ($setArray as $key => $val) {
        if (in_array($key, $kotei) || in_array($key, $model->a004) || in_array($key, $model->c001) || in_array($key, $model->c002)) {
            if ($val["DISABLED"]) continue;
            $setTextField .= $textSep.$key."[]";
            $textSep = ",";
        }
    }

    foreach ($setArray as $key => $val) {
        if (in_array($key, $kotei) || in_array($key, $model->a004Show) || ($key == "ABSENT" && in_array($key, $model->c001Show)) || in_array($key, $model->c001) || in_array($key, $model->c002Show)) {
            if ($key == "REMARK") {
                $extra = " onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$textLineCnt})\"; onPaste=\"return showPaste(this, ".$textLineCnt.");\" ";
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
                $extra = $val["DISABLED"]." STYLE=\"text-align: right; {$setStyle}\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onblur=\"this.value=toInteger(this.value)\"; onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$textLineCnt})\"; onPaste=\"return showPaste(this, ".$textLineCnt.");\" ";
                $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
            }
        }
    }

    return $row;
}

//最終日取得
function getFinalDay($db, $month, $semester) {
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc032eQuery::selectSemesAll($semester),DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month &&
        $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }
    return $lastday;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model) {
    //保存ボタン
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
