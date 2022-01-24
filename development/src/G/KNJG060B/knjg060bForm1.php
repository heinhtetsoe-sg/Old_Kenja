<?php

require_once('for_php7.php');

class knjg060bForm1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjg060bindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //立志舎のみ表示する項目
        if ($model->isRisshi == "1") {
            $arg["isRisshi"] = 1;
        }
        knjCreateHidden($objForm, "isRisshi", $model->isRisshi);

        //新入生ラジオを表示
        if ($model->isFreshman == "1") {
            $arg["isFreshman"] = 1;
        }
        knjCreateHidden($objForm, "isFreshman", $model->isFreshman);

        //ログイン年度表示
        $arg["data0"]["NENDO"] = CTRL_YEAR."年";
        //ログイン学期表示
        $arg["data0"]["GRADE"] = CTRL_SEMESTER."学期";

        //新入生・在校生 ラジオ
        $opt = array(1, 2);
        //新入生ラジオ表示をしない場合は「2:在籍」固定
        if ($model->isFreshman != "1") {
            $model->studentRadio = "2";
        }
        $model->studentRadio = ($model->studentRadio == "") ? "2" : $model->studentRadio;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"STUDENT_RADIO{$val}\" onChange=\"return btn_submit('read');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "STUDENT_RADIO", $model->studentRadio, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;

        //クラス選択コンボボックスを作成する
        $opt=array();
        if ($model->studentRadio == "2") {
            $query = knjg060bQuery::getAuth($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
            $result->free();
        } else {
            $opt[] = array('label' => "新入生",
                           'value' => "");
            $model->field["GRADE_HR_CLASS"] = "";
        }

        if(!isset($model->field["GRADE_HR_CLASS"]) || $model->field["GRADE_HR_CLASS"] == "") {
            $model->field["GRADE_HR_CLASS"] = $opt[0]["value"];
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => " onChange=\"return btn_submit('read');\"",
                            "options"    => isset($opt)?$opt:array()));

        $arg["data0"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //学年数上限値取得
        $max_grade = intval($db->getOne(knjg060bQuery::getMaxGrade()));

        //印刷指示
        makePrintSendData($objForm, $arg, $db, $model, $param, $max_grade);

        //ALLチェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        makeSortLink($arg, $model);

        //生徒データ表示
        if ($model->cmd == "read2" || $model->cmd == "search" || $model->cmd == "change" || $model->cmd == "monthChange") {
                makeStudentInfo($objForm, $arg, $db, $model);
        }

        //月数コンボ
        $opt = array();
        $value_flg = false;
        $query = knjg060bQuery::getNameMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->month_div == $row["VALUE"]) $value_flg = true;
        }
        $model->month_div = ($model->month_div && $value_flg) ? $model->month_div : $opt[0]["value"];
        $extra = "onChange=\"termChange();\"";
        $arg["div"]["MONTH_DIV"] = knjCreateCombo($objForm, "MONTH_DIV", $model->month_div, $opt, $extra, 1);

        //有効期間チェックボックス
        $extra  = ($model->date_blank_check) ? "checked" : "" ;
        $extra .= " onClick=\"dateDisabled(this)\"";
        $arg["div"]["DATE_BLANK_CHECK"] = knjCreateCheckBox($objForm, "DATE_BLANK_CHECK", 1, $extra);

        // 立志舎の場合、ラジオボタン追加
        if ($model->isRisshi == "1") {
            //電車・都バス ラジオ
            $opt = array(1, 2);
            $model->ticketRadio = ($model->ticketRadio == "") ? "1" : $model->ticketRadio;
            $extra = array();
            foreach ($opt as $key => $val) {
                array_push($extra, " id=\"TICKET_RADIO{$val}\" ");
            }
            $radioArray = knjCreateRadio($objForm, "TICKET_RADIO", $model->ticketRadio, $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) $arg[$key] = $val;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $arg, $model, $max_grade);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();

        $arg["print"] = $model->print == "on" ? "newwin('" . SERVLET_URL . "');" :"";
        $model->print = "off";
        $model->print_field = array();
        View::toHTML5($model, "knjg060bForm1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, $model) {
    knjCreateHidden($objForm, "objCntSub", get_count($model->select_data));
    $index = 0;
    if (is_null($model->select_data) || get_count($model->select_data) == 0) {
        return;
    }
    if ($model->studentRadio == "2") {
        $query = knjg060bQuery::getStudentInfoData2($model, $model->select_data);
    } else {
        $query = knjg060bQuery::getFreshmanInfoData2($model, $model->select_data);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $val = $row["SCHREGNO"];
        //登録済み通学データ
        $query = knjg060bQuery::getSchStation($model, $val);
        $stainf = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($stainf) {
            //テキスト出力
            $staname_From = $stainf["REMARK1"];
            $staname_To = $stainf["REMARK2"];
            $staname_Vir = $stainf["REMARK3"];
        } else {
            //通学データ(生徒環境調査データ取得)
            //始発駅:デフォルトは駅データの先頭データの発射駅データを取得
            $staname_From = (!is_null($model->subfield["STATION_FROM"]) && is_array($model->subfield["STATION_FROM"]) && get_count($model->subfield["STATION_FROM"]) > 0) ? $model->subfield["STATION_FROM"][$index] : $db->getRow(knjg060bQuery::getStationList_Josya($model, $row["SCHREGNO"], "1"), DB_FETCHMODE_ASSOC);
            if (is_array($staname_From)) $staname_From = $staname_From["VALUE"];
            //到着駅:デフォルトは駅データの最後の到着駅データを取得
            $staname_To = (!is_null($model->subfield["STATION_TO"]) && is_array($model->subfield["STATION_TO"]) && get_count($model->subfield["STATION_TO"]) > 0) ? $model->subfield["STATION_TO"][$index] : $db->getRow(knjg060bQuery::getStationList_Gesya($model, $row["SCHREGNO"], "7"), DB_FETCHMODE_ASSOC);
            if (is_array($staname_To)) $staname_To = $staname_To["VALUE"];
            //経由駅:デフォルトは空データで設定
            $staname_Vir = (!is_null($model->subfield["STATION_VIA"]) && is_array($model->subfield["STATION_VIA"]) && get_count($model->subfield["STATION_VIA"]) > 0) ? $model->subfield["STATION_VIA"][$index] : "";
        }


        //チェックボックス
        $checkVal = $index.":".$row["SCHREGNO"];
        $extra = in_array($checkVal, $model->checkBoxVal) ? "checked " : "";
        $row["PRINT_CHECK"] = knjcreateCheckBox($objForm, "PRINT_CHECK", $checkVal, $extra, "1");

        $linkData = "loadwindow('knjg060bindex.php?cmd=readStation&SCHREGNO={$row["SCHREGNO"]}',0,0,600,450)";
        $row["NAME"] = View::alink("#", htmlspecialchars($row["NAME"]),"onclick=\"$linkData\"");

        //コンボボックス
        $query = knjg060bQuery::getStationList_Josya($model, $row["SCHREGNO"]);
        $extra = " onPaste=\"return showPaste(this, {$index});\" ";
        $row["STATION_FROM"] = makeCmb($objForm, $arg, $db, $query, $staname_From, "STATION_FROM[]", $extra, 1, "BLANK");
        $query = knjg060bQuery::getStationList_Gesya($model, $row["SCHREGNO"]);
        $row["STATION_TO"] = makeCmb($objForm, $arg, $db, $query, $staname_To, "STATION_TO[]", $extra, 1, "BLANK");
        $query = knjg060bQuery::getStationList_Josya($model, $row["SCHREGNO"]);
        $row["STATION_VIA"] = makeCmb($objForm, $arg, $db, $query, $row["STATION_VIA"], "STATION_VIA[]", $extra, 1, "BLANK");

        $arg["data"][] = $row;

        $index++;
    }
}

//コンボ作成(判定が他と違ってNULL一致を許していたりBLANK指定でBLANKが無い場合のみ追加したりするので注意)
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $blankflg = 0;

    if ($query) {
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
            if ($blank == "BLANK") {
                if (!$blankflg && ($row["VALUE"] == "" || $row["VALUE"] == NULL)) {
                    $blankflg = 1;
                }
            }
        }
        //$value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $value = ($value_flg || ($blank == "BLANK" && $value == "")) ? $value : $opt[0]["value"];
        $result->free();

    }
    if ($blank == "BLANK" && !$blankflg) {
        $opt = array_merge(array(array("label" => "", "value" => "")), $opt);
    }
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//印刷指示
function makePrintSendData(&$objForm, &$arg, $db, &$model, $param, $max_grade) {
    $arg["PRINT_DIV2"] = "1";
    //有効期間開始日付
    $model->str_date = $model->str_date ? $model->str_date : CTRL_DATE;
    $extra = "\" onChange=\"termChange(); ";  //popUpCalendar2内部で結合している関係で、\"の対応がずれることに注意
    $disabled = ($model->date_blank_check) ? " disabled" : "";
    $arg["div"]["STR_DATE"] = View::popUpCalendar2($objForm, "STR_DATE", str_replace("-", "/", $model->str_date), "",  $extra, $disabled);

    //有効期間終了日付
    $sDate = str_replace("-", "/", $model->str_date);
    $date = preg_split("/\//", $sDate);
    list($year, $month, $day) = $date;

    //次月(単純に月に＋１)
    if ($model->cmd == "" || $model->end_date == "") {
        $setMonth = (int) date("m", mktime (0, 0 , 0 , $month + 1, $day, $year));
        $addMonth = $month + 1;
        if ($addMonth < $setMonth) {
            $setMonth = $addMonth;
            $d2Last = date("t", mktime( 0, 0, 0, $setMonth, 1, $year )); //指定したつきの日数
        } else {
            $d2Last = $day - 1;
            $year = date("Y", mktime( 0, 0, 0, $month + 1, 1, $year )); //12月の時の年数を考慮
        }
        $hogeDay = mktime (0, 0 , 0 , $setMonth, $d2Last, $year);
        $model->end_date = date("Y/m/d", $hogeDay);
    }

    $last_day = ($max_grade == 0) ? (CTRL_YEAR + 1) . "/03/31" : (CTRL_YEAR + 1 + $max_grade) . "/03/31";
    if ($model->end_date > $last_day) {
        $model->end_date = $last_day;
    }

    $extra = "";
    $disabled = ($model->date_blank_check) ? " disabled" : "";
    $arg["div"]["END_DATE"] = View::popUpCalendar2($objForm, "END_DATE", str_replace("-", "/", $model->end_date), "", $extra, $disabled);

    //発行日付
    $model->print_date = $model->print_date ? $model->print_date : CTRL_DATE;
    $arg["div"]["DATE"] = View::popUpCalendar($objForm, "DATE", str_replace("-", "/", $model->print_date));
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //読み込み
    $extra = "onclick=\"return btn_submit('read2');\"";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読込み", $extra);

    //発行
    if (AUTHORITY >= DEF_UPDATE_RESTRICT) {
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "発 行", $extra);
    }

    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, &$arg, $model, $max_grade) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SELECT_DATA");

    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "PROGRAMID", "KNJG060B");
    knjCreateHidden($objForm, "PRGID", "KNJG060");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "EXE_YEAR", $model->search["EXE_YEAR"]);
    knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR . "/04/01");
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "knjg060bPrintStamp", $model->Properties["knjg060bPrintStamp"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

    $chk_eyear = ($max_grade == 0) ? (CTRL_YEAR + 1) : (CTRL_YEAR + 1 + $max_grade);
    knjCreateHidden($objForm, "CHK_EDATE", $chk_eyear . "/03/31");

    //印刷指定
    if ($model->print == "on") {
        knjCreateHidden($objForm, "CHECK_TUGAKU", "1");
        knjCreateHidden($objForm, "TYPE", "2");
    }

    //印刷に渡すパラメータ
    if ($model->print_field) {
        foreach ($model->print_field[2] as $val) {
            $arg["data3"][] = $val;
        }
    }

    //ソート用
    knjCreateHidden($objForm, "s_id", $model->s_id);
    knjCreateHidden($objForm, "SORT_SCHREGNO", $model->sort["SORT_SCHREGNO"]);
    knjCreateHidden($objForm, "SORT_HR_NAME", $model->sort["SORT_HR_NAME"]);

}
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    $opt_right = array();
    if ($model->studentRadio == "2") {
        $query = knjg060bQuery::getCategoryName($model);
    } else {
        $query = knjg060bQuery::getFreshmanName($model);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();

    $opt_left = array();
    if ($model->studentRadio == "2") {
        $query = knjg060bQuery::getCategorySelected($model);
    } else {
        $query = knjg060bQuery::getFreshmanSelected($model);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
    }
    $result->free();

    //対象者一覧作成
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
    $arg["data0"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 10);

    //出力対象作成
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
    $arg["data0"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 10);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

// 
function getFromToArray($stainf, $flgarry) {
    $retarry = array();
    $chkallflg = in_array("ALL", $flgarry);
    if ($chkallflg || in_array($stainf["FLG_1"], $flgarry)) {
        if ($stainf["ROSEN_1"] != "" || $stainf["JOSYA_1"] != "" || $stainf["GESYA_1"] != "") {
            $retarry[] = Array("ID"=>"1", "FLG"=>$stainf["FLG_1"], "ROSEN"=>$stainf["ROSEN_1"], "JOSYA"=>$stainf["JOSYA_1"], "GESYA"=>$stainf["GESYA_1"]);
        }
    }
    if ($chkallflg || in_array($stainf["FLG_2"], $flgarry)) {
        if ($stainf["ROSEN_2"] != "" || $stainf["JOSYA_2"] != "" || $stainf["GESYA_2"] != "") {
            $retarry[] = Array("ID"=>"2", "FLG"=>$stainf["FLG_2"], "ROSEN"=>$stainf["ROSEN_2"], "JOSYA"=>$stainf["JOSYA_2"], "GESYA"=>$stainf["GESYA_2"]);
        }
    }
    if ($chkallflg || in_array($stainf["FLG_3"], $flgarry)) {
        if ($stainf["ROSEN_3"] != "" || $stainf["JOSYA_3"] != "" || $stainf["GESYA_3"] != "") {
            $retarry[] = Array("ID"=>"3", "FLG"=>$stainf["FLG_3"], "ROSEN"=>$stainf["ROSEN_3"], "JOSYA"=>$stainf["JOSYA_3"], "GESYA"=>$stainf["GESYA_3"]);
        }
    }
    if ($chkallflg || in_array($stainf["FLG_4"], $flgarry)) {
        if ($stainf["ROSEN_4"] != "" || $stainf["JOSYA_4"] != "" || $stainf["GESYA_4"] != "") {
            $retarry[] = Array("ID"=>"4", "FLG"=>$stainf["FLG_4"], "ROSEN"=>$stainf["ROSEN_4"], "JOSYA"=>$stainf["JOSYA_4"], "GESYA"=>$stainf["GESYA_4"]);
        }
    }
    if ($chkallflg || in_array($stainf["FLG_5"], $flgarry)) {
        if ($stainf["ROSEN_5"] != "" || $stainf["JOSYA_5"] != "" || $stainf["GESYA_5"] != "") {
            $retarry[] = Array("ID"=>"5", "FLG"=>$stainf["FLG_5"], "ROSEN"=>$stainf["ROSEN_5"], "JOSYA"=>$stainf["JOSYA_5"], "GESYA"=>$stainf["GESYA_5"]);
        }
    }
    if ($chkallflg || in_array($stainf["FLG_6"], $flgarry)) {
        if ($stainf["ROSEN_6"] != "" || $stainf["JOSYA_6"] != "" || $stainf["GESYA_6"] != "") {
            $retarry[] = Array("ID"=>"6", "FLG"=>$stainf["FLG_6"], "ROSEN"=>$stainf["ROSEN_6"], "JOSYA"=>$stainf["JOSYA_6"], "GESYA"=>$stainf["GESYA_6"]);
        }
    }
    if ($chkallflg || in_array($stainf["FLG_7"], $flgarry)) {
        if ($stainf["ROSEN_7"] != "" || $stainf["JOSYA_7"] != "" || $stainf["GESYA_7"] != "") {
            $retarry[] = Array("ID"=>"7", "FLG"=>$stainf["FLG_7"], "ROSEN"=>$stainf["ROSEN_7"], "JOSYA"=>$stainf["JOSYA_7"], "GESYA"=>$stainf["GESYA_7"]);
        }
    }
    return $retarry;
}

//ソートリンク作成
function makeSortLink(&$arg, &$model) {
    //ソート
    $mark = array("▼","▲");

    switch ($model->s_id) {
        case "SORT_SCHREGNO":
            $mark1 = $mark[$model->sort[$model->s_id]];
            break;
        case "SORT_HR_NAME":
            $mark2 = $mark[$model->sort[$model->s_id]];
            break;
    }

    $extra = "id=\"dataSort\" onClick=\"return dataSort('SORT_SCHREGNO');\"";
    $arg["SORT_SCHREGNO"] = View::alink("javascript:void(0)", htmlspecialchars("学籍番号".$mark1), $extra);

    $extra = "id=\"dataSort\" onClick=\"return dataSort('SORT_HR_NAME');\"";
    $arg["SORT_HR_NAME"] = View::alink("javascript:void(0)", htmlspecialchars("年組".$mark2), $extra);
}

?>
