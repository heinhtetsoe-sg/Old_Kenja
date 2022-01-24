<?php

require_once('for_php7.php');

class knjd187pForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd187pForm1", "POST", "knjd187pindex.php", "", "knjd187pForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd187pQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd187pChangeSemester'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //クラスコンボ作成
        $query = knjd187pQuery::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('knjd187p'), AllClearList();\"";
        $skind = makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") {
            $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        }

        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd187p', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);

        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd187p', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);

        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        $model->semesterDiv = $db->getOne(knjd187pQuery::getSemesterdiv());

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $skind);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd187pForm1.html", $arg);
    }
}

//リストtoリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    //対象者リストを作成する
    $query = knjd187pQuery::getSchno($model);
    $result = $db->query($query);
    $opt_right = $opt_left = array();
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $idou = "　";
        if (in_array($row["SCHREGNO"], $selectdata)) {
            $opt_left[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]);
        } else {
            $opt_right[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();

    //生徒一覧を作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

    //出力対象一覧を作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $value_idx = 0;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"],
                       'SKIND' => $row["SCHOOL_KIND"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
            $value_idx = get_count($opt)-1;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    if ($name == "GRADE_HR_CLASS") {
        $retVal = $value_flg ? $opt[$value_idx]["SKIND"] : $opt[0]["SKIND"];
    } else {
        $retVal = "";
    }
    return $retVal;
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $skind)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJD187P");
    knjCreateHidden($objForm, "SEMESTER_DIV", $model->semesterDiv);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");

    knjCreateHidden($objForm, "HREPORTREMARK_DAT_COMMUNICATION_SIZE", $model->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_"+$skind]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}
