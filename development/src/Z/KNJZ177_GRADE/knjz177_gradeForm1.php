<?php

require_once('for_php7.php');
class knjz177_gradeForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz177_gradeindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        
        //対象年度
        $arg["YEAR_SHOW"] = $model->year.'年度';

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjz177_gradeQuery::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('edit')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->school_kind, $extra, 1);
        }

        //対象データ表示
        makeList($objForm, $arg, $db, $model);

        //履歴一覧表示
        $rirekiCnt = makeRirekiList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        
        //学年コンボ
        $query = knjz177_gradeQuery::getGrade($model);
        $extra = "onchange=\"btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1, "blank");
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz177_gradeForm1.html", $arg);
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
    $query = knjz177_gradeQuery::getTaishouGrade($model);
    $result = $db->query($query);
    $tr_td = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
    $tr_td .= "<tr nowrap align='center' bgcolor='#ffffff' font='black'><td width={$setwidth} align='center'><a href='knjz177_gradeindex.php?cmd=edit&UPD_FLG=1&GRADE={$row["GRADE"]}'>{$row["GRADE_NAME1"]}</a></td>";
        for ($i = $model->firstcount; $i < $model->lastcount; $i++) {
            //対象データを取得
            list($semester, $month) = explode("-", $model->field["SEM_MONTH".$i]);
            $getAppointedDay = $db->getOne(knjz177_gradeQuery::getRecordList($model, $row["GRADE"],  $semester, $month));
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
    $query = knjz177_gradeQuery::getList($model->year);
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
                $getDay = $db->getOne(knjz177_gradeQuery::getAppointedDay($model, $row['YEAR'], $model->school_kind, $target_month, $row['SEMESTER']));
                $appointedDay = knjCreateTextBox($objForm, $getDay, "APPOINTEDDAY".$counter, 2, 2, $extra);
                //hidden
                $model->field["SEM_MONTH".$counter] = $row['SEMESTER'].'-'.$target_month;
                knjCreateHidden($objForm, "SEM_MONTH".$counter, $model->field["SEM_MONTH".$counter]);
            } else {
                if (isset($model->warning)) {
                    $getDay = $model->field["APPOINTEDDAY".$counter];
                } else {
                    $query = knjz177_gradeQuery::getAppointedGradeDay($row['YEAR'], $target_month, $row['SEMESTER'], $model->grade);
                    $getDay = $db->getOne($query);
                }
                $appointedDay = knjCreateTextBox($objForm, $getDay, "APPOINTEDDAY".$counter, 2, 2, $extra);
                //hidden
                $model->field["SEM_MONTH".$counter] = $row['SEMESTER'].'-'.$target_month;
                knjCreateHidden($objForm, "SEM_MONTH".$counter, $model->field["SEM_MONTH".$counter]);
            }
            //月の表示文字するをDBから取得
            $query = knjz177_gradeQuery::getMonthName($row['YEAR'], sprintf('%02d',$target_month), $model);
            $monthName = $db->getOne($query);
            //背景に色をつけるかどうか、
            $query = knjz177_gradeQuery::getListColor(CTRL_YEAR, sprintf('%02d',$target_month), $model);
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

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //学期マスタ反映ボタンを作成する
    $extra = "onclick=\"return btn_submit('hanei');\"";
    $arg["button"]["btn_hanei"] = knjCreateBtn($objForm, "btn_hanei", "出欠締め日管理マスタ反映", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消しボタンを作成する
    $extra = "onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタンを作成する
    $link = REQUESTROOT."/Z/KNJZ177/knjz177index.php";
    $extra = "onclick=\"parent.location.href='$link';\"";
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
    knjCreateHidden($objForm, "PRGID", KNJZ177_GRADE);
}

?>
