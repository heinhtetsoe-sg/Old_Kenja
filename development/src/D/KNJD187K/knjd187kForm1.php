<?php

require_once('for_php7.php');


class knjd187kForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd187kForm1", "POST", "knjd187kindex.php", "", "knjd187kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd187kQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd187k');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //対象学期の開始日・終了日
        $sDate = $db->getOne(knjd187kQuery::getSemesterDate("SDATE",$model->field["SEMESTER"]));
        $eDate = $db->getOne(knjd187kQuery::getSemesterDate("EDATE",$model->field["SEMESTER"]));
        knjCreateHidden($objForm, "SDATE", str_replace("-","/",$sDate));
        knjCreateHidden($objForm, "EDATE", str_replace("-","/",$eDate));

        //クラスコンボ作成
        $query = knjd187kQuery::getHrClass(CTRL_YEAR, $seme, $model);
        $extra = "onchange=\"return btn_submit('knjd187k');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        
        //学年
        $model->field["GRADE"] = substr($model->field["GRADE_HR_CLASS"],0,2);
        knjCreateHidden($objForm, "GRADE", $model->field["GRADE"]);

        //テストコンボ作成
        $query = knjd187kQuery::getTest($model->field["SEMESTER"], $model->field["GRADE"]);
        $opt = array();
        $value = $model->field["TESTCD"];
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $value = ($value) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["TESTCD"] = knjCreateCombo($objForm, "TESTCD", $value, $opt, $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd187k', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd187k', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $seme);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd187kForm1.html", $arg);
    }
}

function makeListToList(&$objForm, &$arg, $db, $model, $seme) {

    //表示切替
    $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
    $arg["data"]["TITLE_RIGHT"] = "生徒一覧";

    //対象外の生徒取得
    $query = knjd187kQuery::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //初期化
    $opt_right = $opt_left = array();

    //対象者リストを作成する
    $query = knjd187kQuery::getStudent($model, $seme);
    $result = $db->query($query);
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

    //一覧リスト（右）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);
        
    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

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
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJD187K");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
}

?>
