<?php

require_once('for_php7.php');

class knjc031dForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjc031dindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["year"] = CTRL_YEAR;

        /* 学期 */
        $query = knjc031dQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        if ($model->Properties["useFi_Hrclass"] == "1" || $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            //クラス方式選択 (1:法定クラス 2:複式クラス/実クラス)
            $opt = array(1, 2);
            if ($model->field["HR_CLASS_TYPE"] == "") $model->field["HR_CLASS_TYPE"] = "1";
            $click = " onClick=\"return btn_submit('change');\"";
            $extra = array("id=\"HR_CLASS_TYPE1\"".$click, "id=\"HR_CLASS_TYPE2\"".$click);
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
            $arg["data"]["HR_CLASS_TYPE2_LABEL"] = ($model->Properties["useFi_Hrclass"] == "1") ? '複式クラス' : '実クラス';
            $arg["useHukusiki"] = 1;
        } else {
            if ($model->field["HR_CLASS_TYPE"] == "") $model->field["HR_CLASS_TYPE"] = "1";
            knjCreateHidden($objForm, "HR_CLASS_TYPE", "1");
        }

        /* 対象学級 */
        $hrName = makeHrclassCmb($objForm, $arg, $db, $model);

        /* 対象月 */
        $monthName = makeMonthSemeCmb($objForm, $arg, $db, $model);

        //リンク先のURL
        $jump = REQUESTROOT."/C/KNJC031D_2/knjc031d_2index.php";

        //締め日取得
        list($tuki, $gakki) = explode('-', $model->field["month"]);
        $query = knjc031dQuery::getAppointedDay($tuki, $gakki, $model);
        $appointed_day = $db->getOne($query);

        //出欠詳細コードの取得
        $sub_data = array();
        $sub_data_cnt = array();

        //出欠入力対象の項目
        $setFieldName[] =  array("1",   "ABSENT",   "公欠日数", "col");
        $setFieldName[] =  array("2",   "SUSPEND",  "出停",     "row");
        $setFieldName[] =  array("3",   "MOURNING", "忌引",     "row");
        $setFieldName[] =  array("4",   "SICK",     "",         "row");
        $setFieldName[] =  array("5",   "NOTICE",   "",         "row");
        $setFieldName[] =  array("6",   "NONOTICE", "",         "row");
        $setFieldName[] =  array("15",  "LATE",     "遅刻",     "col");
        $setFieldName[] =  array("16",  "EARLY",    "早退",     "col");

        $tmpItem = array();
        $c001 = ($model->schoolkind) ? 'C'.$model->schoolkind.'01' : 'C001';
        $result = $db->query(knjc031dQuery::getNameMst2($c001));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($setFieldName as $order => $val) {
                list ($cd, $field, $label, $show) = $val;

                if ($cd == $row["NAMECD2"]) {
                    $name = (strlen($label)) ? $label : $row["NAME1"];
                    $tmpItem[sprintf("%03d", $order)."000"][$field] = array($cd, $name, $show);

                    if ($cd == "6") {
                        $result006 = $db->query(knjc031dQuery::getNameMst2('C006'));
                        while ($row006 = $result006->fetchRow(DB_FETCHMODE_ASSOC)) {
                            if ($row006["NAMESPARE1"] == $cd) {
                                $tmpItem[sprintf("%03d", $order).$row006["NAMECD2"]]["CNT".$row006["NAMECD2"]] = array($row006["NAMECD2"], $row006["NAME1"], "row");
                            }
                        }
                        $result006->free();
                    }
                }
            }
        }
        $result->free();

        ksort($tmpItem);

        $attendItem = array();
        foreach ($tmpItem as $order => $array) {
            foreach ($array as $field => $val) {
                $attendItem[$field] = $val;
            }
        }

        //詳細画面に渡すパラメータ
        $prgid  = "KNJC031D";
        $auth   = AUTHORITY;

        $param  = "?prgid={$prgid}";
        $param .= "&auth={$auth}";
        $param .= "&HR_CLASS_TYPE={$model->field["HR_CLASS_TYPE"]}";
        $param .= "&GRADE={$model->field["grade"]}";
        $param .= "&HR_CLASS={$model->field["class"]}";
        $param .= "&MONTH={$model->field["month"]}";
        $param .= "&APPOINTED_DAY={$appointed_day}";

        $notUpdateItem = "";
        $SUSPEND = $MOURNING = false;
        //出欠項目名表示
        foreach ($attendItem as $key => $val) {

            $title = "";
            if ($val[2] == "col") {   //縦書
                for ($i = 1; $i < 5; $i++) {
                    $title .= ($i == "1") ? "" : "<br>";
                    $title .= substr($val[1],($i-1)*3,3);
                }
            } else {
                $title = $val[1];
            }

            //詳細入力画面に渡すパラメータ（項目名）
            $param .= "&TITLE={$key}";

            //詳細出欠項目がある場合はリンクにする
            $sub_exist_check = "0";
            $extra = "style=\"font-color:white;\" onClick=\"openSubWindow('{$jump}{$param}');\"";
            $sub_exist_check = get_count($db->getCol(knjc031dQuery::getNameMst('C006', $val[0])));
            if ($sub_exist_check > "0" && $model->field["grade"] && $model->field["class"] && $model->field["month"] && $key != "NONOTICE") {
                $arg[$key] = View::alink("#", "<font color=\"hotpink\">".$title."</font>", $extra);
                $notUpdateItem .= ($notUpdateItem) ? ','.$key : $key;
            } else {
                $arg[$key] = $title;
            }

            if (in_array($key, array("SUSPEND", "MOURNING"))) $$key = true;

        }
        knjCreateHidden($objForm, "NOT_UPDATE_ITEM", $notUpdateItem);

        //出停・忌引の表示切替
        if ($SUSPEND && $MOURNING) {
            $arg["SUS_MOUR"] = 1;
            $arg["NOT_SUS_MOUR"] = "";
        } else {
            $arg["SUS_MOUR"] = "";
            $arg["NOT_SUS_MOUR"] = 1;
        }

        //欠席の詳細出欠項目名（大分類）を表示
        $subl_title = "";
        $subl_cnt = "0";
        $result = $db->query(knjc031dQuery::getNameMst('C006','6'));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //詳細入力画面に渡すパラメータ（項目名）
            $param .= "&TITLE=CNT{$row["VALUE"]}";

            //詳細出欠項目がある場合はリンクにする
            $sub_exist_check = "0";
            $extra = "style=\"font-color:white;\" onClick=\"openSubWindow('{$jump}{$param}');\"";
            $sub_exist_check = get_count($db->getCol(knjc031dQuery::getNameMst('C007', $row["VALUE"])));
            if ($sub_exist_check > "0" && $model->field["grade"] && $model->field["class"] && $model->field["month"]) {
                $label = View::alink("#", "<font color=\"hotpink\">".$row["LABEL"]."</font>", $extra);
                $subl_title .= "<td rowspan=\"2\" width=\"30\"><font size=\"1\">".$label."</font></td>";
            } else {
                $subl_title .= "<td rowspan=\"2\" width=\"30\"><font size=\"1\">".$row["LABEL"]."</font></td>";
            }
            $subl_cnt++;
        }
        $result->free();

        //事故欠（無）があるとき、大分類を表示する
        $sickdiv = array();
        $sickdiv = $db->getCol(knjc031dQuery::getSickDiv($c001));
        if (in_array('6', $sickdiv)) {
            $arg["subl_title"] = $subl_title;
            $arg["subl_title_cnt"] = $subl_cnt;
        }

        //貼り付け対象項目
        $lateFlg = $earlyFlg = false;
        $attendCnt = 0;
        foreach ($attendItem as $attKey => $attVal) {
            $sub_exist_check = "0";
            $namecd1 = (substr($attKey,0,3) == "CNT") ? 'C007' : 'C006';
            $sub_exist_check = get_count($db->getCol(knjc031dQuery::getNameMst($namecd1, $attVal[0])));
            if($sub_exist_check == "0") {
                $setFieldData .= $sep.$attKey."[]";
                $sep = ",";
            }

            $attendCnt++;

            if ($attKey == "LATE")  $lateFlg = true;
            if ($attKey == "EARLY") $earlyFlg = true;
        }
        //hidden
        knjCreateHidden($objForm, "SET_FIELD", $setFieldData);

        //C002
        $setFieldC002 = "";
        $sep = "";
        $c002Data = false;
        $query = knjc031dQuery::getDetailDiv();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setFieldC002 .= $sep."DETAIL_".$row["VALUE"]."[]";
            $sep = ",";
            $c002Data = true;
        }
        $result->free();
        //hidden
        knjCreateHidden($objForm, "SET_FIELD_C002", $setFieldC002);

        //幅設定
        $arg["CLASSDAYS3_WIDTH"]    = ($c002Data || $earlyFlg || $lateFlg) ? "30" : "#";
        $arg["LATE_WIDTH"]          = ($c002Data || $earlyFlg) ? "30" : "#";
        $arg["EARLY_WIDTH"]         = ($c002Data) ? "30" : "#";

        $allWidth  = 440;
        $allWidth  += $attendCnt * 38;
        if ($c002Data == true) $allWidth  += 80;

        //基準幅
        if ($model->Properties["useFi_Hrclass"] == "1" || $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $allWidthBase  = 900;
        } else {
            $allWidthBase  = 650;
        }

        //全体幅
        $arg["ALLWIDTH"] = ($allWidth > $allWidthBase) ? "\"{$allWidth}\"" : "\"{$allWidthBase}\"";

        /* タイトル設定 */
        $kahenField = setTitleData($objForm, $arg, $db, $model);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $objUp, $header, $hrName, $monthName, $kahenField, $attendItem);

        /* ボタン作成 */
        makeButton($objForm, $arg, $db, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);

        knjCreateHidden($objForm, "HIDDEN_HR_CLASS_TYPE");
        knjCreateHidden($objForm, "HIDDEN_HR_CLASS");
        knjCreateHidden($objForm, "HIDDEN_MONTH");

        $arg["finish"]  = $objForm->get_finish();

        /* テンプレート呼び出し */
        View::toHTML($model, "knjc031dForm1.html", $arg);
    }
}

//対象学級コンボ作成
function makeHrclassCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc031dQuery::selectHrClass($model);
    $result     = $db->query($query);
    $opt_hr     = array();

    $opt_hr[] = array("label" => "",
                      "value" => "");

    while($row  = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        //特別支援のとき
        if ($model->field["HR_CLASS_TYPE"] == "2" && $model->Properties["useFi_Hrclass"] != "1" && $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $opt_hr[] = array("label" => $row["GHR_NAME"],
                              "value" => $row["GHR_CD"]);
        //それ以外
        } else {
            $opt_hr[] = array("label" => $row["HR_NAME"],
                              "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
        }
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
    $query      = knjc031dQuery::selectSemesAll();
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
            $getdata = $db->getRow(knjc031dQuery::selectMonthQuery($month, $model), DB_FETCHMODE_ASSOC);
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
function setTitleData(&$objForm, &$arg, $db, $model)
{
    $setFieldName = array("4" => "SICK[]", "5" => "NOTICE[]", "6" => "NONOTICE[]");
    $c001 = ($model->schoolkind) ? 'C'.$model->schoolkind.'01' : 'C001';
    $result = $db->query(knjc031dQuery::getSickDiv($c001));
    $setFieldData = "";
    $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rtnField["C001"][] = $row["VALUE"];
    }
    $result->free();

    $c002Data = false;
    $result = $db->query(knjc031dQuery::getDetailDiv());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["DETAIL_TITLE_".$row["VALUE"]] = $row["LABEL"];
        $rtnField["C002"][] = $row["VALUE"];
        $c002Data = true;
    }
    $result->free();

    return $rtnField;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $headerData, $hrName, $monthName, $kahenField, $attendItem)
{
    //締め日取得
    list($tuki, $gakki) = explode("-", $model->field["month"]);
    $query = knjc031dQuery::getAppointedDay($tuki, $gakki, $model);
    $appointed_day = $db->getOne($query);

    //学校マスタ情報取得
    $schoolMst = array();
    $query = knjc031dQuery::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $schoolMst[$key] = $val;
        }
    }

    $subl_data = array();
    $subl_data = $db->getCol(knjc031dQuery::getNameMst('C006', "6"));

    $query      = knjc031dQuery::selectAttendQuery($model, $schoolMst, $subl_data);
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

        $setArray = array("LESSON"      => array("SIZE" => 2, "MAXLEN" => 3),
                          "OFFDAYS"     => array("SIZE" => 2, "MAXLEN" => 3),
                          "ABROAD"      => array("SIZE" => 2, "MAXLEN" => 3),
                          "ABSENT"      => array("SIZE" => 2, "MAXLEN" => 3),
                          "SUSPEND"     => array("SIZE" => 2, "MAXLEN" => 3),
                          "MOURNING"    => array("SIZE" => 2, "MAXLEN" => 3),
                          "LATE"        => array("SIZE" => 2, "MAXLEN" => 3),
                          "EARLY"       => array("SIZE" => 2, "MAXLEN" => 3)
                          );
        //C001可変
        $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
        $kahenField["C001"] = is_array($kahenField["C001"]) ? $kahenField["C001"] : array();
        foreach ($kahenField["C001"] as $c001Key => $c001Val) {
            $setArray[$setFieldName[$c001Val]] = array("SIZE" => 2, "MAXLEN" => 3);

            if($c001Val == "6") {
                $subl_cd = array();
                $subl_cd = $db->getCol(knjc031dQuery::getNameMst('C006', '6'));
                foreach ($subl_cd as $sublKey) {
                    $setArray["CNT".$sublKey] = array("SIZE" => 2, "MAXLEN" => 3);
                }
            }
        }

        //C002可変
        $kahenField["C002"] = is_array($kahenField["C002"]) ? $kahenField["C002"] : array();
        foreach ($kahenField["C002"] as $c002Key => $c002Val) {
            if ($c002Val == "101") {
                $setArray["DETAIL_".$c002Val] = array("SIZE" => 4, "MAXLEN" => 5);
            } else {
                $setArray["DETAIL_".$c002Val] = array("SIZE" => 2, "MAXLEN" => 3);
            }
        }

        //異動者（退学・転学・卒業）
        $idou_year = ($monthsem[0] < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
        $idou_day = ($appointed_day == "") ? getFinalDay($db, $monthsem[0], $monthsem[1]) : $appointed_day;
        $idou_date = $idou_year.'-'.$monthsem[0].'-'.$idou_day;
        $idou = $db->getOne(knjc031dQuery::getIdouData($row["SCHREGNO"], $idou_date));
        $row["BGCOLOR_IDOU"] = ($idou > 0) ? "bgcolor=yellow" : "";


        $subl_data = "";
        foreach ($setArray as $key => $val) {
            $sub_exist_check = 0;
            foreach ($attendItem as $item => $item_val) {
                if($item == $key) {
                    if(substr($key,0,3) == 'CNT'){
                        $sub_exist_check = get_count($db->getCol(knjc031dQuery::getNameMst('C007', $item_val[0])));
                    } else {
                        $sub_exist_check = get_count($db->getCol(knjc031dQuery::getNameMst('C006', $item_val[0])));
                    }
                }
            }

            if($sub_exist_check > "0") {
                if(substr($key,0,3) == 'CNT'){
                    $subl_data .= "<td {$row["BGCOLOR_IDOU"]} width=\"30\">".$row[$key]."</td>";
                }
                continue;
            } else {
                //入力文字チェック
                $setEntCheck = ($key == "DETAIL_101") ? "NumCheck(this.value)" : "toInteger(this.value)";
                //textbox
                $extra = "STYLE=\"text-align: right\"; onblur=\"this.value={$setEntCheck}\"; onPaste=\"return showPaste(this, ".$schCnt.");\"";
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $value = $row[$key];
                } else {
                    $value = ($row[$key] != 0) ? $row[$key] : "";
                }
                $row[$key] = knjCreateTextBox($objForm, $value, $key."[]", $val["SIZE"], $val["MAXLEN"], $extra);

                if(substr($key,0,3) == 'CNT'){
                    $subl_data .= "<td {$row["BGCOLOR_IDOU"]} width=\"30\">".$row[$key]."</td>";
                }
            }

        }
        $schCnt++;

        if (!$row["SEM_SCHREGNO"]) {
            $row["BGCOLOR_NAME_SHOW"] = "bgcolor=#ccffcc";
        }

        $row["subl_data"] = $subl_data;

        /* hidden(学籍番号) */
        $row["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$row["SCHREGNO"]."\">";

        //5行毎に色を変える
        if ($counter % 5 == 0) {
            $colorFlg = !$colorFlg;
        }
        $row["BGCOLOR_ROW"] = $colorFlg ? "#ffffff" : "#cccccc";
        $counter++;

        $data[] = $row;
    }
    $arg["attend_data"] = $data;

    $arg["SET_APPOINTED_DAY"] = $appointed_day;
    //hidden
    knjCreateHidden($objForm, "SET_APPOINTED_DAY", $appointed_day);
}

//最終日取得
function getFinalDay($db, $month, $semester)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc031dQuery::selectSemesAll($semester),DB_FETCHMODE_ASSOC);
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
    if(AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        //保存ボタン
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", " onclick=\"return btn_submit('update');\"");
        //取消ボタン
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    }
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

?>
