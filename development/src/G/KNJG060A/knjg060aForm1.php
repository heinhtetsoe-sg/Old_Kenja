<?php

require_once('for_php7.php');
class knjg060aForm1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjg060aindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度表示
        $arg["data0"]["NENDO"] = CTRL_YEAR."年";
        //ログイン学期表示
        $arg["data0"]["SEMESTER"] = CTRL_SEMESTER."学期";

        //クラス or 学年
        $opt = array(1, 2);
        if (!$model->field["GROUP"]) $model->field["GROUP"] = "1";
        $extra = array("id=\"GROUP1\" onclick=\"btn_submit('change');\" " , "id=\"GROUP2\" onclick=\"btn_submit('change');\" ");
        $radioArray = knjCreateRadio($objForm, "GROUP", $model->field["GROUP"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data0"][$key] = $val;

        //クラス選択コンボボックスを作成する
        $opt=array();
        $query = knjg060aQuery::getAuth($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        if($model->cmd == 'change' || !isset($model->field["GRADE_HR_CLASS"])) {
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
        $max_grade = intval($db->getOne(knjg060aQuery::getMaxGrade($model)));

        //印刷指示
        makePrintSendData($objForm, $arg, $db, $model, $param, $max_grade);

        //生徒データ表示
        $stationinfo = array("STATION_FROM"=>array(), "STATION_TO"=>array(), "STATION_VIR"=>array());
        if ($model->cmd == "read2" || $model->cmd == "search" || $model->cmd == "change" || $model->cmd == "monthChange") {
            makeStudentInfo($objForm, $arg, $db, $model, $stationinfo);
        }

        //月数コンボ
        $opt = array();
        $value_flg = false;
        $query = knjg060aQuery::getNameMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->month_div == $row["VALUE"]) $value_flg = true;
        }
        $model->month_div = ($model->month_div && $value_flg) ? $model->month_div : $opt[0]["value"];
        $extra = "";
        $arg["div"]["MONTH_DIV"] = knjCreateCombo($objForm, "MONTH_DIV", $model->month_div, $opt, $extra, 1);

        // 年組版情報 (JSON)
        $compareInfo = "";
        $query = knjg060aQuery::getSchregRegdDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($compareInfo != '') {
                $compareInfo .= ", ";
            }
            $compareInfo .= "\"".$row["SCHREGNO"]."\": \"".$row["COMPARE_VALUE"]."\" ";
        }
        $compareInfo = "{".$compareInfo."}";

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $arg, $model, $max_grade, $stationinfo, $compareInfo);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();

        $arg["print"] = $model->print == "on" ? "newwin('" . SERVLET_URL . "');" :"";
        $model->print = "off";
        $model->print_field = array();
        View::toHTML($model, "knjg060aForm1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, $model, &$stationinfo) {
    $extra = " onPaste=\"return showPaste(this, {$index});\" ";
    knjCreateHidden($objForm, "objCntSub", get_count($model->select_data));
    $index = 0;

    //全てチェック
    $extra = $model->field["CHECK_ALL"] == "1" ? "checked" : "";
    $extra .= " id=\"CHECK_ALL\" onclick =\"check_all(this);\"";
    $arg["data0"]["CHECK_ALL"] = knjCreateCheckBox($objForm, "CHECK_ALL", "1", $extra, "");

    foreach ($model->select_data as $val) {
        $query = knjg060aQuery::getSchreg_envir_dat($model, $val);
        $stainf = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //引数のフラグに当たるデータ(=FLG_?が引数のArrayに含まれる)のみを配列で取得。
        $staarry = getFromToArray($stainf, Array("ALL"));
        $laststaid = get_count($staarry) > 0 ? $staarry[get_count($staarry)-1]["ID"] : "";
        $staname_From = "";
        $staname_To = "";
        $staname_Vir = "";
        if ($laststaid != "") {
            $strtid = 0;
            for ($idxwk = 0;$idxwk < get_count($staarry);$idxwk++) {
                if ($staarry[$idxwk]["JOSYA"] != "") {
                    $strtid = $idxwk;
                    break;
                }
            }
            if ($staarry[$strtid]["FLG"] == "1") {
                $query1 = knjg060aQuery::getStationName2($staarry[$strtid]["JOSYA"], $staarry[$strtid]["ROSEN"]);
                $staname_From = $db->getOne($query1);
            }
            if ($staname_From == "") {
                $staname_From = $staarry[$strtid]["JOSYA"];
            }
            if ($staarry[get_count($staarry)-1]["FLG"] == "1") {
                $query2 = knjg060aQuery::getStationName2($staarry[get_count($staarry)-1]["GESYA"], $staarry[get_count($staarry)-1]["ROSEN"]);
                $staname_To = $db->getOne($query2);
            }
            if ($staname_To == "") {
                $staname_To = $staarry[get_count($staarry)-1]["GESYA"];
            }
            if (get_count($staarry)-1 > $strtid) {
                if ($staarry[get_count($staarry)-1]["FLG"] == "1") {
                    $query3 = knjg060aQuery::getStationName2($staarry[get_count($staarry)-1]["JOSYA"], $staarry[get_count($staarry)-1]["ROSEN"]);
                    $staname_Vir = $db->getOne($query3);
                }
                if ($staname_Vir == "") {
                    $staname_Vir = $staarry[get_count($staarry)-1]["JOSYA"];
                }
            }
        }

        $query = knjg060aQuery::getStudentInfoData2($model, $val);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //チェックボックス
        $checkVal = $index.":".$row["SCHREGNO"];
        $extra = in_array($checkVal, $model->checkBoxVal) ? "checked " : "";
        $extra .= " class='issue_check' ";
        $row["PRINT_CHECK"] = knjcreateCheckBox($objForm, "PRINT_CHECK", $checkVal, $extra, "1");

        $linkData = "loadwindow('knjg060aindex.php?cmd=readStation&SCHREGNO={$row["SCHREGNO"]}',0,0,600,450)";
        $row["NAME"] = View::alink("#", htmlspecialchars($row["NAME"]),"onclick=\"$linkData\"");

        //テキスト出力
        $row["STATION_FROM"] = $staname_From;
        $row["STATION_TO"]   = $staname_To;
        $row["STATION_VIA"]  = $staname_Vir;

        //hiddenで保持する。
        $stationinfo["STATION_FROM"][] = $row["STATION_FROM"];
        $stationinfo["STATION_TO"][]   = $row["STATION_TO"];
        $stationinfo["STATION_VIA"][]  = $row["STATION_VIA"];

        $arg["data"][] = $row;

        $index++;
    }
}

//印刷指示
function makePrintSendData(&$objForm, &$arg, $db, &$model, $param, $max_grade) {
    $arg["PRINT_DIV2"] = "1";
    //有効期間開始日付
    //$model->str_date = $model->str_date ? $model->str_date : CTRL_DATE;
    $model->str_date = CTRL_YEAR."/04/01";
    $arg["div"]["STR_DATE"] = View::popUpCalendar($objForm, "STR_DATE", str_replace("-", "/", $model->str_date));

    ////有効期間終了日付
    //$sDate = str_replace("-", "/", $model->str_date);
    //$date = preg_split("{/}", $sDate);
    //list($year, $month, $day) = $date;
    //
    ////次月(単純に月に＋１)
    //if ($model->cmd == "" || $model->end_date == "") {
    //    $setMonth = (int) date("m", mktime (0, 0 , 0 , $month + 1, $day, $year));
    //    $addMonth = $month + 1;
    //    if ($addMonth < $setMonth) {
    //        $setMonth = $addMonth;
    //        $d2Last = date("t", mktime( 0, 0, 0, $setMonth, 1, $year )); //指定したつきの日数
    //    } else {
    //        $d2Last = $day - 1;
    //        $year = date("Y", mktime( 0, 0, 0, $month + 1, 1, $year )); //12月の時の年数を考慮
    //    }
    //    $hogeDay = mktime (0, 0 , 0 , $setMonth, $d2Last, $year);
    //    $model->end_date = date("Y/m/d", $hogeDay);
    //}
    //
    //$last_day = ($max_grade == 0) ? (CTRL_YEAR + 1) . "/03/31" : (CTRL_YEAR + 1 + $max_grade) . "/03/31";
    //if ($model->end_date > $last_day) {
    //    $model->end_date = $last_day;
    //}

    $model->end_date = (CTRL_YEAR + 1) . "/03/31";
    $arg["div"]["END_DATE"] = View::popUpCalendar($objForm, "END_DATE", str_replace("-", "/", $model->end_date));

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
function makeHidden(&$objForm, &$arg, $model, $max_grade, $stationinfo, $compareInfo) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SELECT_DATA");

    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "PROGRAMID", "KNJG060A");
    knjCreateHidden($objForm, "PRGID", "KNJG060A");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "EXE_YEAR", $model->search["EXE_YEAR"]);
    knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR . "/04/01");
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

    $chk_eyear = ($max_grade == 0) ? (CTRL_YEAR + 1) : (CTRL_YEAR + 1 + $max_grade);
    knjCreateHidden($objForm, "CHK_EDATE", $chk_eyear . "/03/31");

    $setstastr = "";
    $sep = "";
    for ($ii = 0;$ii < get_count($stationinfo["STATION_FROM"]);$ii++) {
        $setstastr .= $sep . $stationinfo["STATION_FROM"][$ii];
        $sep = ",";
    }
    knjCreateHidden($objForm, "STATION_FROM" , $setstastr);

    $setstastr = "";
    $sep = "";
    for ($ii = 0;$ii < get_count($stationinfo["STATION_TO"]);$ii++) {
        $setstastr .= $sep . $stationinfo["STATION_TO"][$ii];
        $sep = ",";
    }
    knjCreateHidden($objForm, "STATION_TO" , $setstastr);

    $setstastr = "";
    $sep = "";
    for ($ii = 0;$ii < get_count($stationinfo["STATION_VIA"]);$ii++) {
        $setstastr .= $sep . $stationinfo["STATION_VIA"][$ii];
        $sep = ",";
    }
    knjCreateHidden($objForm, "STATION_VIA" , $setstastr);

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
    knjCreateHidden($objForm, "compareInfo", $compareInfo);

}
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    $opt_right = array();
    $query = knjg060aQuery::getCategoryName($model, "right");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();

    $opt_left = array();
    $query = knjg060aQuery::getCategoryName($model, "left");
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
?>
