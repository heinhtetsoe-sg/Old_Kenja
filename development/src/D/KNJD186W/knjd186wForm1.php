<?php

require_once('for_php7.php');

class knjd186wForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd186wForm1", "POST", "knjd186windex.php", "", "knjd186wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd186wQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd186w'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年組コンボ
        $query = knjd186wQuery::getGradeHrClass($model, $seme);
        $extra = "onchange=\"return btn_submit('knjd186w'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd186w', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd186w', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $seme);

        //設定の初期値取得
        $dataTmp = array();
        $query = knjd186wQuery::getHreportConditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTmp[$row["SEQ"]] = $row;
        }

        //帳票出力ラジオボタン 1:A 2:B 3:C
        knjCreateHidden($objForm, "SEQ001", $dataTmp["001"]["REMARK1"] ? $dataTmp["001"]["REMARK1"] : "1");

        //科目ラジオボタン 1:科目名 2:講座名 3:講座番号(下3桁)付講座名
        knjCreateHidden($objForm, "SEQ002", $dataTmp["002"]["REMARK1"] ? $dataTmp["002"]["REMARK1"] : "1");

        //年組番／学籍番号ラジオボタン 1:年組番号 2:学籍番号
        knjCreateHidden($objForm, "SEQ003", $dataTmp["003"]["REMARK1"] ? $dataTmp["003"]["REMARK1"] : "1");

        //欠課時数ラジオボタン 1:欠課数 2:欠課数／時間数 3:出席数／時数
        knjCreateHidden($objForm, "SEQ004", $dataTmp["004"]["REMARK1"] ? $dataTmp["004"]["REMARK1"] : "1");

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        knjCreateHidden($objForm, "SEQ005", $dataTmp["005"]["REMARK1"] ? $dataTmp["005"]["REMARK1"] : "1");

        //平均・順位チェックボックス
        for ($i = 1; $i <= 3; $i++) {
            if ($dataTmp["006"]["REMARK".$i] == "1") {
                knjCreateHidden($objForm, "SEQ006".$i, "1");
            }
        }

        //特別活動の記録ラジオボタン 1:表示する 2:表示しない
        knjCreateHidden($objForm, "SEQ007", $dataTmp["007"]["REMARK1"] ? $dataTmp["007"]["REMARK1"] : "1");

        //総合的な学習の時間/検定ラジオボタン 1:表示する 2:表示しない
        knjCreateHidden($objForm, "SEQ008", $dataTmp["008"]["REMARK1"] ? $dataTmp["008"]["REMARK1"] : "1");

        //備考ラジオボタン 1:表示する 2:表示しない
        knjCreateHidden($objForm, "SEQ009", $dataTmp["009"]["REMARK1"] ? $dataTmp["009"]["REMARK1"] : "1");

        //定型コメントラジオボタン 1:表示する 2:表示しない
        knjCreateHidden($objForm, "SEQ010", $dataTmp["010"]["REMARK1"] ? $dataTmp["010"]["REMARK1"] : "1");

        //未履修科目コメントラジオボタン 1:表示する 2:表示しない
        knjCreateHidden($objForm, "SEQ011", $dataTmp["011"]["REMARK1"] ? $dataTmp["011"]["REMARK1"] : "1");

        //増加単位 1:加算する 2:加算しない
        knjCreateHidden($objForm, "SEQ021", $dataTmp["021"]["REMARK1"] ? $dataTmp["021"]["REMARK1"] : "2");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd186wForm1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $seme) {
    //対象外の生徒取得
    $query = knjd186wQuery::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //対象者リスト
    $query = knjd186wQuery::getStudent($model, $seme);
    $result = $db->query($query);
    $opt_right = $opt_left = array();
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
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //生徒一覧リスト
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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
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
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD186W");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H", $model->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_SIZE_H", $model->Properties["HREPORTREMARK_DAT_SIZE_H"]);
}
?>
