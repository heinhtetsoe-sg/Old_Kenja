<?php

require_once('for_php7.php');

class knjc230Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("knjc230Form1", "POST", "knjc230index.php", "", "knjc230Form1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjc230Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHKIND"], "SCHKIND", $extra, 1);
        }

        //クラブコンボ
        $query = knjc230Query::selectClubQuery($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["CLUB"], "CLUB", $extra, 1, "BLANK");

        //生徒一覧
        makeStudentList($objForm, $arg, $db, $model);

        //開始日付
        $date1 = isset($model->field["FROMDATE"]) ? str_replace("-", "/", $model->field["FROMDATE"]) : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["FROMDATE"] = View::popUpCalendar($objForm, "FROMDATE", $date1);

        //終了日付
        $date2 = isset($model->field["TODATE"]) ? str_replace("-", "/", $model->field["TODATE"]) : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["TODATE"] = View::popUpCalendar($objForm, "TODATE", $date2);

        //開始校時
        $query = knjc230Query::getPeriodcd();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["FROMPERIOD"], "FROMPERIOD", $extra, 1, "BLANK");

        //終了校時
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TOPERIOD"], "TOPERIOD", $extra, 1, "BLANK");

        $data_cnt = 0;
        $model->arr_period = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($data_cnt == 0) {
                $model->s_period = $row["VALUE"];
            }
            $model->e_period = $row["VALUE"];
            $model->arr_period[] = $row["VALUE"];
            $data_cnt++;
        }

        //勤怠
        $query = knjc230Query::getDicd();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["DI_CD"], "DI_CD", $extra, 1, "BLANK");

        //症状・理由リストToリスト作成
        makeDiremarkList($objForm, $arg, $db, $model);

        //その他
        $extra = "";
        $arg["data"]["SONOTA"] = knjCreateTextBox($objForm, $model->field["SONOTA"], "SONOTA", 40, 20, $extra);

        //追加
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "追 加", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectStudent");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc230Form1.html", $arg);
    }
}
//makeCmb
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
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    $opt_left = $opt_right = array();

    //年組のMAX文字数取得
    $query = knjc230Query::selectStudentQuery($model);
    $result = $db->query($query);
    $model->selectStudent = is_array($model->selectStudent) ? $model->selectStudent : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
        $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
        $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
    }

    //生徒取得
    $query = knjc230Query::selectStudentQuery($model);
    $result = $db->query($query);
    $model->selectStudent = is_array($model->selectStudent) ? $model->selectStudent : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //クラス名称調整
        $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
        $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
        $len = $zenkaku * 2 + $hankaku;
        $hr_name = $row["HR_NAME"];
        for ($j = 0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) {
            $hr_name .= "&nbsp;";
        }

        list($gha, $schregno) = preg_split("/-/", $row["VALUE"]);

        if (in_array($schregno, $model->selectStudent)) {
            $opt_left[]= array('label' => $hr_name." ".$row["ATTENDNO"]." ".$row["NAME"],
                               'value' => $row["VALUE"]);
        } else {
            $opt_right[]= array('label' => $hr_name." ".$row["ATTENDNO"]." ".$row["NAME"],
                                'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧リストを作成する
    $extra = "multiple style=\"width:270px\" width:\"270px\" ondblclick=\"move1('left', 'student')\"";
    $arg["data"]["STUDENT_NAME"] = knjCreateCombo($objForm, "student_name", "", $opt_right, $extra, 10);

    //対象一覧リストを作成する
    $extra = "multiple style=\"width:270px\" width:\"270px\" ondblclick=\"move1('right', 'student')\"";
    $arg["data"]["STUDENT_SELECTED"] = knjCreateCombo($objForm, "student_selected", "", $opt_left, $extra, 10);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', 'student');\"";
    $arg["button"]["btn_stRights"] = knjCreateBtn($objForm, "btn_stRights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', 'student');\"";
    $arg["button"]["btn_stLefts"] = knjCreateBtn($objForm, "btn_stLefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 'student');\"";
    $arg["button"]["btn_stRight1"] = knjCreateBtn($objForm, "btn_stRight1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', 'student');\"";
    $arg["button"]["btn_stLeft1"] = knjCreateBtn($objForm, "btn_stLeft1", "＜", $extra);
}

//症状・理由リストToリスト作成
function makeDiremarkList(&$objForm, &$arg, $db, $model)
{
    $opt_left = $opt_right = array();

    $query = knjc230Query::getDiRemarkcd($model);
    $result = $db->query($query);
    $model->selectdata = is_array($model->selectdata) ? $model->selectdata : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $model->selectdata)) {
            $opt_left[]= array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
        } else {
            $opt_right[]= array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //症状・理由一覧リストを作成する
    $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('left', 'di')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 10);

    //対象一覧リストを作成する
    $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('right', 'di')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 10);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', 'di');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', 'di');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 'di');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', 'di');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}
