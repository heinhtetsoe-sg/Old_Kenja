<?php

require_once('for_php7.php');


//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjc031cForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        /* CSV */
        $objUp = new csvFile();

        $arg["start"]   = $objForm->get_start("main", "POST", "knjc031cindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = CTRL_YEAR;

        /* 学期 */
        $query = knjc031cQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        /* 対象学級 */
        $hrName = makeHrclassCmb($objForm, $arg, $db, $model);
        
        $model->school_kind = $db->getOne(knjc031cQuery::getSchoolKind($model));

        /* 対象月 */
        $monthName = makeMonthSemeCmb($objForm, $arg, $db, $model);

        /* タイトル設定 */
        $kahenField = setTitleData($objForm, $arg, $db, $model);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $objUp, $header, $hrName, $monthName, $kahenField);

        /* ボタン作成 */
        makeButton($objForm, $arg, $db, $model);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC031C");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjc031cForm1.html", $arg);
    }
}

//対象学級コンボ作成
function makeHrclassCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc031cQuery::selectHrClass($model);
    $result     = $db->query($query);
    $opt_hr     = array();

    /* 2004/08/27 arakaki 近大-作業依頼書20040824.doc */
    $opt_hr[] = array("label" => "",
                      "value" => "");

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
    }
    $arg["hr_class"] = knjCreateCombo($objForm, "HR_CLASS", $model->field["hr_class"], $opt_hr, "onChange=\"btn_submit('change')\";", 1);
    $rtnHrname = "";
    for ($i = 0; $i < get_count($opt_hr); $i++) {
        $rtnHrname = ($opt_hr[$i]["value"] == $model->field["hr_class"]) ? $opt_hr[$i]["label"] : $rtnHrname;
    }
    return $rtnHrname;
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc031cQuery::selectSemesAll();
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month  = array();
    $opt_month[] = array("label" => "",
                         "value" => "");

    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $getdata = $db->getRow(knjc031cQuery::selectMonthQuery($month, $model), DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
            }
        }
    }
    if($model->field["month"] == "" || $model->field["month"] == NULL){
        $model->field["month"] = "";
    }
    $arg["month"] = knjCreateCombo($objForm, "MONTH", $model->field["month"], $opt_month, "onChange=\"btn_submit('change')\";", 1);

    $rtnMonth = "";
    for ($i = 0; $i < get_count($opt_month); $i++) {
        $rtnMonth = ($opt_month[$i]["value"] == $model->field["month"]) ? $opt_month[$i]["label"] : $rtnMonth;
    }
    return $rtnMonth;
}

//タイトル設定
function setTitleData(&$objForm, &$arg, $db, &$model)
{
    $setFieldName = array("4" => "SICK[]", "5" => "NOTICE[]", "6" => "NONOTICE[]");
    $result = $db->query(knjc031cQuery::getSickDiv());
    $setFieldData = "";
    $sep = "";
    $attendCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnField["C001"][] = $row["VALUE"];
        $setFieldData .= $sep.$setFieldName[$row["VALUE"]];
        $sep = ",";
        $attendCnt++;
    }
    $result->free();
    //hidden
    knjCreateHidden($objForm, "SET_FIELD", $setFieldData);

    $result = $db->query(knjc031cQuery::getDetailDiv());
    $detailCnt = 0;
    $setFieldData = "";
    $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["DETAIL_TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnField["C002"][] = $row["VALUE"];
        $setFieldData .= $sep."DETAIL_".$row["VALUE"]."[]";
        $sep = ",";
        $detailCnt++;
    }
    $result->free();
    //hidden
    knjCreateHidden($objForm, "SET_DETAIL_FIELD", $setFieldData);

    //出停
    $suspendCnt = 0;
    $setSuspendData = "";
    $sep = "";
    $arg["TITLE_SUSPEND"] = '出停';
    if ($model->Properties["useKoudome"] == "true") {
        $arg["TITLE_SUSPEND"] = '法止';
        $setSuspendData .= "KOUDOME[]";
        $sep = ",";
        $suspendCnt++;
    }
    if ($model->Properties["useVirus"] == "true") {
        $setSuspendData .= $sep."VIRUS[]";
        $suspendCnt++;
    }

    $arg["DATA_COLSPAN"] = 13 + $attendCnt + $detailCnt + $suspendCnt;
    $arg["RUIKEI_COLSPAN"] = 8 + $detailCnt + $suspendCnt;
    $arg["DATA_WIDTH"] = (128 + $suspendCnt * 9)."%";

    //hidden
    knjCreateHidden($objForm, "SET_SUSPEND_FIELD", $setSuspendData);

    return $rtnField;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $headerData, $hrName, $monthName, $kahenField)
{
    $tukiAndGakki = explode("-", $model->field["month"]);
    $tuki  = $tukiAndGakki[0];
    $gakki = $tukiAndGakki[1];
    $query = knjc031cQuery::getAppointedDay($tuki, $gakki, $model);
    $appointed_day = $db->getOne($query);

    $schoolMst = array();
    $query = knjc031cQuery::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }

    $query      = knjc031cQuery::selectAttendQuery($model, $schoolMst);
    $result     = $db->query($query);

    $monthsem = array();
    $monthsem = preg_split("/-/", $model->field["month"]);

    knjCreateHidden($objForm, "objCntSub", get_count($db->getCol($query)));

    $counter  = 0;
    $colorFlg = false;
    $data = array();
    $schCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        /* 出欠月別累積データ */
        if (isset($model->warning)) {
            if (is_array($model->reset[$row["SCHREGNO"]])) {
                foreach ($model->reset[$row["SCHREGNO"]] as $key => $val) {
                    $row[$key] = $val;
                }
            }
        }

        $setArray = array("LESSON"      => array("SIZE" => 2, "MAXLEN" => 3),               //授業日数
                          "OFFDAYS"     => array("SIZE" => 2, "MAXLEN" => 3),
                          "ABROAD"      => array("SIZE" => 2, "MAXLEN" => 3),
                          "ABSENT"      => array("SIZE" => 2, "MAXLEN" => 3),
                          "SUSPEND"     => array("SIZE" => 2, "MAXLEN" => 3),
                          "MOURNING"    => array("SIZE" => 2, "MAXLEN" => 3),
                          "LATE"        => array("SIZE" => 2, "MAXLEN" => 3),
                          "EARLY"       => array("SIZE" => 2, "MAXLEN" => 3)
                          );
        if ($model->Properties["useKoudome"] == "true") {
            $setArray["KOUDOME"] = array("SIZE" => 2, "MAXLEN" => 3);
        }
        if ($model->Properties["useVirus"] == "true") {
            $setArray["VIRUS"] = array("SIZE" => 2, "MAXLEN" => 3);
        }
        //C001可変
        $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
        $kahenField["C001"] = is_array($kahenField["C001"]) ? $kahenField["C001"] : array();
        foreach ($kahenField["C001"] as $c001Key => $c001Val) {
            $setArray[$setFieldName[$c001Val]] = array("SIZE" => 2, "MAXLEN" => 3);
        }

        //C002可変
        $kahenField["C002"] = is_array($kahenField["C002"]) ? $kahenField["C002"] : array();
        foreach ($kahenField["C002"] as $c002Key => $c002Val) {
            $setArray["DETAIL_".$c002Val] = array("SIZE" => 2, "MAXLEN" => 3);
        }

        foreach ($setArray as $key => $val) {
            //textbox
            $extra = "STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"; onPaste=\"return showPaste(this, ".$schCnt.");\"";
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                $value = $row[$key];
            } else {
                $value = ($row[$key] != 0) ?  $row[$key] : "";
            }
            $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);
        }
        $schCnt++;

        //異動者（退学・転学・卒業）
        $idou_year = ($monthsem[0] < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
        $idou_day = ($appointed_day == "") ? getFinalDay($db, $monthsem[0], $monthsem[1]) : $appointed_day;
        $idou_date = $idou_year.'-'.$monthsem[0].'-'.$idou_day;
        $idou = $db->getOne(knjc031cQuery::getIdouData($row["SCHREGNO"], $idou_date));

        //異動月取得
        $smonth = $emonth = array();
        $smonth = $db->getCol(knjc031cQuery::getTransferData2($row["SCHREGNO"], "s"));
        $emonth = $db->getCol(knjc031cQuery::getTransferData2($row["SCHREGNO"], "e"));

        //異動者（留学・休学）
        $idou2 = $db->getOne(knjc031cQuery::getTransferData1($row["SCHREGNO"], $idou_date));
        $idou3 = 0;
        if (in_array(sprintf('%02d', $monthsem[0]), $smonth) || in_array(sprintf('%02d', $monthsem[0]), $emonth)) {
            $idou3 = 1;
        }

        //異動期間は背景色を黄色にする
        $row["BGCOLOR_IDOU"] = ($idou > 0 || $idou2 > 0 || $idou3 > 0) ? "bgcolor=yellow" : "";

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

    $arg["SET_APPOINTED_DAY"] = $appointed_day;
    //hidden
    knjCreateHidden($objForm, "SET_APPOINTED_DAY", $appointed_day);


    $suspect_cols = 2;

    if ($model->Properties["useVirus"] == "true") {
        $arg["useVirus"] = 1;
        $suspect_cols++;
    }
    if ($model->Properties["useKoudome"] == "true") {
        $arg["useKoudome"] = 1;
        $suspect_cols++;
    }

    $arg["SUSPECT_COLS"] = $suspect_cols;
    $arg["SUSPECT_WIDTH"] = $suspect_cols * 35;
}

//最終日取得
function getFinalDay($db, $month, $semester)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc031cQuery::selectSemesAll($semester),DB_FETCHMODE_ASSOC);
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
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    //セキュリティーチェック
    $securityCnt = $db->getOne(knjc031cQuery::getSecurityHigh());
    $csvSetName = "ＣＳＶ処理";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル処理";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        //ＣＳＶ処理ボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_C031C/knjx_c031cindex.php?SEND_PRGID=KNJC031C&SEND_AUTH={$model->auth}&SEND_hr_class={$model->field["hr_class"]}&SEND_month={$model->field["month"]}&selectSchoolKind={$model->selectSchoolKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $csvSetName, $extra);
    }
}

?>
