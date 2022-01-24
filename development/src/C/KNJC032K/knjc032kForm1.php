<?php

require_once('for_php7.php');

class knjc032kForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc032kindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = $model->ctrl_year;

        /* 学期 */
        $query = knjc032kQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        /* 対象学級 */
        $query = knjc032kQuery::selectHrClass($model);
        $extra = "onChange=\"btn_submit('change_class')\";";
        makeCmb($objForm, $arg, $db, $query, "hr_class", "HR_CLASS", $model->field["hr_class"], $extra, 1, "BLANK");

        /* 対象生徒 */
        $query      = knjc032kQuery::selectStudent($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, "schregno",  "SCHREGNO", $model->field["schregno"], $extra, 1, "BLANK");

        /* タイトル設定 */
        $model->titleVal = setTitleData($objForm, $arg, $db, $model);

        /* 可変サイズ */
        $gedanCol = 15;
        /* ウイルスあり */
        if ($model->Properties["useVirus"] == "true") {
            $arg["useVirus"] = "1";
            $gedanCol = $gedanCol + 1;
        }
        /* 交止あり */
        $arg["SUSPEND_NAME"] = "";
        if ($model->Properties["useKoudome"] == "true") {
            $arg["useKoudome"] = "1";
            $arg["SUSPEND_NAME"] = "法止";
            $gedanCol = $gedanCol + 1;
        }
        $arg["useGedanCol"] = $gedanCol;

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model);

        //学校マスタ
        $schoolMst = array();
        $query = knjc032kQuery::getSchoolMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $schoolMst[$key] = $val;
            }
        }
        $result->free();

        /* 累積 */
        $query = knjc032kQuery::selectAttendSemester($model,0,$schoolMst);
        $arg["sum_attend"] = $db->getRow($query);
        /* ボタン作成 */
        makeButton($objForm, $arg);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "unUseOffdays", $model->Properties["unUseOffdays"]);
        knjCreateHidden($objForm, "unUseAbroad", $model->Properties["unUseAbroad"]);
        knjCreateHidden($objForm, "unUseAbsent", $model->Properties["unUseAbsent"]);
        knjCreateHidden($objForm, "changeVal", 0);
        knjCreateHidden($objForm, "HIDDEN_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_SCHREGNO");

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjc032kForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $name2, $value, $extra, $size, $blank = "")
{
    $opt = array();
    //先頭に空リストをセット
    if ($blank == "BLANK") {
        $opt[]  = array("label" => "", "value" => "");
    }

    $result = $db->query($query);
    while($row  = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();
    $arg[$name] = createCombo($objForm, $name2, $value, $opt, $extra, $size);
}

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, $model)
{
    $rtnTitle = array();
    $result = $db->query(knjc032kQuery::getSickDiv());
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
function makeDataList(&$objForm, &$arg, $db, $model) {
    $query      = knjc032kQuery::selectSemesAll();
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    //学校マスタ
    $schoolMst = array();
    $query = knjc032kQuery::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }
    $result->free();

    //異動月取得
    $smonth = $emonth = array();
    $smonth = $db->getCol(knjc032kQuery::getTransferData2($model->field["schregno"], "s"));
    $emonth = $db->getCol(knjc032kQuery::getTransferData2($model->field["schregno"], "e"));

    $monthCnt = 0;
    $textLineCnt = 0;
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query  = knjc032kQuery::selectAttendQuery($model, $month, $data[$dcnt]["SEMESTER"], $schoolMst);
            $result = $db->query($query);
            $rowMeisai = array();
            while ($rowMeisai = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($rowMeisai["CONTROL_CODE"] == $rowMeisai["MONTH"]) {
                    if (!isset($model->warning)) {
                        $rowMeisai = makeTextData($model, $rowMeisai, $data[$dcnt]["SEMESTER"], $monthCnt);
                        $textLineCnt++;
                    } else {
                        $rowMeisai = makeErrTextData($model, $rowMeisai["MONTH"], $data[$dcnt]["SEMESTER"], $rowMeisai, $monthCnt);
                    }
                } else {
                    $rowMeisai["MONTH"] = "";
                    //データ未入力の月は背景色を変える
                    if (strlen($rowMeisai["SEM_SCHREGNO"]) == 0) {
                        $rowMeisai["BGCOLOR_MONTH_NAME"] = "bgcolor=#ccffcc";
                    }
                }

                //異動者（退学・転学・卒業）
                $idou_year = ($month < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
                $appointed_day = $db->getOne(knjc032kQuery::getAppointedDay(sprintf('%02d', $month), $data[$dcnt]["SEMESTER"], $model));
                $idou_day = ($appointed_day == "") ? getFinalDay($db, sprintf('%02d', $month), $data[$dcnt]["SEMESTER"]) : $appointed_day;
                $idou_date = $idou_year.'-'.sprintf('%02d', $month).'-'.$idou_day;
                $idou = $db->getOne(knjc032kQuery::getIdouData($model->field["schregno"], $idou_date));

                //異動者（留学・休学）
                $idou2 = $db->getOne(knjc032kQuery::getTransferData1($model->field["schregno"], $idou_date));
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
        $query = knjc032kQuery::selectAttendSemester($model, $data[$dcnt]["SEMESTER"], $schoolMst);
        $result2 = $db->query($query);
        $rowsemes = array();
        while ($rowsemes = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$model->field["schregno"]) {
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
function makeErrTextData($model, $month, $seme, $meisai, $monthCnt) {
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
                $meisai["KOUDOME"]       = $model->field["KOUDOME"][$key];   //交止
            }
            if ($model->Properties["useVirus"] == "true") {
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
    $row = makeTextData($model, $meisai, $seme, $monthCnt);
    return $row;
}

//編集可能データの作成
function makeTextData($model, $row, $seme, $monthCnt)
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

    //データ未入力の月は背景色を変える
    if (strlen($row["SEM_SCHREGNO"]) == 0) {
        $row["BGCOLOR_MONTH_NAME"] = "bgcolor=#ccffcc";
    }

    foreach ($setArray as $key => $val) {
        $setStyle = "";
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
function getFinalDay($db, $month, $semester)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc032kQuery::selectSemesAll($semester),DB_FETCHMODE_ASSOC);
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
    $arg["btn_update"] = createBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeMsg();\"");
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
}

?>
