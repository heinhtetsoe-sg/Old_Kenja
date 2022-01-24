<?php

require_once('for_php7.php');

class knjb1221Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb1221Form1", "POST", "knjb1221index.php", "", "knjb1221Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //コースコンボ
        $query = knjb1221Query::getCourse();
        $extra = "onchange=\"return btn_submit('changeCourse')\"";
        makeCmb($objForm, $arg, $db, $query, "COURSE", $model->field["COURSE"], $extra, 1);

        //年組コンボ
        $query = knjb1221Query::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('changeHr')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //コピー元生徒コンボ
        $query = knjb1221Query::getStudent($model);
        $extra = "onchange=\"return btn_submit('changeStudent')\"";
        makeCmb($objForm, $arg, $db, $query, "STUDENT", $model->field["STUDENT"], $extra, 1);

        //コピー元講座日付初期値セット
        if ($model->field["COPY_CHAIR_DATE"] == "") $model->field["COPY_CHAIR_DATE"] = str_replace("-", "/", CTRL_DATE);
        //コピー元講座日付（テキスト）
        $extra = "onblur=\"isDate(this); tmp_list('changeCopyChairDate', 'on')\"";
        $date_textbox = knjCreateTextBox($objForm, $model->field["COPY_CHAIR_DATE"], "COPY_CHAIR_DATE", 12, 12, $extra);
        //コピー元講座日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('changeCopyChairDate', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=COPY_CHAIR_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;scrollY = window.innerHeight > event.clientY + 200 ? scrollY : scrollY - 200;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_copy_chair_calen", "･･･", $extra);
        //コピー元講座日付
        $arg["data"]["COPY_CHAIR_DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //リストToリスト作成 講座
        makeListToListChair($objForm, $arg, $db, $model);

        //更新対象年組コンボ
        $query = knjb1221Query::getTrgtGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('changeTrgtHr')\"";
        makeCmb($objForm, $arg, $db, $query, "TRGT_GRADE_HR_CLASS", $model->field["TRGT_GRADE_HR_CLASS"], $extra, 1);

        //開始日初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        //開始日（テキスト）
        $extra = "onblur=\"isDate(this); tmp_list('changeDate', 'on')\"";
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //開始日（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('changeDate', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;scrollY = window.innerHeight > event.clientY + 200 ? scrollY : scrollY - 200;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //開始日
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //リストToリスト作成 生徒
        makeListToListStd($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1221Form1.html", $arg);
    }
}

//リストTOリスト作成 講座
function makeListToListChair(&$objForm, &$arg, $db, &$model) {
    //初期化
    $optRight = $optLeft = array();
    $selectdataChair  = array();
    $selectdataChairText = array();

    //履修講座リスト
    if ($model->cmd == "changeTrgtHr" || $model->cmd == "changeDate" || $model->cmd == "update") {
        //左リストに移動された講座
        $selectdataChair = ($model->selectdataChair != "") ? explode("|", $model->selectdataChair) : array();
        $selectdataChairText = ($model->selectdataChairText != "") ? explode("|", $model->selectdataChairText) : array();
    } else {
        //履修講座を取得
        $result = $db->query(knjb1221Query::getChairStd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $selectdataChair[] = $row["CHAIRCD"];
            $selectdataChairText[] = $row["CHAIRCD"]."　".$row["CHAIRNAME"];
        }
        $result->free();
    }
    for ($i = 0; $i < get_count($selectdataChair); $i++) {
        $optLeft[] = array("label" => $selectdataChairText[$i],
            "value" => $selectdataChair[$i]);
    }

    //開講講座リスト
    $query = knjb1221Query::getCourseChair($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["CHAIRCD"], $selectdataChair)) {
            $optRight[] = array('label' => $row["CHAIRCD"]."　".$row["CHAIRNAME"],
                'value' => $row["CHAIRCD"]);
        }
    }
    $result->free();

    makeListToList($objForm, $arg, $optRight, $optLeft, "CHAIR_SELECTED", "CHAIR_NAME", "chair_button");

}

//リストTOリスト作成 生徒
function makeListToListStd(&$objForm, &$arg, $db, $model) {
    //対象外の生徒取得
    $query = knjb1221Query::getSchnoIdou($model);
    $result = $db->query($query);
    $optIdou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optIdou[] = $row["SCHREGNO"];
    }
    $result->free();

    //初期化
    $optRight = $optLeft = array();

    //左リストに移動された生徒を再セット
    $selectdataStd = ($model->selectdataStd != "") ? explode("|", $model->selectdataStd) : array();
    $selectdataStdText = ($model->selectdataStdText != "") ? explode("|", $model->selectdataStdText) : array();
    for ($i = 0; $i < get_count($selectdataStd); $i++) {
        $optLeft[] = array("label" => $selectdataStdText[$i],
                            "value" => $selectdataStd[$i]);
    }

    //対象者リスト
    $query = knjb1221Query::getTrgtStudent($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $idou = (in_array($row["SCHREGNO"], $optIdou)) ? "●" : "　";
        if (!in_array($row["SCHREGNO"], $selectdataStd)) {
            $optRight[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();

    makeListToList($objForm, $arg, $optRight, $optLeft, "STD_SELECTED", "STD_NAME", "std_button");

}

//リストTOリスト作成 共通部分
function makeListToList(&$objForm, &$arg, $optRight, $optLeft, $categorySelected, $categoryName, $button_name) {
    //一覧リスト（右）
    $extra = "multiple style=\"width:250px; height:200px\" ondblclick=\"move1('left', '".$categoryName."', '".$categorySelected."')\"";
    $arg["data"][$categoryName] = knjCreateCombo($objForm, $categoryName, "", $optRight, $extra, 20);

    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:250px; height:200px\" ondblclick=\"move1('right', '".$categoryName."', '".$categorySelected."')\"";
    $arg["data"][$categorySelected] = knjCreateCombo($objForm, $categorySelected, "", $optLeft, $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '".$categoryName."', '".$categorySelected."');\"";
    $arg[$button_name]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '".$categoryName."', '".$categorySelected."');\"";
    $arg[$button_name]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '".$categoryName."', '".$categorySelected."');\"";
    $arg[$button_name]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '".$categoryName."', '".$categorySelected."');\"";
    $arg[$button_name]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJB1221");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdataStd");
    knjCreateHidden($objForm, "selectdataStdText");
    knjCreateHidden($objForm, "selectdataChair");
    knjCreateHidden($objForm, "selectdataChairText");
    knjCreateHidden($objForm, "sdate", $model->sdate);
    knjCreateHidden($objForm, "edate", $model->edate);
}
?>
