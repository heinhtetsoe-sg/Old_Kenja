<?php

require_once('for_php7.php');

class knjc035eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc035eindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["year"] = CTRL_YEAR;

        //学期
        $arg["semester"] = CTRL_SEMESTERNAME;

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //科目コンボ
        $query = knjc035eQuery::getSubclasscd($model);
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $model->field["CHAIRCD"] = ($model->cmd == "subclasscd") ? "" : $model->field["CHAIRCD"];
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");
        list($classcd, $model->school_kind, $curriculumcd, $subclasscd) = explode("-", $model->field["SUBCLASSCD"]);

        //講座コンボ
        $query = knjc035eQuery::getChaircd($model);
        $extra = "onChange=\"btn_submit('chaircd')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", $extra, 1, "BLANK");

        //月コンボ
        $semeMonth = makeMonthSemeCmb($objForm, $arg, $db, $model);

        //授業時数セット
        makeLessonSet($objForm, $arg, $db, $model);

        //コピー貼付用
        $copyArray = array();
        $copyHidden = "";

        //A004
        $query = knjc035eQuery::getA004();
        $result = $db->query($query);
        $model->a004 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["A004_".$row["NAMECD2"]] = "1";
            $model->a004[$row["NAMECD2"]] = $model->a004Field[$row["NAMECD2"]];
            if ($model->a004Field[$row["NAMECD2"]]) {
                $copyArray[$model->a004FieldSort[$row["NAMECD2"]]] = $model->a004Field[$row["NAMECD2"]];
            }
        }
        $result->free();

        //C001
        $query = knjc035eQuery::getC001();
        $result = $db->query($query);
        $model->c001 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["C001_".$row["NAMECD2"]] = "1";
            $model->c001[$row["NAMECD2"]] = $model->c001Field[$row["NAMECD2"]];
            $arg["C001_{$row["NAMECD2"]}_NAME"] = $row["NAME1"];
            if ($model->c001Field[$row["NAMECD2"]]) {
                $copyArray[$model->c001FieldSort[$row["NAMECD2"]]] = $model->c001Field[$row["NAMECD2"]];
            }
        }
        $result->free();

        //コピー貼付用hidden
        ksort($copyArray);
        $sep = "";
        foreach ($copyArray as $key => $val) {
            if ($val == "VIRUS"   && !$model->Properties["useVirus"])   continue;
            if ($val == "KOUDOME" && !$model->Properties["useKoudome"]) continue;
            $copyHidden .= $sep.$val."[]";
            $sep = ":";
        }
        knjCreateHidden($objForm, "copyField", $copyHidden);

        /* 可変サイズ */
        $gedanCol = 14;
        /* ウイルスあり */
        if ($model->c001["25"]) {
            $arg["useVirus"] = "1";
            $gedanCol = $gedanCol + 1;
        }
        /* 交止あり */
        if ($model->c001["25"]) {
            $arg["useKoudome"] = "1";
            $gedanCol = $gedanCol + 1;
        }
        $arg["useGedanCol"] = $gedanCol;

        //更新用データ（締め日・授業時数）
        makeInputData($objForm, $arg, $db, $model);

        //項目名設定
        setTitleData($arg, $db);

        //編集対象データリスト
        makeDataList($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC035E");
        knjCreateHidden($objForm, "useVirus", $model->c001["19"] ? "true" : "");
        knjCreateHidden($objForm, "useKoudome", $model->c001["25"] ? "true" : "");
        knjCreateHidden($objForm, "sendSubclass", $model->sendSubclass);
        knjCreateHidden($objForm, "sendChair", $model->sendChair);
        knjCreateHidden($objForm, "use_Attend_zero_hyoji", $model->Properties["use_Attend_zero_hyoji"]);
        knjCreateHidden($objForm, "getPrgId", $model->getPrgId);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //コンボ変更時、MSG108表示用
        knjCreateHidden($objForm, "SELECT_SUBCLASSCD", $model->field["SUBCLASSCD"]);
        knjCreateHidden($objForm, "SELECT_CHAIRCD", $model->field["CHAIRCD"]);
        knjCreateHidden($objForm, "SELECT_MONTH", $model->field["MONTHCD"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);

        knjCreateHidden($objForm, "HIDDEN_SUBCLASSCD");
        knjCreateHidden($objForm, "HIDDEN_CHAIRCD");
        knjCreateHidden($objForm, "HIDDEN_MONTHCD");
        knjCreateHidden($objForm, "HIDDEN_LESSON_SET");
        knjCreateHidden($objForm, "HIDDEN_MOVE_ENTER");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML($model, "knjc035eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    $cnt = 1;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($name == "SUBCLASSCD") {
            knjCreateHidden($objForm, "LIST_SUBCLASSCD" . $row["VALUE"], $cnt);
            $cnt++;
        } else if ($name == "CHAIRCD") {
            knjCreateHidden($objForm, "LIST_CHAIRCD" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model) {
    $query = knjc035eQuery::selectSemesAll();
    $result = $db->query($query);
    $data = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month = setMonth($db, $data, $objForm, $model);
    $model->field["MONTHCD"] = $model->field["MONTHCD"] != "" ? $model->field["MONTHCD"] : getDefaultMonthCd($opt_month);

    $extra = "onChange=\"btn_submit('change')\";";
    $arg["MONTHCD"] = knjCreateCombo($objForm, "MONTHCD", $model->field["MONTHCD"], $opt_month, $extra, 1);

    $month = preg_split("/-/", $model->field["MONTHCD"]);
    $model->field["MONTH"]    = $month[0];
    $model->field["SEMESTER"] = $month[1];
    return $data;
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
            $query = knjc035eQuery::selectMonthQuery($month, $model);
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

//授業時数セット
function makeLessonSet(&$objForm, &$arg, $db, &$model) {

    if (in_array($model->cmd, array("subclasscd", "chaircd", "change", "reset"))) {
        $model->field["LESSON_SET"] = "";
    }

    //入力されているMAX授業時数取得
    $query = knjc035eQuery::getInputMaxLesson($model);
    $input_lesson = $db->getOne($query);

    //入力されているMAX授業時数セット
    $model->field["LESSON_SET"] = ($model->field["LESSON_SET"]) ? $model->field["LESSON_SET"] : $input_lesson;

    $extra = "style=\"text-align: right;\" onChange=\"this.style.background='#ccffcc';\" onblur=\"this.value=toInteger(this.value);\"";

    if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
        //課程学科件数取得
        $query = knjc035eQuery::getCourseMajor($model);
        $cmCnt = get_count($db->getCol($query));
        $cm = $db->getOne($query);

        if ($cmCnt == 1) {
            //授業時数区分取得
            $query = knjc035eQuery::getJugyouJisuFlg($model, $cm);
            $jugyou_jisu_flg = $db->getOne($query);

            //レコードなし＆日々出欠なし＆法定時数のとき
            if (!$model->field["LESSON_SET"] && $model->Properties["hibiNyuuryokuNasi"] == "1" && $model->Properties["useJugyoujisuuSanshutsu"] == "1" && $jugyou_jisu_flg == "1") {
                $syusu = $credit = 0;

                //学期の最終月判定
                $query = knjc035eQuery::getMaxSemeMonthCnt($model);
                $maxMonth = $db->getOne($query);

                //単位数取得
                $query = knjc035eQuery::getCredit($model);
                $credit = $db->getOne($query);

                //月別週数を使用するとき
                if ($model->Properties["use_Month_Syusu"] == "1") {
                    //月毎の週数取得
                    $query = knjc035eQuery::getMonthSyusu($model, $cm);
                    $syusu = $db->getOne($query);

                    if ($syusu > 0 && $credit > 0) {
                        $extra = "style=\"text-align: right;background-color: #ff0099;\" onblur=\"this.value=toInteger(this.value);\"";
                        $model->field["LESSON_SET"] = $credit * $syusu;
                    }

                //学期の最終月のとき
                } else if ($maxMonth == "1") {
                    //学期の週数取得
                    $query = knjc035eQuery::getSyusu($model, $model->field["SEMESTER"], $cm);
                    $syusu = $db->getOne($query);

                    if ($syusu > 0 && $credit > 0) {
                        //学期内で合算したLESSONのMAX値を取得
                        $query = knjc035eQuery::getMaxSumLesson($model);
                        $maxLesson = $db->getOne($query);

                        $extra = "style=\"text-align: right;background-color: #ff0099;\" onblur=\"this.value=toInteger(this.value);\"";
                        $cre_syu = $credit * $syusu;
                        $model->field["LESSON_SET"] = ($cre_syu - $maxLesson > 0) ? $cre_syu - $maxLesson : "";
                    }
                }
            }
        }

    } else {

        //授業時数区分取得
        $query = knjc035eQuery::getJugyouJisuFlg($model);
        $jugyou_jisu_flg = $db->getOne($query);

        //レコードなし＆日々出欠なし＆法定時数のとき
        if (!$model->field["LESSON_SET"] && $model->Properties["hibiNyuuryokuNasi"] == "1" && $model->Properties["useJugyoujisuuSanshutsu"] == "1" && $jugyou_jisu_flg == "1") {
            $syusu = $credit = 0;

            //学期の最終月判定
            $query = knjc035eQuery::getMaxSemeMonthCnt($model);
            $maxMonth = $db->getOne($query);

            //単位数取得
            $query = knjc035eQuery::getCredit($model);
            $credit = $db->getOne($query);

            //月別週数を使用するとき
            if ($model->Properties["use_Month_Syusu"] == "1") {
                //月毎の週数取得
                $query = knjc035eQuery::getMonthSyusu($model);
                $syusu = $db->getOne($query);

                if ($syusu > 0 && $credit > 0) {
                    $extra = "style=\"text-align: right;background-color: #ff0099;\" onblur=\"this.value=toInteger(this.value);\"";
                    $model->field["LESSON_SET"] = $credit * $syusu;
                }

            //学期の最終月のとき
            } else if ($maxMonth == "1") {
                //学期の週数取得
                $query = knjc035eQuery::getSyusu($model, $model->field["SEMESTER"]);
                $syusu = $db->getOne($query);

                if ($syusu > 0 && $credit > 0) {
                    //学期内で合算したLESSONのMAX値を取得
                    $query = knjc035eQuery::getMaxSumLesson($model);
                    $maxLesson = $db->getOne($query);

                    $extra = "style=\"text-align: right;background-color: #ff0099;\" onblur=\"this.value=toInteger(this.value);\"";
                    $cre_syu = $credit * $syusu;
                    $model->field["LESSON_SET"] = ($cre_syu - $maxLesson > 0) ? $cre_syu - $maxLesson : "";
                }
            }
        }
    }

    //授業時数テキスト(セット用)
    $arg["LESSON_SET"] = knjCreateTextBox($objForm, $model->field["LESSON_SET"], "LESSON_SET", 3, 3, $extra);

    //反映ボタン
    if ($model->field["SUBCLASSCD"] && $model->field["CHAIRCD"] && $model->field["MONTHCD"]) {
        $extra = "onclick=\"reflect();\"";
    } else {
        $extra = "disabled";
    }
    $arg["btn_reflect"] = knjCreateBtn($objForm, "btn_reflect", "反 映", $extra);

    //クリアボタン
    if ($model->field["SUBCLASSCD"] && $model->field["CHAIRCD"] && $model->field["MONTHCD"]) {
        $extra = "onclick=\"lesson_clear();\"";
    } else {
        $extra = "disabled";
    }
    $extra .= " style=\"padding: 1px 5px;font-size: 80%;\"";
    $arg["btn_lesson_clear"] = knjCreateBtn($objForm, "btn_lesson_clear", "ｸﾘｱ", $extra);

    return;
}

//入力フィールド作成
function makeInputData(&$objForm, &$arg, $db, &$model) {
    if ($model->cmd == "subclasscd" || $model->cmd == "change") {
        $model->appointed_day = "";
    }

    //締め日
    $query = knjc035eQuery::getAppointedDay($model);
    $model->appointed_day = $db->getOne($query);
    $arg["APPOINTED_DAY"] = $model->appointed_day;
    knjCreateHidden($objForm, "APPOINTED_DAY", $model->appointed_day);

    return;
}

//項目名設定
function setTitleData(&$arg, $db) {
    $result = $db->query(knjc035eQuery::getSickDiv());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model) {
    //学校マスタ取得
    $knjSchoolMst = $db->getRow(knjc035eQuery::getSchoolMst($model), DB_FETCHMODE_ASSOC);

    $schCnt = 0;
    $textLineCnt = 0;
    $query  = knjc035eQuery::selectMeisaiQuery($model, $knjSchoolMst);
    $result = $db->query($query);
    $rowMeisai = array();
    while ($rowMeisai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $schregno = $rowMeisai["SCHREGNO"];

        //5行毎に色を変える
        if ($schCnt % 5 == 0) {
            $colorFlg = !$colorFlg;
        }
        $bgcolor_basic = $colorFlg ? "bgcolor=#ffffff" : "bgcolor=#cccccc";

        //編集データ
        if (!isset($model->warning)) {
            $rowMeisai = makeTextData($objForm, $model, $rowMeisai, $schCnt, $bgcolor_basic);
            $textLineCnt++;
        } else {
            $rowMeisai = makeErrTextData($objForm, $model, $rowMeisai["SCHREGNO"], $rowMeisai, $schCnt, $bgcolor_basic);
        }

        $idou = $idou2 = 0;
        if ($model->field["MONTH"]) {
            //異動者（退学・転学・卒業）
            $idou_month = sprintf('%02d', $model->field["MONTH"]);
            $idou_year = ($idou_month < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
            $appointed_day = $db->getOne(knjc035eQuery::getAppointedDay($model));
            $idou_day = ($appointed_day == "") ? getFinalDay($db, $idou_month, $model->field["SEMESTER"]) : $appointed_day;
            $idou_date = $idou_year.'-'.$idou_month.'-'.$idou_day;
            $idou = $db->getOne(knjc035eQuery::getIdouData($schregno, $idou_date));

            //異動者（留学・休学）
            $idou2 = $db->getOne(knjc035eQuery::getTransferData($schregno, $idou_date));
        }
        //異動期間は背景色を黄色にする
        $rowMeisai["BGCOLOR_IDOU"] = ($idou > 0 || $idou2 > 0) ? "bgcolor=yellow" : $bgcolor_basic;

        foreach ($rowMeisai as $key => $val) {
            if ($key == "VIRUS"   && !$model->Properties["useVirus"])   $rowMeisai["VIRUS"]   = "";
            if ($key == "KOUDOME" && !$model->Properties["useKoudome"]) $rowMeisai["KOUDOME"] = "";
        }

        //氏名欄に学籍番号表記
        if ($model->Properties["use_SchregNo_hyoji"] == 1) {
            $rowMeisai["SCHREGNO_SHOW"] = $schregno . "　";
        }

        $arg["attend_data"][] = $rowMeisai;
        $schCnt++;
    }
    $result->free();
    //件数
    knjCreateHidden($objForm, "COUNTER", $schCnt);
    knjCreateHidden($objForm, "objCntSub", $textLineCnt);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//エラー時の編集可能データ
function makeErrTextData(&$objForm, $model, $schregno, $meisai, $schCnt, $bgcolor_basic) {
    foreach($model->field["SCHREGNO"] as $key => $val) {
        if ($schregno == $val) {
            $meisai["APPOINTED_DAY"] = $model->field["APPOINTED_DAY"][$key]; //締め日
            $meisai["LESSON"]        = $model->field["LESSON"][$key];        //授業日数
            $meisai["OFFDAYS"]       = $model->field["OFFDAYS"][$key];       //休学日数
            $meisai["ABROAD"]        = $model->field["ABROAD"][$key];        //留学日数
            $meisai["ABSENT"]        = $model->field["ABSENT"][$key];        //公欠日数
            $meisai["SUSPEND"]       = $model->field["SUSPEND"][$key];       //出停日数
            $meisai["KOUDOME"]       = $model->field["KOUDOME"][$key];       //交止
            $meisai["VIRUS"]         = $model->field["VIRUS"][$key];         //伝染病
            $meisai["MOURNING"]      = $model->field["MOURNING"][$key];      //忌引日数
            $meisai["SICK"]          = $model->field["SICK"][$key];          //病欠
            $meisai["NOTICE"]        = $model->field["NOTICE"][$key];        //事故欠届
            $meisai["NONOTICE"]      = $model->field["NONOTICE"][$key];      //事故欠無
            $meisai["NURSEOFF"]      = $model->field["NURSEOFF"][$key];      //保健室欠課
            $meisai["LATE"]          = $model->field["LATE"][$key];          //遅刻回数
            $meisai["EARLY"]         = $model->field["EARLY"][$key];         //早退回数
        }
    }
    $row = makeTextData($objForm, $model, $meisai, $schCnt, $bgcolor_basic);

    return $row;
}

//編集可能データの作成
function makeTextData(&$objForm, $model, $row, $schCnt, $bgcolor_basic) {

    $row["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$row["SCHREGNO"]."\">";
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
    $setArray["SICK"]       =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["NOTICE"]     =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["NONOTICE"]   =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["NURSEOFF"]   =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["LATE"]       =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["EARLY"]      =  array("SIZE" => 2, "MAXLEN" => 3);

    $setTextField = "";
    $textSep = "";
    foreach ($setArray as $key => $val) {
        if ($key == "LESSON" || in_array($key, $model->a004) || in_array($key, $model->c001)) {
            if ($key == "VIRUS"   && !$model->Properties["useVirus"])   continue;
            if ($key == "KOUDOME" && !$model->Properties["useKoudome"]) continue;
            $setTextField .= $textSep.$key."[]";
            $textSep = ",";
        }
    }

    foreach ($setArray as $key => $val) {
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $value = $row[$key];
        } else {
            $value = ($row[$key] != 0) ? $row[$key] : "";
        }
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$schCnt})\"; onPaste=\"return showPaste(this, ".$schCnt.");\"";
        $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
    }
    //未入力生徒の背景色を変える
    $row["BGCOLOR_MONTH_NAME"] = (strlen($row["SUBCL_SCHREGNO"]) == 0) ? "bgcolor=#ccffcc" : $bgcolor_basic;

    return $row;
}

//最終日取得
function getFinalDay($db, $month, $semester) {
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc035eQuery::selectSemesAll($semester), DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month && $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }

    return $lastday;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model) {
    //保存ボタン
    if ($model->field["SUBCLASSCD"] && $model->field["CHAIRCD"] && $model->field["MONTHCD"]) {
        $extra = "onclick=\"return btn_submit('update');\"";
    } else {
        $extra = "disabled";
    }
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $setEndTitle = $model->getPrgId ? "戻 る" : "終 了";
    $extra = $model->getPrgId ? "onclick=\"closeFunc();\"" : "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", $setEndTitle, $extra);

    //印刷
    //コールされたら、ボタン非表示
    if (!$model->getPrgId) {
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    }

    //セキュリティーチェック
    $securityCnt = $db->getOne(knjc035eQuery::getSecurityHigh());
    $csvSetName = "ＣＳＶ処理";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル処理";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        //ＣＳＶ処理ボタン
        //コールされたら、ボタン非表示
        if (!$model->getPrgId) {
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_C035E/knjx_c035eindex.php?SEND_PRGID=KNJC035E&SEND_AUTH={$model->auth}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_CHAIRCD={$model->field["CHAIRCD"]}&SEND_MONTHCD={$model->field["MONTHCD"]}&selectSchoolKind={$model->selectSchoolKind}','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $csvSetName, $extra);
        }
    }
}
?>
