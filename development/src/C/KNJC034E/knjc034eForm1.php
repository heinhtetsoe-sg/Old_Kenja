<?php

require_once('for_php7.php');

class knjc034eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc034eindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //処理学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //科目コンボ
        $query = knjc034eQuery::selectSubclassQuery($model);
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $model->field["CHAIRCD"] = ($model->cmd == "subclasscd") ? "" : $model->field["CHAIRCD"];
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //講座コンボ
        $query = knjc034eQuery::selectChairQuery($model);
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $model->field["SCHREGNO"] = ($model->cmd == "chaircd") ? "" : $model->field["SCHREGNO"];
        makeCmb($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", $extra, 1, "BLANK");

        //生徒コンボ
        $query = knjc034eQuery::selectStudent($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHREGNO"], "SCHREGNO", $extra, 1, "BLANK");

        $query = knjc034eQuery::getStudentGrade($model);
        $schGrade = $db->getOne($query);
        knjCreateHidden($objForm, "schGrade", $schGrade);

        //タイトル設定
        $model->titleValC001 = setTitleData($objForm, $arg, $db, $model);

        //コピー貼付用
        $copyArray = array();
        $copyHidden = "";

        //A004（異動区分）
        $model->a004 = array();
        $query = knjc034eQuery::getNameMst("A004");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["A004_".$row["NAMECD2"]] = "1";
            if ($model->a004Field[$row["NAMECD2"]]) {
                $model->a004[$row["NAMECD2"]] = $model->a004Field[$row["NAMECD2"]];
                $copyArray[$model->a004FieldSort[$row["NAMECD2"]]] = $model->a004Field[$row["NAMECD2"]];
            }
        }
        $result->free();

        //C001
        $model->c001 = array();
        $query = knjc034eQuery::getNameMst("C001");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["C001_".$row["NAMECD2"]] = "1";
            if ($model->c001Field[$row["NAMECD2"]]) {
                $model->c001[$row["NAMECD2"]] = $model->c001Field[$row["NAMECD2"]];
                $copyArray[$model->c001FieldSort[$row["NAMECD2"]]] = $model->c001Field[$row["NAMECD2"]];
            }
        }
        $result->free();

        //エンター押下時の移動方向
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
            if ($val == "VIRUS" && !$model->Properties["useVirus"]) continue;
            if ($val == "KOUDOME" && !$model->Properties["useKoudome"]) continue;
            $copyHidden .= $sep.$val."[]";
            $sep = ":";
        }
        knjCreateHidden($objForm, "copyField", $copyHidden);

        //可変サイズ
        $gedanCol = 3;
        $gedanCol = $gedanCol + get_count($model->a004);
        $gedanCol = $gedanCol + get_count($model->c001);
        $arg["useGedanCol"] = $gedanCol;

        //出停名称
        $arg["SUSPEND_NAME"] = "時数";
        if ($model->koudome) {
            $arg["SUSPEND_NAME"] = "法止<br>時数";
        }

        //編集対象データリスト
        makeDataList($objForm, $arg, $db, $model, $schGrade);

        //累積
        if (!$model->field["SCHREGNO"]) {
            for($i=0; $i<$gedanCol-1; $i++) {
                $arg["sum_attend"][$i] = '';
            }
        } else {
            $kotei = array("APPOINTED_DAY", "LESSON");
            $sum_row = array();
            $query = knjc034eQuery::selectAttendSemester($model, 0, $schGrade);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sum_row = $row;
            }
            $result->free();

            $sum_attend = array();
            foreach ($sum_row as $key => $val) {
                if (in_array($key, $kotei) || in_array($key, $model->a004) || in_array($key, $model->c001)) {
                    if ($key == "VIRUS" && !$model->Properties["useVirus"])     $val = "";
                    if ($key == "KOUDOME" && !$model->Properties["useKoudome"]) $val = "";

                    $sum_attend[] = $val;
                }
            }
            $arg["sum_attend"] = $sum_attend;
        }

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //データベース切断
        Query::dbCheckIn($db);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "unUseOffdays", $model->Properties["unUseOffdays"]);
        knjCreateHidden($objForm, "unUseAbroad", $model->Properties["unUseAbroad"]);
        knjCreateHidden($objForm, "unUseAbsent", $model->Properties["unUseAbsent"]);
        knjCreateHidden($objForm, "changeVal", 0);
        knjCreateHidden($objForm, "HIDDEN_SUBCLASSCD");
        knjCreateHidden($objForm, "HIDDEN_CHAIRCD");
        knjCreateHidden($objForm, "HIDDEN_SCHREGNO");
        knjCreateHidden($objForm, "HIDDEN_MOVE_ENTER");

        $arg["finish"]  = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML($model, "knjc034eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
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

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, $model) {
    //C001
    $rtnTitleC001 = array();
    $result = $db->query(knjc034eQuery::getSickDiv());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnTitleC001[$row["VALUE"]] = $row["VALUE"];
    }
    $result->free();

    return $rtnTitleC001;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, $schGrade) {
    $query = knjc034eQuery::selectSemesAll($model, $schGrade);
    $result = $db->query($query);
    $data = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }

    //異動月取得
    $smonth = $emonth = array();
    $smonth = $db->getCol(knjc034eQuery::getTransferData2($model->field["SCHREGNO"], "s"));
    $emonth = $db->getCol(knjc034eQuery::getTransferData2($model->field["SCHREGNO"], "e"));

    $result->free();
    $monthCnt = 0;
    $textLineCnt = 0;
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query  = knjc034eQuery::selectAttendQuery($model, $month, $data[$dcnt]["SEMESTER"], $schGrade);
            $result = $db->query($query);
            $rowMeisai = array();
            while ($rowMeisai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //未入力月の背景色を変える
                $m_data = "";
                foreach ($rowMeisai as $key => $val) {
                    if ($key == "SUB_SCHREGNO" || in_array($key, $model->a004) || in_array($key, $model->c001)) {
                        $m_data .= $val;
                    }
                }
                if (strlen($m_data) == 0) {
                    $rowMeisai["BGCOLOR_MONTH_NAME"] = "bgcolor=#ccffcc";
                }

                $extra = "";
                if ($rowMeisai["CONTROL_CODE"] == $rowMeisai["MONTH"]) {
                    //学期の最終月取得
                    $query = knjc034eQuery::getMaxSemeMonthCnt($model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"], $schGrade);
                    $maxMonth = $db->getOne($query);

                    if (strlen($rowMeisai["LESSON"]) == 0 && $maxMonth == "1") {
                        //課程学科コース取得
                        $query = knjc034eQuery::getCourse($model->field["SCHREGNO"], $data[$dcnt]["SEMESTER"]);
                        $schCourse = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        //週数取得
                        $cm = ($model->Properties["use_school_detail_gcm_dat"] == "1") ? $schCourse["COURSECD"].$schCourse["MAJORCD"] : "";
                        $query = knjc034eQuery::getSyusu($data[$dcnt]["SEMESTER"], $cm);
                        $syusu = $db->getOne($query);
                        //単位数取得
                        $query = knjc034eQuery::getCredit($schCourse, $model);
                        $credit = $db->getOne($query);

                        if ($syusu > 0 && $credit > 0 && $model->Properties["hibiNyuuryokuNasi"] == "1") {
                            //学期内で合算したLESSONを取得
                            $query = knjc034eQuery::getSumLesson($model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"]);
                            $sumLesson = $db->getOne($query);

                            $extra = " background-color : #ff0099 ";
                            $cre_syu = $credit * $syusu;
                            $rowMeisai["LESSON"] = ($cre_syu - $sumLesson > 0) ? $cre_syu - $sumLesson : "";
                        }
                    }
                    if (!isset($model->warning)) {
                        $rowMeisai = makeTextData($objForm, $rowMeisai, $data[$dcnt]["SEMESTER"], $textLineCnt, $extra, $model);
                        $textLineCnt++;
                    } else {
                        $rowMeisai = makeErrTextData($objForm, $model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"], $rowMeisai, $textLineCnt, $extra);
                    }
                } else {
                    $rowMeisai["MONTH"] = "";
                }

                //異動者（退学・転学・卒業）
                $idou_year = ($month < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
                $appointed_day = $db->getOne(knjc034eQuery::getAppointedDay(sprintf('%02d', $month), $data[$dcnt]["SEMESTER"], $schGrade));
                $idou_day = ($appointed_day == "") ? getFinalDay($db, sprintf('%02d', $month), $data[$dcnt]["SEMESTER"]) : $appointed_day;
                $idou_date = $idou_year.'-'.sprintf('%02d', $month).'-'.$idou_day;
                $idou = $db->getOne(knjc034eQuery::getIdouData($model->field["SCHREGNO"], $idou_date));

                //異動者（留学・休学）
                $idou2 = $db->getOne(knjc034eQuery::getTransferData1($model->field["SCHREGNO"], $idou_date));
                $idou3 = 0;
                if (in_array(sprintf('%02d', $month), $smonth) || in_array(sprintf('%02d', $month), $emonth)) {
                    $idou3 = 1;
                }

                //異動期間は背景色を黄色にする
                $rowMeisai["BGCOLOR_IDOU"] = ($idou > 0 || $idou2 > 0 || $idou3 > 0) ? "bgcolor=yellow" : "";

                foreach ($rowMeisai as $key => $val) {
                    if ($key == "VIRUS" && !$model->Properties["useVirus"])     $rowMeisai["VIRUS"] = "";
                    if ($key == "KOUDOME" && !$model->Properties["useKoudome"]) $rowMeisai["KOUDOME"] = "";
                }

                $arg["attend_data".$data[$dcnt]["SEMESTER"]][] = $rowMeisai;
            }
            $result->free();
            $monthCnt++;
        }

        //学期計
        $query = knjc034eQuery::selectAttendSemester($model, $data[$dcnt]["SEMESTER"], $schGrade);

        $result2 = $db->query($query);
        $rowsemes = array();
        
        while ($rowsemes = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$model->field["SCHREGNO"]) {
                $rowsemes["MONTH_NAME"]    = $db->getOne($query);
            }

            foreach ($rowsemes as $key => $val) {
                if ($key == "VIRUS"   && !$model->Properties["useVirus"])   $rowsemes["VIRUS"]   = "";
                if ($key == "KOUDOME" && !$model->Properties["useKoudome"]) $rowsemes["KOUDOME"] = "";
            }

            $arg["sum_semester".$data[$dcnt]["SEMESTER"]][] = $rowsemes;
        }
        $result2->free();
    }
    knjCreateHidden($objForm, "objCntSub", $textLineCnt);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//エラー時の編集可能データ
function makeErrTextData(&$objForm, $model, $month, $seme, $meisai, $textLineCnt, $extra) {
    foreach($model->field["MONTH"] as $key => $val) {
        //$monthAr[0] = 月、$monthAr[1] = 学期
        $monthAr = preg_split("/-/", $val);
        if ($month == $monthAr[0] && $seme == $monthAr[1]) {
            $meisai["APPOINTED_DAY"] = $model->field["APPOINTED_DAY"][$key]; //締め日
            $meisai["LESSON"]        = $model->field["LESSON"][$key];        //授業日数
            $meisai["OFFDAYS"]       = $model->field["OFFDAYS"][$key];       //休学日数
            $meisai["ABROAD"]        = $model->field["ABROAD"][$key];        //留学日数
            $meisai["ABSENT"]        = $model->field["ABSENT"][$key];        //公欠日数
            $meisai["SUSPEND"]       = $model->field["SUSPEND"][$key];       //出停日数
            $meisai["KOUDOME"]       = $model->field["KOUDOME"][$key];       //交止
            $meisai["VIRUS"]         = $model->field["VIRUS"][$key];         //伝染病
            $meisai["MOURNING"]      = $model->field["MOURNING"][$key];      //忌引日数
            //C001
            foreach ($model->titleValC001 as $key => $val) {
                $meisai[$model->c001[$key]]          = $model->field[$model->c001[$key]][$key];
            }
            $meisai["NURSEOFF"]      = $model->field["NURSEOFF"][$key];      //保健室欠課
            $meisai["LATE"]          = $model->field["LATE"][$key];          //遅刻回数
            $meisai["EARLY"]         = $model->field["EARLY"][$key];         //早退回数
        }
    }
    $row = makeTextData($objForm, $meisai, $seme, $textLineCnt, $extra, $model);
    return $row;
}

//編集可能データの作成
function makeTextData(&$objForm, $row, $seme, $textLineCnt, $extra, $model) {
    $row["MONTH"]         = "<input type=\"hidden\" name=\"MONTH[]\" value=\"".$row["MONTH"]."-".$seme."\">";
    $row["MONTH_NAME"]    = $row["MONTH_NAME"];
    $row["APPOINTED_DAY"] = "<input type='hidden' name='APPOINTED_DAY[]' value='{$row["APPOINTED_DAY"]}'>{$row["APPOINTED_DAY"]}"; //締め日

    $setArray = array();
    $setArray["LESSON"]     =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["OFFDAYS"]    =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["ABROAD"]     =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["ABSENT"]     =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["SUSPEND"]    =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["KOUDOME"]    =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["VIRUS"]      =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["MOURNING"]   =  array("SIZE" => 2, "MAXLEN" => 3);
    //C001
    foreach ($model->titleValC001 as $key => $val) {
        $setArray[$model->c001[$key]]   =  array("SIZE" => 2, "MAXLEN" => 3);
    }
    $setArray["NURSEOFF"]   =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["LATE"]       =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["EARLY"]      =  array("SIZE" => 2, "MAXLEN" => 3);

    //移動方向対象項目
    $setTextField = "";
    $textSep = "";
    foreach ($setArray as $key => $val) {
        if ($key == "LESSON" || in_array($key, $model->a004) || in_array($key, $model->c001)) {
            if ($key == "VIRUS" && !$model->Properties["useVirus"]) continue;
            if ($key == "KOUDOME" && !$model->Properties["useKoudome"]) continue;
            $setTextField .= $textSep.$key."[]";
            $textSep = ",";
        }
    }

    foreach ($setArray as $key => $val) {
        $setStyle = "";
        if ($key == "LESSON" && $extra) {
            $setStyle = $extra;
        }
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $value = $row[$key];
        } else {
            $value = ($row[$key] != 0) ? $row[$key] : "";
        }
        $extra = " STYLE=\"text-align: right; {$setStyle}\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onblur=\"this.value=toInteger(this.value)\"; onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$textLineCnt})\"; onPaste=\"return showPaste(this, ".$textLineCnt.");\" ";
        $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
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
    $semeday = $db->getRow(knjc034eQuery::selectSemesAll($model, $semester),DB_FETCHMODE_ASSOC);
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
    $extra = ($model->field["SCHREGNO"]) ? " onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeMsg();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
