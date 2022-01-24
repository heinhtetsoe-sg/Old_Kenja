<?php

require_once('for_php7.php');

class knjc161gForm1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc161gForm1", "POST", "knjc161gindex.php", "", "knjc161gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        
        //学期
        $model->field["SEMESTER"] = $model->field["SEMESTER"] == null ? CTRL_SEMESTER : $model->field["SEMESTER"];
        
        //学期コンボ作成
        $query = knjc161gQuery::getSemester($model);
        $extra = "onchange=\"return btn_submit('semester'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //表示指定ラジオボタン 1:クラス 2:個人
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('change')\"", "id=\"DISP2\" onClick=\"return btn_submit('change')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, get_count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //年組コンボ
        if ($model->field["DISP"] == "1") {
            $query = knjc161gQuery::getGrade($model, $model->field["SEMESTER"]);
        } else {
            $query = knjc161gQuery::getGradeHrClass($model, $model->field["SEMESTER"]);
        }
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        //校種
        if ($model->field["DISP"] == "1") {
            $query = knjc161gQuery::getGrade($model, $model->field["SEMESTER"], $model->field["GRADE_HR_CLASS"]);
        } else {
            $query = knjc161gQuery::getGradeHrClass($model, $model->field["SEMESTER"], $model->field["GRADE_HR_CLASS"]);
        }
        $model->schoolKind = $db->getOne($query);
        
        $query = knjc161gQuery::getSemester($model, "SDATE");
        $model->sDate = $db->getOne($query);
        $query = knjc161gQuery::getSemester($model, "EDATE");
        $model->eDate = $db->getOne($query);
        
        $sDate = str_replace("-", "/", $model->sDate);
        $eDate = str_replace("-", "/", $model->eDate);

        //開始日付初期値
        if ($model->field["SDATE"] == "") {
            $model->field["SDATE"] = $sDate;
        }
        //開始日（テキスト）
        $disabled = "";
        $extra = "onblur=\"tmp_list('knjc161g', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["SDATE"], "SDATE", 12, 12, $extra);
        //開始日（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjc161g', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=SDATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['SDATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //簿日付
        $arg["data"]["SDATE"] = View::setIframeJs().$date_textbox.$date_button;
        
        //終了日付初期値
        if ($model->field["EDATE"] == "") {
            $model->field["EDATE"] = $eDate;
        }
        //終了日（テキスト）
        $disabled = "";
        $extra = "onblur=\"tmp_list('knjc161g', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["EDATE"], "EDATE", 12, 12, $extra);
        //終了日（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjc161g', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=EDATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['EDATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //簿日付
        $arg["data"]["EDATE"] = View::setIframeJs().$date_textbox.$date_button;
        
        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $model->field["SEMESTER"]);

        //ボタン作成
        makeBtn($objForm, $arg, $sDate, $eDate);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc161gForm1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $seme)
{
    //対象外の生徒取得
    $query = knjc161gQuery::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //初期化
    $opt_right = $opt_left = array();

    //左リストで選択されたものを再セット
    $selectdata = ($model->selectdata != "") ? explode(",", $model->selectdata) : array();
    $selectdataText = ($model->selectdataText != "") ? explode(",", $model->selectdataText) : array();
    for ($i = 0; $i < get_count($selectdata); $i++) {
        $opt_left[] = array("label" => $selectdataText[$i],
                            "value" => $selectdata[$i]);
    }

    //対象者リスト
    if ($model->field["DISP"] == "1") {
        $query = knjc161gQuery::getGradeHrClass($model, $seme, $model->field["GRADE_HR_CLASS"]);
    } else {
        $query = knjc161gQuery::getStudent($model, $seme);
    }
    $result = $db->query($query);
    if ($model->field["DISP"] == "1") {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["VALUE"], $selectdata)) {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
    } else {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["SCHREGNO"], $opt_idou)) {
            } elseif (!in_array($row["SCHREGNO"], $selectdata)) {
                $opt_right[] = array('label' => $row["SCHREGNO"]." ".$row["ATTENDNO"]."番"." ".$row["NAME_SHOW"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $sDate, $eDate)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" .$sDate ."', '" .$eDate ."');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "sDate", $model->sDate);
    knjCreateHidden($objForm, "eDate", $model->eDate);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJC161G");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataText");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
