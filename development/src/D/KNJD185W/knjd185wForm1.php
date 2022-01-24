<?php

require_once('for_php7.php');

class knjd185wForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd185wForm1", "POST", "knjd185windex.php", "", "knjd185wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd185wQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd185w'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年組コンボ
        $query = knjd185wQuery::getGradeHrClass($model, $seme);
        $extra = "onchange=\"return btn_submit('knjd185w'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd185w', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd185w', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $seme);

        //設定の初期値取得
        $dataTmp = array();
        $query = knjd185wQuery::getHreportConditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTmp[$row["SEQ"]] = $row;
        }

        //帳票出力ラジオボタン 1:A 2:B 3:C 4:D 5:E 6:F
        $opt001 = array(1, 2, 3, 4, 5, 6);
        $SEQ001 = $dataTmp["001"]["REMARK1"] ? $dataTmp["001"]["REMARK1"] : "1";
        $extra = array("id=\"SEQ0011\" disabled=\"disabled\"", "id=\"SEQ0012\" disabled=\"disabled\"", "id=\"SEQ0013\" disabled=\"disabled\"",
                       "id=\"SEQ0014\" disabled=\"disabled\"", "id=\"SEQ0015\" disabled=\"disabled\"", "id=\"SEQ0016\" disabled=\"disabled\"");
        $radioArray = knjCreateRadio($objForm, "SEQ001", $SEQ001, $extra, $opt001, get_count($opt001));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $opt005 = array(1, 2);
        $SEQ005 = $dataTmp["005"]["REMARK1"] ? $dataTmp["005"]["REMARK1"] : "1";
        $extra = array("id=\"SEQ0051\" disabled=\"disabled\"", "id=\"SEQ0052\" disabled=\"disabled\"");
        $radioArray = knjCreateRadio($objForm, "SEQ005", $SEQ005, $extra, $opt005, get_count($opt005));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //席次（クラス）チェックボックス
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK1', 'SEQ0061');
        
        //席次（コース）チェックボックス
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK2', 'SEQ0062');
        
        //席次（学年）チェックボックス
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK3', 'SEQ0063');
        
        //席次（学科）チェックボックス
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK4', 'SEQ0064');

        //順位表記
        makeCheckBox($objForm, $model, $dataTmp, $arg, '012', 'REMARK1', 'SEQ012');
        
        //追指導表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '013', 'REMARK1', 'SEQ013');
        
        //欠点表示
        $SEQ014 = $dataTmp["014"]["REMARK1"] ? $dataTmp["014"]["REMARK1"] : (is_array($dataTmp["014"]) ? "" : "40");
        $extra = "onblur=\"this.value=toInteger(this.value)\" disabled=\"disabled\"";
        $arg["data"]["SEQ014"] = knjCreateTextBox($objForm, $SEQ014, "SEQ014", 4, 4, $extra);
        
        //欠課時数
        $SEQ0151 = $dataTmp["015"]["REMARK1"] ? $dataTmp["015"]["REMARK1"] : "1";
        $extra = "onblur=\"this.value=toInteger(this.value)\" disabled=\"disabled\"";
        $arg["data"]["SEQ0151"] = knjCreateTextBox($objForm, $SEQ0151, "SEQ0151", 4, 4, $extra);
        
        $SEQ0152 = $dataTmp["015"]["REMARK2"] ? $dataTmp["015"]["REMARK2"] : "3";
        $extra = "onblur=\"this.value=toInteger(this.value)\" disabled=\"disabled\"";
        $arg["data"]["SEQ0152"] = knjCreateTextBox($objForm, $SEQ0152, "SEQ0152", 4, 4, $extra);
        
        //出力順設定
        makeCheckBox($objForm, $model, $dataTmp, $arg, '016', 'REMARK1', 'SEQ016');
        
        //総合的な学習の時間
        makeCheckBox($objForm, $model, $dataTmp, $arg, '008', 'REMARK1', 'SEQ008');
        
        //特別活動
        makeCheckBox($objForm, $model, $dataTmp, $arg, '007', 'REMARK1', 'SEQ007');
        
        //所見欄
        makeCheckBox($objForm, $model, $dataTmp, $arg, '017', 'REMARK1', 'SEQ017');
        
        //科目担当教員
        makeCheckBox($objForm, $model, $dataTmp, $arg, '018', 'REMARK1', 'SEQ018');
        
        //出欠の記録
        makeCheckBox($objForm, $model, $dataTmp, $arg, '019', 'REMARK1', 'SEQ019');
        
        //追指導表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '020', 'REMARK1', 'SEQ020');
        
        //評定表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '021', 'REMARK1', 'SEQ021');
        
        //増加単位を加算する 1:加算する 2:加算しない
        $opt022 = array(1, 2);
        $model->field["SEQ022"] = ($model->field["SEQ022"]) ? $model->field["SEQ022"] : ($dataTmp["022"]["REMARK1"] ? $dataTmp["022"]["REMARK1"] : "2");
        $extra = array("id=\"SEQ0221\" disabled=\"disabled\"", "id=\"SEQ0222\" disabled=\"disabled\"",);
        $radioArray = knjCreateRadio($objForm, "SEQ022", $model->field["SEQ022"], $extra, $opt022, get_count($opt022));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
  
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd185wForm1.html", $arg);
    }
}

//チェックボックス作成
function makeCheckBox($objForm, $model, $dataTmp, &$arg, $fieldSEQ, $remark, $id){
    $extra = "";
    if ((!get_count($dataTmp)) ||
        (get_count($dataTmp) > 0 && $dataTmp[$fieldSEQ][$remark] == "1")) {
        $extra = "checked";
    }
    $extra .= " id=\"{$id}\" disabled=\"disabled\"";
    $arg["data"][$id] = knjCreateCheckBox($objForm, $id, "1", $extra, "");
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $seme) {
    //対象外の生徒取得
    $query = knjd185wQuery::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //対象者リスト
    $query = knjd185wQuery::getStudent($model, $seme);
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
    knjCreateHidden($objForm, "PRGID", "KNJD185W");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H", $model->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H"]);
    knjCreateHidden($objForm, "HREPORTREMARK_DAT_SIZE_H", $model->Properties["HREPORTREMARK_DAT_SIZE_H"]);
    knjCreateHidden($objForm, "tutihyoUseSubclassElectdivAsShoworder", $model->Properties["tutihyoUseSubclassElectdivAsShoworder"]);
    knjCreateHidden($objForm, "tutihyoPrintSubclasscd90Over", $model->Properties["tutihyoPrintSubclasscd90Over"]);
    knjCreateHidden($objForm, "knjd185wNotPrintAttendSubclassDatLesson", $model->Properties["knjd185wNotPrintAttendSubclassDatLesson"]);
    knjCreateHidden($objForm, "knjd185wNotPrintNotPrintZenkiKamokuInSemester2", $model->Properties["knjd185wNotPrintNotPrintZenkiKamokuInSemester2"]);
}
?>
