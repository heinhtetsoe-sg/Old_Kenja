<?php

require_once('for_php7.php');


class knjd185lForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd185lForm1", "POST", "knjd185lindex.php", "", "knjd185lForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $model->semester = $model->semester == "" ? CTRL_SEMESTER : $model->semester;
        $query = knjd185lQuery::getSemester($getCountsemester);
        $extra = "onchange=\"return btn_submit('knjd185l');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //クラスコンボ作成
        $query = knjd185lQuery::getHrClass(CTRL_YEAR, $seme, $model);
        $extra = "onchange=\"return btn_submit('knjd185l');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd185l', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd185l', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //異動対象日付初期値セット
        if ($model->field["COMP_DATE"] == "") $model->field["COMP_DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd185l', 'on')\"".$disabled;
        $compdate_textbox = knjCreateTextBox($objForm, $model->field["COMP_DATE"], "COMP_DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd185l', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=COMP_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['COMP_DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $compdate_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["COMP_DATE"] = View::setIframeJs().$compdate_textbox.$compdate_button;

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model, $seme);

//        //出力内容 1:テスト点 2:素点
//        $model->field["ITEM_DIV"] = $model->field["ITEM_DIV"] ? $model->field["ITEM_DIV"] : '1';
//        $opt_rank = array(1, 2);
//        $extra = array("id=\"ITEM_DIV1\"", "id=\"ITEM_DIV2\"");
//        $radioArray = knjCreateRadio($objForm, "ITEM_DIV", $model->field["ITEM_DIV"], $extra, $opt_rank, get_count($opt_rank));
//        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

//        //順位の基準点ラジオボタン 1:総合点 2:平均点
//        $model->field["RANK_DIV"] = $model->field["RANK_DIV"] ? $model->field["RANK_DIV"] : '1';
//        $opt_rank = array(1, 2);
//        $extra = array("id=\"RANK_DIV1\"", "id=\"RANK_DIV2\"");
//        $radioArray = knjCreateRadio($objForm, "RANK_DIV", $model->field["RANK_DIV"], $extra, $opt_rank, get_count($opt_rank));
//        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

//        //平均・席次・偏差値ラジオボタン 1:学年 2:クラス 3:コース 4:学科
//        $opt_group = array(1, 2, 3, 4);
//        $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
//        $extraEnabled = "";
//        $extra = array("id=\"GROUP_DIV1\"".$extraEnabled, "id=\"GROUP_DIV2\"".$extraEnabled, "id=\"GROUP_DIV3\"".$extraEnabled, "id=\"GROUP_DIV4\"".$extraEnabled);
//        $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt_group, get_count($opt_group));
//        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

//        // 順位表記
//        $extra  = ($model->field["PRINT_RANK"] == "1" || $model->cmd == '') ? "checked='checked' " : "";
//        $extra .= " id=\"PRINT_RANK\"";
//        $extra .= " onclick=\"kubun();\" ";
//        $arg["data"]["PRINT_RANK"] = knjCreateCheckBox($objForm, "PRINT_RANK", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd185lForm1.html", $arg);
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model, $seme) {
    //対象外の生徒取得
    $query = knjd185lQuery::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //対象者リストを作成する
    $query = knjd185lQuery::getStudent($model, $seme);
    $result = $db->query($query);
    $opt_right = $opt_left = array();
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
        if (in_array($row["SCHREGNO"], $selectdata)) {
            $opt_left[] = array('label' => $row["SCHREGNO_SHOW"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]);
        } else {
            $opt_right[] = array('label' => $row["SCHREGNO_SHOW"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

    //生徒一覧リストを作成する//
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJD185L");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_COMMUNICATION_SIZE_P", $model->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_COMMUNICATION_SIZE_J", $model->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_COMMUNICATION_SIZE_H", $model->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"]);
    // knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    // knjCreateHidden($objForm, "useAssessCourseMst", $model->Properties["useAssessCourseMst"]);
    // knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    
}

?>
