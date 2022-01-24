<?php
class knje371bSubForm_Subclass
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knje371bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR_SHOW"] = $model->year.'年度';

        //学校名
        $schoolAllName = knje371bQuery::getSchoolAllName($db, $model);
        $arg["SCHOOLNAME_SHOW"] = (strlen(trim($schoolAllName)) > 0) ? "学校名:".$schoolAllName : "";

        //科目数
        $model->field["SUBCLASS_NUM"] = ($model->field["SUBCLASS_NUM"] != "" && $model->cmd != "subSubclass_reset") ? $model->field["SUBCLASS_NUM"] : $db->getOne(knje371bQuery::getSubclassNum($model));
        $extra = "onblur=\"this.value = toInteger(this.value);\"";
        $arg["SUBCLASS_NUM"] = knjCreateTextBox($objForm, $model->field["SUBCLASS_NUM"], "SUBCLASS_NUM", 2, 1, $extra);


        /***一覧リスト***/
        $query = knje371bQuery::getList3($model, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学校名リンク
            $aHash = array("cmd"            => "subSubclass_edit",
                           "SUBCLASSCD"     => $row["SUBCLASSCD"]);
            $row["SUBCLASSNAME"] = View::alink("knje371bindex.php", $row["SUBCLASSNAME"], "", $aHash);

            //対象学年
            $sep1 = ($row["GRADE1_FLG"] == "1" && $row["GRADE2_FLG"] == "1") ? ", " : ""; 
            $sep2 = (($row["GRADE1_FLG"] == "1" || $row["GRADE2_FLG"] == "1") && $row["GRADE3_FLG"] == "1") ? ", " : "";
            $gradeName  = "";
            $gradeName .= ($row["GRADE1_FLG"] == "1") ? "1年" : "";
            $gradeName .= $sep1 . (($row["GRADE2_FLG"] == "1") ? "2年" : "");
            $gradeName .= $sep2 . (($row["GRADE3_FLG"] == "1") ? "3年" : "");
            $row["GRADE_NAME"] = $gradeName;

            //要件チェック
            $row["REQUIRED_FLG"]  = ($row["REQUIRED_FLG"] == "1") ? "レ" : "";
            $row["TRANSFER_FLG"]  = ($row["TRANSFER_FLG"] == "1") ? "レ" : "";
            $row["COMEBACK_FLG"]  = ($row["COMEBACK_FLG"] == "1") ? "レ" : "";

            $arg["list"][] = $row;
        }

        /***入力欄***/
        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $subclasscd = ($model->cmd == "subSubclass_chg") ? $model->field["SUBCLASSCD"] : $model->subclassAllCd;
            $Row = $db->getRow(knje371bQuery::getList3($model, "row", $subclasscd), DB_FETCHMODE_ASSOC);
            $model->field["SUBCLASSCD"] = $subclasscd;
        } else {
            $Row =& $model->field;
        }

        //科目コンボ
        $extra = " onchange=\"return btn_submit('subSubclass_chg');\" ";
        $query = knje371bQuery::getSubclass($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }

        //学年1チェック
        $extra  = " id=\"GRADE1_FLG\" ";
        $extra .= ($Row["GRADE1_FLG"]) ? " checked " : "";
        $arg["data"]["GRADE1_FLG"] = knjCreateCheckBox($objForm, "GRADE1_FLG", 1, $extra);
        //学年2チェック
        $extra  = " id=\"GRADE2_FLG\" ";
        $extra .= ($Row["GRADE2_FLG"]) ? " checked " : "";
        $arg["data"]["GRADE2_FLG"] = knjCreateCheckBox($objForm, "GRADE2_FLG", 1, $extra);
        //学年3チェック
        $extra  = " id=\"GRADE3_FLG\" ";
        $extra .= ($Row["GRADE3_FLG"]) ? " checked " : "";
        $arg["data"]["GRADE3_FLG"] = knjCreateCheckBox($objForm, "GRADE3_FLG", 1, $extra);

        //コース条件
        $extra = "";
        $query = knje371bQuery::getCourseCode($model);
        makeCmb($objForm, $arg, $db, $query, "COURSECODE2", $Row["COURSECODE2"], $extra, 1, "blank");

        //必須科目チェック
        $extra  = " id=\"REQUIRED_FLG\" ";
        $extra .= ($Row["REQUIRED_FLG"]) ? " checked " : "";
        $arg["data"]["REQUIRED_FLG"] = knjCreateCheckBox($objForm, "REQUIRED_FLG", 1, $extra);
        //2年編入者用科目チェック
        $extra  = " id=\"TRANSFER_FLG\" ";
        $extra .= ($Row["TRANSFER_FLG"]) ? " checked " : "";
        $arg["data"]["TRANSFER_FLG"] = knjCreateCheckBox($objForm, "TRANSFER_FLG", 1, $extra);
        //長期復学者用科目チェック
        $extra  = " id=\"COMEBACK_FLG\" ";
        $extra .= ($Row["COMEBACK_FLG"]) ? " checked " : "";
        $arg["data"]["COMEBACK_FLG"] = knjCreateCheckBox($objForm, "COMEBACK_FLG", 1, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje371bSubForm_Subclass.html", $arg);
    }
}

//履歴一覧
function makeRirekiList(&$arg, $db, &$model) {
    //ヘッダー情報を表示
    $setwidth = "60";
    $th = '';
    $th .= "<th width={$setwidth} align='center' rowspan='2'>学年</th>";
    foreach ($model->semsterNameArray as $key => $value) {
        if ($value["COUNTER"] == $model->lastcount - 1) {
            $th .= "<th width=* align='center' colspan={$value["SEMCOUNT"]}>{$value["NAME"]}</th>";
        } else {
            $th .= "<th width={$setwidth} align='center' colspan={$value["SEMCOUNT"]}>{$value["NAME"]}</th>";
        }
    }
    $arg['header1'][] = array('th' => $th);
    
    $th = '';
    foreach ($model->monthNameArray as $key => $monthName) {
        if ($key == $model->lastcount - 1) {
            $th .= "<th width=* align='center'>{$monthName}</th>";
        } else {
            $th .= "<th width={$setwidth} align='center'>{$monthName}</th>";
        }
    }
    $arg['header2'][] = array('th' => $th);
    
    //対象データ表示
    $query = knje371bQuery::getTaishouGrade($model);
    $result = $db->query($query);
    $tr_td = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    $tr_td .= "<tr nowrap align='center' bgcolor='#ffffff' font='black'><td width={$setwidth} align='center'><a href='knje371bindex.php?cmd=edit&UPD_FLG=1&GRADE={$row["GRADE"]}'>{$row["GRADE_NAME1"]}</a></td>";
        for ($i = $model->firstcount; $i < $model->lastcount; $i++) {
            //対象データを取得
            list($semester, $month) = explode("-", $model->field["SEM_MONTH".$i]);
            $getAppointedDay = $db->getOne(knje371bQuery::getRecordList($model, $row["GRADE"],  $semester, $month));
            if ($i == $model->lastcount - 1) {
                $tr_td .= "<td width=* align='center'>{$getAppointedDay}</td>";
            } else {
                $tr_td .= "<td width={$setwidth} align='center'>{$getAppointedDay}</td>";
            }
        }
        $tr_td .= "</tr>";
    }
    $arg['data2'][] = array('tr_td' => $tr_td);
    $result->free();
}

//データ表示
function makeList(&$objForm, &$arg, $db, &$model) {
    $model->monthNameArray = array();
    $model->semsterNameArray = array();
    $appointedDay = "";
    $valArray = array();
    $query = knje371bQuery::getList($model->year);
    $result = $db->query($query);
    $counter = 1;
    $semcounter = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        for ($i = $row['SMONTH']; $i <= $row['EMONTH']; $i++) {
            //カウンター用初期値を取得
            if ($counter == 1 && $i == $row['SMONTH']) {
                $model->firstcount = $counter;
                //学期名の名称表示用に取得
                $keySemester = $row['SEMESTER'];
            }
            $valArray = array();
            if ($i > 12) {
                $target_month = $i - 12;
            } else {
                $target_month = $i;
            }
            //反映時
            if ($model->cmd == "hanei") {
                $getDay = $db->getOne(knje371bQuery::getAppointedDay($model, $row['YEAR'], $model->school_kind, $target_month, $row['SEMESTER']));
                $appointedDay = knjCreateTextBox($objForm, $getDay, "APPOINTEDDAY".$counter, 2, 2, $extra);
                //hidden
                $model->field["SEM_MONTH".$counter] = $row['SEMESTER'].'-'.$target_month;
                knjCreateHidden($objForm, "SEM_MONTH".$counter, $model->field["SEM_MONTH".$counter]);
            } else {
                if (isset($model->warning)) {
                    $getDay = $model->field["APPOINTEDDAY".$counter];
                } else {
                    $query = knje371bQuery::getAppointedGradeDay($row['YEAR'], $target_month, $row['SEMESTER'], $model->grade);
                    $getDay = $db->getOne($query);
                }
                $appointedDay = knjCreateTextBox($objForm, $getDay, "APPOINTEDDAY".$counter, 2, 2, $extra);
                //hidden
                $model->field["SEM_MONTH".$counter] = $row['SEMESTER'].'-'.$target_month;
                knjCreateHidden($objForm, "SEM_MONTH".$counter, $model->field["SEM_MONTH".$counter]);
            }
            //月の表示文字するをDBから取得
            $query = knje371bQuery::getMonthName($row['YEAR'], sprintf('%02d',$target_month), $model);
            $monthName = $db->getOne($query);
            //背景に色をつけるかどうか、
            $query = knje371bQuery::getListColor(CTRL_YEAR, sprintf('%02d',$target_month), $model);
            $rowOfAdmin = $db->getRow($query);
            $td = '';
            if ($rowOfAdmin) {
                $td .= "<td align='center' class='no_search'>{$row['SEMESTERNAME']}</td>";
                $td .= "<td align='center' bgcolor='#C6DCEC'>{$monthName}</td>";
                $td .= "<td align='center' bgcolor='#C6DCEC'>{$appointedDay}</td>";
            } else {
                $td .= "<td align='center' class='no_search'>{$row['SEMESTERNAME']}</td>";
                $td .= "<td align='center' bgcolor='#ffffff'>{$monthName}</td>";
                $td .= "<td align='center' bgcolor='#ffffff'>{$appointedDay}</td>";
            }
            //月の名称履歴表示用に取得
            $model->monthNameArray[$counter] = $monthName;
            //学期の名称表示用に取得
            if ($keySemester == $row["SEMESTER"]) {
                $semcounter++;
                $model->semsterNameArray[$row["SEMESTER"]]["NAME"] = $row['SEMESTERNAME'];
                $model->semsterNameArray[$row["SEMESTER"]]["SEMCOUNT"] = $semcounter;
                $model->semsterNameArray[$row["SEMESTER"]]["COUNTER"] = $counter;
            } else {
                $model->semsterNameArray[$row["SEMESTER"]]["NAME"] = $row['SEMESTERNAME'];
                $model->semsterNameArray[$row["SEMESTER"]]["SEMCOUNT"] = $semcounter;
                $model->semsterNameArray[$row["SEMESTER"]]["COUNTER"] = $counter;
                $semcounter = 1;
            }
            $keySemester = $row["SEMESTER"];
            $arg['data'][] = array('td' => $td);
            $counter++;
        }
    }
    $result->free();
    $model->lastcount = $counter;
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('subSubclass_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subSubclass_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('subSubclass_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消しボタンを作成する
    $extra = "onclick=\"return btn_submit('subSubclass_reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタンを作成する
    $extra = "onclick=\"return btn_submit('main_edit')\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE371B");
    //キーを保持
    knjCreateHidden($objForm, "SCHOOL_CD"   , $model->schoolCd);
    knjCreateHidden($objForm, "FACULTYCD"   , $model->facultyCd);
    knjCreateHidden($objForm, "DEPARTMENTCD", $model->departmentCd);
}

?>
