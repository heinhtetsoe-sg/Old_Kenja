<?php

require_once('for_php7.php');

class knjd672fForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd672fForm1", "POST", "knjd672findex.php", "", "knjd672fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd672fQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd672f'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ
        $query = knjd672fQuery::getHrClass($model);
        $extra = "onchange=\"return btn_submit('knjd672f'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //年組コンボ
        $query = knjd672fQuery::getTantou($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TANTOU", $model->field["TANTOU"], $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this);btn_submit('knjd672f')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list(); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        // 皆勤欠席
        if (!$model->field["KAIKIN_KESSEKI"]) {
            $model->field["KAIKIN_KESSEKI"] = "0";
        }
        $arg["data"]["KAIKIN_KESSEKI"] = $model->field["KAIKIN_KESSEKI"];
        // 精勤欠席
        if (!$model->field["SEIKIN_KESSEKI"]) {
            $model->field["SEIKIN_KESSEKI"] = "3";
        }
        $arg["data"]["SEIKIN_KESSEKI"] = $model->field["SEIKIN_KESSEKI"];
        // 欠席換算
        if (!$model->field["KESSEKI_KANSAN"]) {
            $model->field["KESSEKI_KANSAN"] = "3";
        }

        //extra
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        //皆勤者/遅刻
        $value = $model->field["KAIKIN_KAIKIN_TIKOKU"];
        $value = $value ? $value : "2";
        $arg["data"]["KAIKIN_KAIKIN_TIKOKU"] = knjCreateTextBox($objForm, $value, "KAIKIN_KAIKIN_TIKOKU", 3, 2, $extra);
        //精勤者/遅刻
        $value = $model->field["KAIKIN_SEIKIN_TIKOKU"];
        $value = $value ? $value : "2";
        $arg["data"]["KAIKIN_SEIKIN_TIKOKU"] = knjCreateTextBox($objForm, $value, "KAIKIN_SEIKIN_TIKOKU", 3, 3, $extra);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //リストToリスト作成(進学希望一覧)
        makeListToListShingaku($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd672fForm1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //対象外の生徒取得
    $query = knjd672fQuery::getSchnoIdou($model);
    $result = $db->query($query);
    $opt_idou = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    $opt_right = $opt_left = array();

    //一覧取得
    $query = knjd672fQuery::getStudent($model);
    $result = $db->query($query);
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
        if (in_array($row["SCHREGNO"], $selectdata)) {
            $opt_left[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]);
        } else {
            $opt_right[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();

    //生徒一覧リスト
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 12);
    //出力対象一覧リスト
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 12);

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

//リストToリスト作成(進学希望一覧)
function makeListToListShingaku(&$objForm, &$arg, $db, $model) {
    $opt_right = $opt_left = array();

    //一覧取得
    $query = knjd672fQuery::getShingaku();
    $result = $db->query($query);
    $selectdata_sg = ($model->selectdata_sg) ? explode(',', $model->selectdata_sg) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selectdata_sg)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        } else {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧リスト
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move2('left')\"";
    $arg["data"]["SHINGAKU_NAME"] = knjCreateCombo($objForm, "SHINGAKU_NAME", "", $opt_right, $extra, 3);
    //出力対象一覧リスト
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move2('right')\"";
    $arg["data"]["SHINGAKU_SELECTED"] = knjCreateCombo($objForm, "SHINGAKU_SELECTED", "", $opt_left, $extra, 3);

    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move2('right');\"";
    $arg["button"]["btn_right1_sg"] = knjCreateBtn($objForm, "btn_right1_sg", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move2('left');\"";
    $arg["button"]["btn_left1_sg"] = knjCreateBtn($objForm, "btn_left1_sg", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    if ($name == "TANTOU") {
      $opt[] = array('label' => '',
                     'value' => '');
    }
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name != "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    } else {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD672F");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdata_sg");
    knjCreateHidden($objForm, "KESSEKI_KANSAN", $model->field["KESSEKI_KANSAN"]);
    knjCreateHidden($objForm, "KAIKIN_KESSEKI", $model->field["KAIKIN_KESSEKI"]);
    knjCreateHidden($objForm, "SEIKIN_KESSEKI", $model->field["SEIKIN_KESSEKI"]);
}
?>
