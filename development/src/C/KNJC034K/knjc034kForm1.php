<?php

require_once('for_php7.php');

class knjc034kForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc034kindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = CTRL_YEAR;

        /* 学期 */
        $query = knjc034kQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        //科目コンボ
        $query = knjc034kQuery::selectSubclassQuery($model);
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $model->field["CHAIRCD"] = ($model->cmd == "subclasscd") ? "" : $model->field["CHAIRCD"];
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //講座コンボ
        $query = knjc034kQuery::selectChairQuery($model);
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $model->field["SCHREGNO"] = ($model->cmd == "chaircd") ? "" : $model->field["SCHREGNO"];
        makeCmb($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", $extra, 1, "BLANK");

        /* 対象生徒 */
        $query      = knjc034kQuery::selectStudent($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHREGNO"], "SCHREGNO", $extra, 1, "BLANK");

        //学年取得
        $query = knjc034kQuery::getStudentGrade($model);
        $schGrade = $db->getOne($query);
        knjCreateHidden($objForm, "schGrade", $schGrade);

        /* タイトル設定 */
        $model->titleVal = setTitleData($objForm, $arg, $db, $model);

        /* 可変サイズ */
        $gedanCol = 13;
        /* ウイルスあり */
        if ($model->Properties["useVirus"] == "true") {
            $arg["useVirus"] = "1";
            $gedanCol = $gedanCol + 1;
        }
        /* 交止あり */
        $arg["SUSPEND_NAME"] = "時数";
        if ($model->Properties["useKoudome"] == "true") {
            $arg["useKoudome"] = "1";
            $arg["SUSPEND_NAME"] = "法止<br>時数";
            $gedanCol = $gedanCol + 1;
        }
        $arg["useGedanCol"] = $gedanCol;

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $schGrade);

        /* 累積 */
        $model->isTitle4 = $arg['title4'];
        $model->isTitle5 = $arg['title5'];
        $model->isTitle6 = $arg['title6'];
        $query = knjc034kQuery::selectAttendSemester($model, 0, $schGrade);
        $arg["sum_attend"] = $db->getRow($query);
        /* ボタン作成 */
        makeButton($objForm, $arg);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
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

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjc034kForm1.html", $arg);
    }
}

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

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, $model)
{
    $rtnTitle = array();
    $result = $db->query(knjc034kQuery::getSickDiv());
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
function makeDataList(&$objForm, &$arg, $db, $model, $schGrade) {
    $query      = knjc034kQuery::selectSemesAll($model, $schGrade);
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }

    //異動月取得
    $smonth = $emonth = array();
    $smonth = $db->getCol(knjc034kQuery::getTransferData2($model->field["SCHREGNO"], "s"));
    $emonth = $db->getCol(knjc034kQuery::getTransferData2($model->field["SCHREGNO"], "e"));

    $result->free();
    $monthCnt = 0;
    $textLineCnt = 0;
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query  = knjc034kQuery::selectAttendQuery($model, $month, $data[$dcnt]["SEMESTER"], $schGrade);
            $result = $db->query($query);
            $rowMeisai = array();
            while ($rowMeisai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $extra = "";
                if ($rowMeisai["CONTROL_CODE"] == $rowMeisai["MONTH"]) {

                    $query = knjc034kQuery::getMaxSemeMonthCnt($model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"], $schGrade);
                    $maxMonth = $db->getOne($query);
                    if (strlen($rowMeisai["LESSON"]) == 0 && $maxMonth == "1") {
                        $query = knjc034kQuery::getSyusu($data[$dcnt]["SEMESTER"]);
                        $syusu = $db->getOne($query);
                        $query = knjc034kQuery::getCourse($model->field["SCHREGNO"], $data[$dcnt]["SEMESTER"]);
                        $schCourse = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $query = knjc034kQuery::getCredit($schCourse, $model);
                        $credit = $db->getOne($query);
                        if ($syusu > 0 && $credit > 0 && $model->Properties["hibiNyuuryokuNasi"] == "1") {
                            //学期内で合算したLESSONを取得
                            $query = knjc034kQuery::getSumLesson($model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"]);
                            $sumLesson = $db->getOne($query);

                            $extra = " background-color : #ff0099 ";
                            $cre_syu = $credit * $syusu;
                            $rowMeisai["LESSON"] = ($cre_syu - $sumLesson > 0) ? $cre_syu - $sumLesson : "";
                        }
                    }
                    if (!isset($model->warning)) {
                        $rowMeisai = makeTextData($rowMeisai, $data[$dcnt]["SEMESTER"], $monthCnt, $extra, $model);
                        $textLineCnt++;
                    } else {
                        $rowMeisai = makeErrTextData($model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"], $rowMeisai, $monthCnt, $extra);
                    }
                } else {
                    $rowMeisai["MONTH"] = "";
                }

                //未入力月の背景色を変更
                if (strlen($rowMeisai["SUB_SCHREGNO"]) == 0) {
                    $rowMeisai["BGCOLOR_MONTH_NAME"] = "bgcolor=#ccffcc";
                }

                //異動者（退学・転学・卒業）
                $idou_year = ($month < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
                $appointed_day = $db->getOne(knjc034kQuery::getAppointedDay(sprintf('%02d', $month), $data[$dcnt]["SEMESTER"], $schGrade));
                $idou_day = ($appointed_day == "") ? getFinalDay($db, $model, sprintf('%02d', $month), $data[$dcnt]["SEMESTER"], $schGrade) : $appointed_day;
                $idou_date = $idou_year.'-'.sprintf('%02d', $month).'-'.$idou_day;
                $idou = $db->getOne(knjc034kQuery::getIdouData($model->field["SCHREGNO"], $idou_date));

                //異動者（留学・休学）
                $idou2 = $db->getOne(knjc034kQuery::getTransferData1($model->field["SCHREGNO"], $idou_date));
                $idou3 = 0;
                if (in_array(sprintf('%02d', $month), $smonth) || in_array(sprintf('%02d', $month), $emonth)) {
                    $idou3 = 1;
                }

                //異動期間は背景色を黄色にする
                $rowMeisai["BGCOLOR_IDOU"] = ($idou > 0 || $idou2 > 0 || $idou3 > 0) ? "bgcolor=yellow" : "";

                $arg["attend_data".$data[$dcnt]["SEMESTER"]][] = $rowMeisai;
            }
            $result->free();
            $monthCnt++;
        }
        /* 学期計 */
        $query = knjc034kQuery::selectAttendSemester($model, $data[$dcnt]["SEMESTER"], $schGrade);
        $result2 = $db->query($query);
        $rowsemes = array();
        while ($rowsemes = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$model->field["SCHREGNO"]) {
                $rowsemes["MONTH_NAME"]    = $db->getOne($query);;
            }
            $arg["sum_semester".$data[$dcnt]["SEMESTER"]][] = $rowsemes;
        }
        $result2->free();
    }
    knjCreateHidden($objForm, "objCntSub", $textLineCnt);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//エラー時の編集可能データ
function makeErrTextData($model, $month, $seme, $meisai, $monthCnt, $extra)
{
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
            if ($model->Properties["useKoudome"] == "true") {
                $meisai["KOUDOME"]     = $model->field["KOUDOME"][$key];     //交止
            }
            if ($model->Properties["useVirus"] == "true") {
                $meisai["VIRUS"]       = $model->field["VIRUS"][$key];       //伝染病
            }
            $meisai["MOURNING"]      = $model->field["MOURNING"][$key];      //忌引日数
            /* C001 */
            foreach ($model->titleVal as $key => $val) {
                $meisai[$model->c001[$key]]          = $model->field[$model->c001[$key]][$key];
            }
            $meisai["LATE"]          = $model->field["LATE"][$key];          //遅刻回数
            $meisai["EARLY"]         = $model->field["EARLY"][$key];         //早退回数
        }

    }
    $row = makeTextData($meisai, $seme, $monthCnt, $extra, $model);
    return $row;
}

//編集可能データの作成
function makeTextData($row, $seme, $monthCnt, $extra, $model)
{
    $row["MONTH"]         = "<input type=\"hidden\" name=\"MONTH[]\" value=\"".$row["MONTH"]."-".$seme."\">";
    $row["MONTH_NAME"]    = $row["MONTH_NAME"];
    $row["APPOINTED_DAY"] = "<input type='hidden' name='APPOINTED_DAY[]' value='{$row["APPOINTED_DAY"]}'>{$row["APPOINTED_DAY"]}"; //締め日

    $setArray = array();
    $setArray["LESSON"]     =  array("SIZE" => 2, "MAXLEN" => 3);
    $setArray["OFFDAYS"]    =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseOffdays"] == "true" ? " disabled " : "");
    $setArray["ABROAD"]     =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseAbroad"] == "true" ? " disabled " : "");
    $setArray["ABSENT"]     =  array("SIZE" => 2, "MAXLEN" => 3, "DISABLED" => $model->Properties["unUseAbsent"] == "true" ? " disabled " : "");
    $setArray["SUSPEND"]    =  array("SIZE" => 2, "MAXLEN" => 3);
    if ($model->Properties["useKoudome"] == "true") {
        $setArray["KOUDOME"]  =  array("SIZE" => 2, "MAXLEN" => 3);
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

    foreach ($setArray as $key => $val) {
        $setStyle = "";
        if ($key == "LESSON" && $extra) {
            $setStyle = $extra;
        }
        if ($val["DISABLED"]) {
            $setStyle = " background-color : #999999 ";
        }
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $value = $row[$key];
        } else {
            $value = ($row[$key] != 0) ? $row[$key] : "";
        }
        $extra = $val["DISABLED"]." STYLE=\"text-align: right; {$setStyle}\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onblur=\"this.value=toInteger(this.value)\"; onKeyDown=\"keyChangeEntToTab(this)\"; onPaste=\"return showPaste(this, ".$monthCnt.");\" ";
        $row[$key] = createTextBox($value, $key."[]", $val["SIZE"], $val["MAXLEN"], $monthCnt, $extra);
    }

    return $row;
}

//最終日取得
function getFinalDay($db, $model, $month, $semester, $schGrade)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc034kQuery::selectSemesAll($model, $schGrade, $semester),DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month &&
        $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }
    return $lastday;
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

/**** テキストボックスを作成する関数    ****
 **** $data … テキストボックスに入る値 ****
 **** $name … テキストボックスの名称   ****/
function createTextBox($data, $name, $size, $maxlen, $monthCnt, $extra){
    $objForm = new form;
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => $extra) );
    return $objForm->ge($name);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //保存ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeMsg();\"");
}

?>
