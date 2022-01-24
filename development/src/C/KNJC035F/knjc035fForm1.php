<?php

require_once('for_php7.php');

class knjc035fForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc035findex.php", "", "main");

        // Add by PP for CurrentCursor and PC-Talker 2020-01-20 start
        $arg["TITLE_LABEL"] = "講座別欠時数情報入力画面";
        echo "<script>var TITLE= '".$arg["TITLE_LABEL"]."';
              </script>";
        // Add by PP for CurrentCursor and PC-Talker 2020-01-31 end

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //事前チェック（出欠管理者コントロール）
        if (get_count($model->item_array) == 0) {
            $arg["jscript"] = "preCheck();";
        }

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjc035fQuery::getNameMstA023($model);
            // Add by PP for CurrentCursor and PC-Talker 2020-01-20 start
            $extra = "id=\"schoolkind\" onChange=\"current_cursor('schoolkind'); btn_submit('schoolkind')\"; aria-label=\"校種\"";
            // Add by PP for CurrentCursor and PC-Talker 2020-01-31 end
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1);
        }

        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //課程学科コンボ
            $query = knjc035fQuery::getCourseMajor($model, $model->field["SCHOOL_KIND"]);
            // Add by PP for CurrentCursor and PC-Talker 2020-01-20 start
            $extra = "id=\"course\" onChange=\"current_cursor('course'); btn_submit('course')\"; aria-label=\"\"";
            // Add by PP for CurrentCursor and PC-Talker 2020-01-31 end
            $model->field["SUBCLASSCD"] = ($model->cmd == "course") ? "" : $model->field["SUBCLASSCD"];
            makeCmb($objForm, $arg, $db, $query, $model->field["COURSE_MAJOR"], "COURSE_MAJOR", $extra, 1);
        }

        //科目コンボ
        $query = knjc035fQuery::getSubclasscd($model);
        // Add by PP for CurrentCursor and PC-Talker 2020-01-20 start
        $extra = "id=\"subclasscd\" onChange=\"current_cursor('subclasscd'); btn_submit('subclasscd')\";  aria-label=\"科目\"";
        // Add by PP for CurrentCursor and PC-Talker 2020-01-31 end
        $model->field["CHAIRCD"] = ($model->cmd == "subclasscd") ? "" : $model->field["CHAIRCD"];
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //講座コンボ
        $query = knjc035fQuery::getChaircd($model);
        // Add by PP for CurrentCursor and PC-Talker 2020-01-20 start
        $extra = "id=\"chaircd\" onChange=\"current_cursor('chaircd'); btn_submit('chaircd')\";  aria-label=\"講座\"";
        // Add by PP for CurrentCursor and PC-Talker 2020-01-31 end
        makeCmb($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", $extra, 1, "BLANK");

        //月コンボ
        $semeMonth = makeMonthSemeCmb($objForm, $arg, $db, $model);

        //締め日
        makeAppointedDay($objForm, $arg, $db, $model);

        //授業時数セット
        $btn_lesson_clear = makeLessonSet($objForm, $arg, $db, $model);

        //項目名設定
        setTitleData($arg, $db, $model, $btn_lesson_clear);

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach ($opt as $key => $val) {
            $label = ($val == 1) ? "aria-label=\"移動方向の縦\"" : "aria-label=\"移動方向の横\"";
            array_push($extra, " id=\"MOVE_ENTER{$val}\" $label");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //出欠完了データ
        if (isset($model->warning)) {
            //エラーの場合
            $executed = $model->field["EXECUTED"];
        } else {
            //取得
            $executed = $db->getOne(knjc035fQuery::getExecuted($model));
        }

        //出欠完了チェックボックス
        $extra = "disabled";
        if ($model->field["CHAIRCD"] && $model->field["MONTHCD"]) {
            $extra  = ($executed == "1") ? "checked" : "";
            $extra .= " id=\"EXECUTED\" onclick=\"checkExecutedLabel(this, 'zumi');\"";
        }
        $arg["EXECUTED"] = knjCreateCheckBox($objForm, "EXECUTED", "1", $extra, "");

        //出欠完了チェックボックス（ラベル）
        $zumi = "";
        if ($model->field["CHAIRCD"] && $model->field["MONTHCD"]) {
            if ($executed == "1") {
                $zumi = "<LABEL for=\"EXECUTED\"><span id=\"zumi\" style=\"color:white;\">出欠済</span></LABEL>";
            } else {
                $zumi = "<LABEL for=\"EXECUTED\"><span id=\"zumi\" style=\"color:#ff0099;\">出欠未</span></LABEL>";
            }
        }
        $arg["EXECUTED_LABEL"] = $zumi;

        //コピー貼付用hidden
        $sep = "";
        $useVirus = $useKoudome = "";
        foreach ($model->item_array as $key => $val) {
            $copyHidden .= $sep.$val["item"]."[]";
            $sep = ":";

            if ($val["item"] == "VIRUS") {
                $useVirus = "true";
            }
            if ($val["item"] == "KOUDOME") {
                $useKoudome = "true";
            }
        }
        knjCreateHidden($objForm, "copyField", $copyHidden);

        //編集対象データリスト
        makeDataList($objForm, $arg, $db, $model);

        if (!$model->Properties["use_Attend_zero_hyoji"]) {
            $arg["ZERO_HYOJI_COMMENT"] = "※'0'データは、空白で表示します。";
        }

        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC035F");
        knjCreateHidden($objForm, "useVirus", $useVirus);
        knjCreateHidden($objForm, "useKoudome", $useKoudome);
        knjCreateHidden($objForm, "sendCourseMajor", $model->sendCourseMajor);
        knjCreateHidden($objForm, "sendSubclass", $model->sendSubclass);
        knjCreateHidden($objForm, "sendChair", $model->sendChair);
        knjCreateHidden($objForm, "use_Attend_zero_hyoji", $model->Properties["use_Attend_zero_hyoji"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        knjCreateHidden($objForm, "getPrgId", $model->getPrgId);

        //コンボ変更時、MSG108表示用
        knjCreateHidden($objForm, "SELECT_COURSE_MAJOR", $model->field["COURSE_MAJOR"]);
        knjCreateHidden($objForm, "SELECT_SUBCLASSCD", $model->field["SUBCLASSCD"]);
        knjCreateHidden($objForm, "SELECT_CHAIRCD", $model->field["CHAIRCD"]);
        knjCreateHidden($objForm, "SELECT_MONTH", $model->field["MONTHCD"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);

        knjCreateHidden($objForm, "HIDDEN_SCHOOL_KIND");
        knjCreateHidden($objForm, "HIDDEN_COURSE_MAJOR");
        knjCreateHidden($objForm, "HIDDEN_SUBCLASSCD");
        knjCreateHidden($objForm, "HIDDEN_CHAIRCD");
        knjCreateHidden($objForm, "HIDDEN_MONTHCD");
        knjCreateHidden($objForm, "HIDDEN_LESSON_SET");
        knjCreateHidden($objForm, "HIDDEN_MOVE_ENTER");
        knjCreateHidden($objForm, "HIDDEN_EXECUTED");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML($model, "knjc035fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    $cnt = ($blank == "BLANK") ? 1 : 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        if ($name == "COURSE_MAJOR") {
            knjCreateHidden($objForm, "LIST_COURSE_MAJOR" . $row["VALUE"], $cnt);
            $cnt++;
        } elseif ($name == "SUBCLASSCD") {
            knjCreateHidden($objForm, "LIST_SUBCLASSCD" . $row["VALUE"], $cnt);
            $cnt++;
        } elseif ($name == "CHAIRCD") {
            knjCreateHidden($objForm, "LIST_CHAIRCD" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query = knjc035fQuery::selectSemesAll();
    $result = $db->query($query);
    $data = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month = setMonth($db, $data, $objForm, $model);
    $model->field["MONTHCD"] = $model->field["MONTHCD"] != "" ? $model->field["MONTHCD"] : getDefaultMonthCd($opt_month);

    // Add by PP for PC-Talker and current cursor 2020-01-20 start
    $extra = "id=\"MONTHCD\" onChange=\"current_cursor('MONTHCD'); btn_submit('change')\"; aria-label=\"対象月\"";
    // Add by PP for PC-Talker and current cursor 2020-01-31 end
    $arg["MONTHCD"] = knjCreateCombo($objForm, "MONTHCD", $model->field["MONTHCD"], $opt_month, $extra, 1);

    $month = preg_split("/-/", $model->field["MONTHCD"]);
    $model->field["MONTH"]    = $month[0];
    $model->field["SEMESTER"] = $month[1];

    return $data;
}

//学期・月データ取得
function setMonth($db, $data, &$objForm, $model)
{
    $cnt = 0;
    $opt_month = array();
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query = knjc035fQuery::selectMonthQuery($month, $model);
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
function getDefaultMonthCd($opt_month)
{
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
function makeLessonSet(&$objForm, &$arg, $db, &$model)
{
    if (in_array($model->cmd, array("course", "subclasscd", "chaircd", "change", "reset"))) {
        $model->field["LESSON_SET"] = "";
    }

    //入力されているMAX授業時数取得
    $query = knjc035fQuery::getInputMaxLesson($model);
    $input_lesson = $db->getOne($query);

    //課程学科
    $cm = ($model->Properties["use_school_detail_gcm_dat"] == "1") ? $model->field["COURSE_MAJOR"] : "";

    //入力されているMAX授業時数セット
    $model->field["LESSON_SET"] = ($model->field["LESSON_SET"]) ? $model->field["LESSON_SET"] : $input_lesson;

    $extra = "id= \"LESSON_SET\" aria-label=\"授業時数\" style=\"text-align: right;\" onChange=\"this.style.background='#ccffcc';\" onblur=\"tmpSet(this, 'LESSON_SET');this.value=toInteger(this.value);\"";

    //授業時数区分取得
    $query = knjc035fQuery::getJugyouJisuFlg($model, $cm);
    $jugyou_jisu_flg = $db->getOne($query);

    //レコードなし＆日々出欠なし＆法定時数のとき
    if (!$model->field["LESSON_SET"] && $model->Properties["hibiNyuuryokuNasi"] == "1" && $model->Properties["useJugyoujisuuSanshutsu"] == "1" && $jugyou_jisu_flg == "1") {
        $syusu = $credit = 0;

        //学期の最終月判定
        $query = knjc035fQuery::getMaxSemeMonthCnt($model);
        $maxMonth = $db->getOne($query);

        //単位数取得
        $query = knjc035fQuery::getCredit($model);
        $credit = $db->getOne($query);

        //月別週数を使用するとき
        if ($model->Properties["use_Month_Syusu"] == "1") {
            //月毎の週数取得
            $query = knjc035fQuery::getMonthSyusu($model, $cm);
            $syusu = $db->getOne($query);

            if ($syusu > 0 && $credit > 0) {
                $extra = "style=\"text-align: right;background-color: #ff0099;\" onblur=\"this.value=toInteger(this.value);\"";
                $model->field["LESSON_SET"] = (int)$credit * (int)$syusu;
            }

            //学期の最終月のとき
        } elseif ($maxMonth == "1") {
            //学期の週数取得
            $query = knjc035fQuery::getSyusu($model, $model->field["SEMESTER"], $cm);
            $syusu = $db->getOne($query);

            if ($syusu > 0 && $credit > 0) {
                //学期内で合算したLESSONのMAX値を取得
                $query = knjc035fQuery::getMaxSumLesson($model);
                $maxLesson = $db->getOne($query);

                $extra = "style=\"text-align: right;background-color: #ff0099;\" onblur=\"this.value=toInteger(this.value);\"";
                $cre_syu = (int)$credit * (int)$syusu;
                $model->field["LESSON_SET"] = ((int)$cre_syu - (int)$maxLesson > 0) ? (int)$cre_syu - (int)$maxLesson : "";
            }
        }
    }

    //授業時数テキスト(セット用)
    $arg["LESSON_SET"] = knjCreateTextBox($objForm, $model->field["LESSON_SET"], "LESSON_SET", 3, 3, $extra);

    //授業時数の表示項目有無チェック
    $useLesson = false;
    foreach ($model->item_array as $key => $val) {
        if ($val["item"] == "LESSON" && $val["input"] == "1") {
            $useLesson = true;
        }
    }
    knjCreateHidden($objForm, "USE_LESSON", $useLesson);

    //反映ボタン
    if ($model->field["SUBCLASSCD"] && $model->field["CHAIRCD"] && $model->field["MONTHCD"] && $useLesson) {
        $extra = "id=\"btn_reflect\" onclick=\"reflect();\" aria-label=\"反映\"";
    } else {
        $extra = "disabled";
    }
    $arg["btn_reflect"] = knjCreateBtn($objForm, "btn_reflect", "反 映", $extra);

    //クリアボタン
    if ($model->field["SUBCLASSCD"] && $model->field["CHAIRCD"] && $model->field["MONTHCD"] && $useLesson) {
        $extra = "onclick=\"lesson_clear();\"";
    } else {
        $extra = "disabled";
    }
    $extra .= " style=\"padding: 1px 5px;font-size: 80%;\"";
    $btn_lesson_clear = knjCreateBtn($objForm, "btn_lesson_clear", "ｸﾘｱ", $extra);

    return $btn_lesson_clear;
}

//締め日
function makeAppointedDay(&$objForm, &$arg, $db, &$model)
{
    if ($model->cmd == "subclasscd" || $model->cmd == "change") {
        $model->appointed_day = "";
    }

    //締め日
    $query = knjc035fQuery::getAppointedDay($model);
    $model->appointed_day = $db->getOne($query);
    $arg["APPOINTED_DAY"] = $model->appointed_day;
    knjCreateHidden($objForm, "APPOINTED_DAY", $model->appointed_day);
}

//項目名設定
function setTitleData(&$arg, $db, $model, $btn_lesson_clear)
{
    //使用する項目セット
    $setTmp = "";
    $allWidth = 450;
    foreach ($model->item_array as $key => $val) {
        $width = ($val["item"] == $model->last_field) ? "#" : "50";
        $label = "";
        for ($i = 0; $i < mb_strlen($val["label"], "UTF-8"); $i++) {
            if ($i % 2 == 0) {
                if ($i != 0) {
                    $label .= "<br>";
                }
                $label .= mb_substr($val["label"], $i, 2, "UTF-8");
            }
        }
        $setTmp .= "<td rowspan=\"2\" width=\"".$width."\">".$label;
        if ($val["item"] == "LESSON") {
            $setTmp .= "<br>".$btn_lesson_clear;
        }
        $setTmp .= "</td>";
        $allWidth += 57;
    }
    $arg["TITLE"] = $setTmp;

    //使用する項目セット（累積）
    $setRuisekiTmp = "";
    $ruiseki_cnt = 0;
    foreach ($model->itemR_array as $key => $val) {
        $width = ('RUISEKI_' . $val["item"] == $model->last_field) ? "#" : "50";
        $label = "";
        for ($i = 0; $i < mb_strlen($val["label"], "UTF-8"); $i++) {
            if ($i % 2 == 0) {
                if ($i != 0) {
                    $label .= "<br>";
                }
                $label .= mb_substr($val["label"], $i, 2, "UTF-8");
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

    //全体のサイズ
    $arg["useAllWidth"] = $allWidth;
    //締め日のサイズ
    $arg["APPOINTED_DAY_WIDTH"] = (get_count($model->item_array) > 0) ? "50" : "#";
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model)
{
    $idou_date = $sdate = "";
    if ($model->field["MONTH"]) {
        //異動対象日付
        $idou_month = sprintf('%02d', $model->field["MONTH"]);
        $idou_year = ($idou_month < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
        $appointed_day = $db->getOne(knjc035fQuery::getAppointedDay($model));
        $idou_day = ($appointed_day == "") ? getFinalDay($db, $idou_month, $model->field["SEMESTER"]) : $appointed_day;
        $idou_date = $idou_year.'-'.$idou_month.'-'.sprintf('%02d', $idou_day);

        //対象月の開始日
        $semeday = $db->getRow(knjc035fQuery::selectSemesAll($model->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
        if (sprintf('%02d', $semeday["S_MONTH"]) == $idou_month) {
            $sdate = $idou_year.'-'.$idou_month.'-'.sprintf('%02d', $semeday["S_DAY"]);
        } else {
            $sdate = $idou_year.'-'.$idou_month.'-01';
        }
    }

    $useline = array();
    $schCnt = 0;
    $textLineCnt = 0;
    $query  = knjc035fQuery::selectMeisaiQuery($model);
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
        if (isset($model->warning)) {
            foreach ($model->field["SCHREGNO"] as $key => $val) {
                if ($rowMeisai["SCHREGNO"] == $val) {
                    $rowMeisai["APPOINTED_DAY"] = $model->field["APPOINTED_DAY"][$key]; //締め日
                    $rowMeisai["LESSON"]        = $model->field["LESSON"][$key];        //授業日数
                    $rowMeisai["OFFDAYS"]       = $model->field["OFFDAYS"][$key];       //休学日数
                    $rowMeisai["ABROAD"]        = $model->field["ABROAD"][$key];        //留学日数
                    $rowMeisai["ABSENT"]        = $model->field["ABSENT"][$key];        //公欠日数
                    $rowMeisai["SUSPEND"]       = $model->field["SUSPEND"][$key];       //出停日数
                    $rowMeisai["KOUDOME"]       = $model->field["KOUDOME"][$key];       //交止
                    $rowMeisai["VIRUS"]         = $model->field["VIRUS"][$key];         //伝染病
                    $rowMeisai["MOURNING"]      = $model->field["MOURNING"][$key];      //忌引日数
                    $rowMeisai["SICK"]          = $model->field["SICK"][$key];          //病欠
                    $rowMeisai["NOTICE"]        = $model->field["NOTICE"][$key];        //事故欠届
                    $rowMeisai["NONOTICE"]      = $model->field["NONOTICE"][$key];      //事故欠無
                    $rowMeisai["NURSEOFF"]      = $model->field["NURSEOFF"][$key];      //保健室欠課
                    $rowMeisai["LATE"]          = $model->field["LATE"][$key];          //遅刻回数
                    $rowMeisai["EARLY"]         = $model->field["EARLY"][$key];         //早退回数
                }
            }
        }

        $idou = $idouText = 0;
        if ($model->field["MONTH"]) {
            //異動者（退学・転学・卒業）
            $idouData1 = $db->getRow(knjc035fQuery::getIdouData($schregno, $idou_date, $sdate), DB_FETCHMODE_ASSOC);
            $idou += (int)$idouData1["IDOU_COLOR"];
            if ($model->Properties["useIdouInputDisabled"] == "1") {
                $idouText += (int)$idouData1["IDOU_TEXT"];
            }

            //異動者（留学・休学）
            $idouData2 = $db->getRow(knjc035fQuery::getTransferData($schregno, $idou_date, $sdate), DB_FETCHMODE_ASSOC);
            $idou += (int)$idouData2["IDOU_COLOR"];
            if ($model->Properties["useIdouInputDisabled"] == "1") {
                $idouText += (int)$idouData2["IDOU_TEXT"];
            }
        }
        //異動期間は背景色を黄色にする
        $bgcolor_idou = ($idou > 0) ? "bgcolor=yellow" : $bgcolor_basic;
        $rowMeisai["BGCOLOR_IDOU"] = $bgcolor_idou;

        //欠席数が履修上限を超えた場合は背景色を赤色にする
        $risuuJyougen = "";
        if (isset($model->risyuuJyougenArray[$rowMeisai["GRADE"]."-".$rowMeisai["COURSECD"]."-".$rowMeisai["MAJORCD"]."-".$rowMeisai["COURSECODE"]])) {
            $risuuJyougen = $model->risyuuJyougenArray[$rowMeisai["GRADE"]."-".$rowMeisai["COURSECD"]."-".$rowMeisai["MAJORCD"]."-".$rowMeisai["COURSECODE"]];
        }
        $rowMeisai["BGCOLOR_TYUUI"] = ($model->Properties["useAttendLessonOverHyouji"] == "1" && $risuuJyougen && $rowMeisai["SUM_KETUJISU"] > $risuuJyougen) ? "bgcolor=#ff0099" : "";

        //移動可能行格納
        if ($idouText == "0") {
            $useline[] = $schCnt;
        }

        //編集可能データの作成
        $rowMeisai = makeTextData($objForm, $model, $rowMeisai, $schCnt, $bgcolor_idou, $idouText);
        $textLineCnt++;

        //氏名欄に学籍番号表記
        if ($model->Properties["use_SchregNo_hyoji"] == 1) {
            $rowMeisai["SCHREGNO_SHOW"] = $schregno . "　";
        }

        //データがない場合、氏名の背景色を変える
        $rowMeisai["BGCOLOR_MONTH_NAME"] = (strlen($rowMeisai["SUBCL_SCHREGNO"]) == 0) ? "bgcolor=#ccffcc" : $bgcolor_basic;
        //欠席が履修上限を上回ったら背景を赤
        $rowMeisai["BGCOLOR_MONTH_NAME"] = (strlen($rowMeisai["BGCOLOR_TYUUI"]) > 0) ? $rowMeisai["BGCOLOR_TYUUI"] : $bgcolor_basic;

        $arg["attend_data"][] = $rowMeisai;
        $schCnt++;
    }
    $result->free();

    //件数
    knjCreateHidden($objForm, "COUNTER", $schCnt);
    knjCreateHidden($objForm, "objCntSub", $textLineCnt);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    knjCreateHidden($objForm, "useLine", implode(',', $useline));
}

//編集可能データの作成
function makeTextData(&$objForm, $model, $row, $schCnt, $bgcolor_idou, $idouText)
{
    $row["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$row["SCHREGNO"]."\">";
    $row["APPOINTED_DAY"] = "<input type='hidden' name='APPOINTED_DAY[]' value='{$row["APPOINTED_DAY"]}'>{$row["APPOINTED_DAY"]}"; //締め日

    //テキスト設定
    $setArray = $setTextArray = array();
    $prefix = '';
    foreach ($model->item_array as $key => $val) {
        //入力可・不可
        $disable = ($val["input"] == "1" && $idouText == "0") ? "" : " disabled";
        $setArray[$val["item"]] =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $disable, "label" => $val["label"]);
        if ($val["input"] == "1") {
            $setTextArray[] = $val["item"];
        }
    }
    foreach ($model->itemR_array as $key => $val) {
        //入力可・不可
        $disable = ($val["input"] == "1" && $idouText == "0") ? "" : " disabled";
        $setArray['RUISEKI_' . $val["item"]] =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $disable, "label" => $val["label"]);
        if ($val["input"] == "1") {
            $setTextArray[] = $val["item"];
        }
        $prefix = 'RUISEKI_';
    }

    //ENTERキーでの移動対象項目
    $setTextField = "";
    $textSep = "";
    foreach ($setTextArray as $key) {
        $setTextField .= $textSep.$key."[]";
        $textSep = ",";
    }
    //出欠項目
    $setTmp = "";
    foreach ($setArray as $key => $val) {
        $setStyle = "";
        if ($val["DISABLED"]) {
            $setStyle = "background-color: #999999;";
        }

        //"0"表示
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $value = $row[$key];
        } else {
            $value = ($row[$key] != 0) ? $row[$key] : "";
        }
        $id = $row["HR_ATTENDNO"];
        $extra = $val["DISABLED"]." aria-label=\"".$row["HR_ATTENDNO"]."".$row["NAME"]."の".$val["label"]." \" id= \"$id-$key\" STYLE=\"text-align: right; {$setStyle}\" onblur=\"tmpSet(this, '$id-$key');this.value=toInteger(this.value)\"; onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$schCnt})\"; onPaste=\"return showPaste(this, ".$schCnt.");\" ";
        $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);

        //順にセット
        $width = ($key == $model->last_field) ? "#" : "50";

        $bgcolor_kesseki =  (in_array($key, $model->kessekiItem) && strlen($row["BGCOLOR_TYUUI"]) != 0) ? $row["BGCOLOR_TYUUI"] : "";
        $setTmp .= "<td width=\"".$width."\" {$bgcolor_kesseki} {$bgcolor_idou}>".$row[$key]."</td>";
    }
    $schCnt++;
    $row["ATTEND_DATA"] = $setTmp;

    return $row;
}

//最終日取得
function getFinalDay($db, $month, $semester)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime(0, 0, 0, $month, 1, $year));
    $semeday = $db->getRow(knjc035fQuery::selectSemesAll($semester), DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month && $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }

    return $lastday;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    //保存ボタン
    if ($model->field["SUBCLASSCD"] && $model->field["CHAIRCD"] && $model->field["MONTHCD"]) {
        $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('update');\" aria-label=\"更新\"";
    } else {
        $extra = "disabled";
    }
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('reset');\" aria-label=\"取消\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $setEndTitle = $model->getPrgId ? "戻 る" : "終 了";
    $extra = $model->getPrgId ? "onclick=\"closeFunc();\" aria-label=\"戻る\"" : "onclick=\"closeWin();\" aria-label=\"終了\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", $setEndTitle, $extra);

    //セキュリティーチェック
    $securityCnt = $db->getOne(knjc035fQuery::getSecurityHigh());
    $csvSetName = "ＣＳＶ処理";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル処理";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        //ＣＳＶ処理ボタン
        //コールされたら、ボタン非表示
        if (!$model->getPrgId) {
            $extra = "id= \"btn_csv\" onClick=\" current_cursor('btn_csv');  wopen('".REQUESTROOT."/X/KNJX_C035F/knjx_c035findex.php?SEND_PRGID=KNJC035F&SEND_AUTH={$model->auth}&PROGRAMID=KNJC035F&SEND_SCHOOL_KIND={$model->field["SCHOOL_KIND"]}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_CHAIRCD={$model->field["CHAIRCD"]}&SEND_MONTHCD={$model->field["MONTHCD"]}','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $csvSetName, $extra);
        }
    }
}
