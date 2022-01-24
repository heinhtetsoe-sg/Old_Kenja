<?php

require_once('for_php7.php');

class knjf150aSubForm2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjf150aindex.php", "", "subform2");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //種別区分（外科）
        $model->type ='2';

        //データ取得
        if (($model->cmd == "subform2A") || ($model->cmd == "subform2_clear")) {
            if (isset($model->schregno) && !isset($model->warning)) {
                $row = $db->getRow(knjf150aQuery::getRow($model), DB_FETCHMODE_ASSOC);
            } else {
                $row =& $model->field;
            }
        } else {
            $row =& $model->field;
        }

        //submitしたら直近のcmdをセット
        if ($model->cmd == "subform2B") {
            $model->cmd = $model->cmd_keep;
        }

        //extra
        $extra_int = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $extra_flote = " onblur=\"this.value=toFlote(this.value)\"";

        //生徒情報
        $hr_name = $db->getOne(knjf150aQuery::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        /******************/
        /*    来室日時    */
        /******************/

        //来室日付初期値セット
        $value = ($row["VISIT_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["VISIT_DATE"]);
        //来室日付（テキスト）
        $extra = "onblur=\"isDate(this); calendarSubmit('subform2B', 'on')\"";
        $date_textbox = knjCreateTextBox($objForm, $value, "VISIT_DATE", 12, 12, $extra);
        //来室日付（カレンダー）
        global $sess;
        $extra = "onclick=\"calendarSubmit('subform2B', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=VISIT_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['VISIT_DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //来室日付
        $arg["data"]["VISIT_DATE"] = View::setIframeJs().$date_textbox.$date_button;
        //来室日付（曜日）
        makeWeekday($arg, "VISIT_DATE", $value);

        //来室時間（時）
        $arg["data"]["VISIT_HOUR"] = knjCreateTextBox($objForm, $row["VISIT_HOUR"], "VISIT_HOUR", 2, 2, $extra_int);

        //来室時間（分）
        $arg["data"]["VISIT_MINUTE"] = knjCreateTextBox($objForm, $row["VISIT_MINUTE"], "VISIT_MINUTE", 2, 2, $extra_int);

        //現在の教科
        $arg["data"]["LESSON_CLASS"] = knjCreateTextBox($objForm, $row["LESSON_CLASS"], "LESSON_CLASS", 30, 40, "");

        //来室校時ラベル取得
        $query = knjf150aQuery::getNameMstPeriod();
        $label_array = makeLabel($db, $query);
        //来室校時チェックボックス 01～15, 16.未記入(BLANK)
        $opt_visit_periodcd = array();
        for ($i = 1; $i < 16; $i++) {
            $opt_visit_periodcd[$i] = sprintf("%02d", $i);
        }
        knjCreateHidden($objForm, "SEQ11_REMARK1_OPT", implode(",", $opt_visit_periodcd));

        $opt_visit_periodcd[] = "BLANK";

        $extra = " onclick=\"OptionUse3(this, 'SEQ11_REMARK1', '');\"";
        $aryValue = array();
        makeChceckbox($objForm, $arg, "SEQ11_REMARK1", $row["SEQ11_REMARK1"], $extra, $opt_visit_periodcd, $label_array, $aryValue);

        /******************/
        /*    来室理由    */
        /******************/

        //来室理由のテキスト入力する項目
        knjCreateHidden($objForm, "VISIT_REASON_LIST", "10");

        //来室理由１コンボ
        $query = knjf150aQuery::getNameMst('F201');
        $extra = "onChange=\"OptionUse(this, 'VISIT_REASON_LIST', 'VISIT_REASON1_TEXT');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON1", $row["VISIT_REASON1"], $extra, 1);
        //テキスト
        $extra = ($row["VISIT_REASON1"] == "10") ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["VISIT_REASON1_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON1_TEXT"], "VISIT_REASON1_TEXT", 40, 60, $extra);

        //来室理由２コンボ作成
        $extra = "onChange=\"OptionUse(this, 'VISIT_REASON_LIST', 'VISIT_REASON2_TEXT');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON2", $row["VISIT_REASON2"], $extra, 1);
        //テキスト
        $extra = ($row["VISIT_REASON2"] == "10") ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["VISIT_REASON2_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON2_TEXT"], "VISIT_REASON2_TEXT", 40, 60, $extra);

        //来室理由３コンボ作成
        $extra = "onChange=\"OptionUse(this, 'VISIT_REASON_LIST', 'VISIT_REASON3_TEXT');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON3", $row["VISIT_REASON3"], $extra, 1);
        //テキスト
        $extra = ($row["VISIT_REASON3"] == "10") ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["VISIT_REASON3_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON3_TEXT"], "VISIT_REASON3_TEXT", 40, 60, $extra);

        /********************/
        /*    けがの部位    */
        /********************/

        //けがの部位のテキスト入力する項目
        knjCreateHidden($objForm, "INJURY_PART_LIST", "05");

        //けがの部位１コンボ
        $query = knjf150aQuery::getNameMst('F207');
        $extra = "onChange=\"OptionUse(this, 'INJURY_PART_LIST', 'INJURY_PART1_TEXT');\"";
        makeCmb($objForm, $arg, $db, $query, "INJURY_PART1", $row["INJURY_PART1"], $extra, 1);
        //テキスト
        $extra = ($row["INJURY_PART1"] == "05") ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["INJURY_PART1_TEXT"] = knjCreateTextBox($objForm, $row["INJURY_PART1_TEXT"], "INJURY_PART1_TEXT", 40, 60, $extra);

        //けがの部位２コンボ作成
        $extra = "onChange=\"OptionUse(this, 'INJURY_PART_LIST', 'INJURY_PART2_TEXT');\"";
        makeCmb($objForm, $arg, $db, $query, "INJURY_PART2", $row["INJURY_PART2"], $extra, 1);
        //テキスト
        $extra = ($row["INJURY_PART2"] == "05") ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["INJURY_PART2_TEXT"] = knjCreateTextBox($objForm, $row["INJURY_PART2_TEXT"], "INJURY_PART2_TEXT", 40, 60, $extra);

        //けがの部位３コンボ作成
        $extra = "onChange=\"OptionUse(this, 'INJURY_PART_LIST', 'INJURY_PART3_TEXT');\"";
        makeCmb($objForm, $arg, $db, $query, "INJURY_PART3", $row["INJURY_PART3"], $extra, 1);
        //テキスト
        $extra = ($row["INJURY_PART3"] == "05") ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["INJURY_PART3_TEXT"] = knjCreateTextBox($objForm, $row["INJURY_PART3_TEXT"], "INJURY_PART3_TEXT", 40, 60, $extra);

        /****************/
        /*    発生時    */
        /****************/

        //発生日付初期値セット
        $value = ($row["OCCUR_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["OCCUR_DATE"]);
        //発生日付（テキスト）
        $extra = "onblur=\"isDate(this); calendarSubmit('subform2B', 'on')\"";
        $date_textbox = knjCreateTextBox($objForm, $value, "OCCUR_DATE", 12, 12, $extra);
        //発生日付（カレンダー）
        global $sess;
        $extra = "onclick=\"calendarSubmit('subform2B', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=OCCUR_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['OCCUR_DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //発生日付
        $arg["data"]["OCCUR_DATE"] = View::setIframeJs().$date_textbox.$date_button;
        //発生日付（曜日）
        makeWeekday($arg, "OCCUR_DATE", $value);

        //発生時間（時）
        $arg["data"]["OCCUR_HOUR"] = knjCreateTextBox($objForm, $row["OCCUR_HOUR"], "OCCUR_HOUR", 2, 2, $extra_int);

        //発生時間（分）
        $arg["data"]["OCCUR_MINUTE"] = knjCreateTextBox($objForm, $row["OCCUR_MINUTE"], "OCCUR_MINUTE", 2, 2, $extra_int);

        //発生時の場合ラベル取得
        $query = knjf150aQuery::getNameMst('F216');
        $label_array = makeLabel($db, $query);

        //発生時の場合ラジオボタン 01～08, 9.未記入(BLANK)
        $opt_occur_act = $extra = array();
        for ($i = 1; $i < 9; $i++) {
            $opt_occur_act[$i] = sprintf("%02d", $i);
        }
        $opt_occur_act[] = "BLANK";
        $value = ($row["OCCUR_ACT"] == "" || $row["OCCUR_ACT"] == "BLANK") ? "BLANK" : $row["OCCUR_ACT"];
        foreach ($opt_occur_act as $key => $val) {
            $extra[] = "id=\"OCCUR_ACT".$val."\"";
        }
        createRadio2($objForm, $arg, "OCCUR_ACT", $value, $extra, $opt_occur_act, get_count($opt_occur_act), $label_array);
        //テキスト
        $extra = "";
        $arg["data"]["OCCUR_SITUATION"] = knjCreateTextBox($objForm, $row["OCCUR_SITUATION"], "OCCUR_SITUATION", 40, 60, $extra);

        //発生時の場所ラベル取得
        $query = knjf150aQuery::getNameMst('F206');
        $label_array = makeLabel($db, $query);

        //発生時の場所ラジオボタン 01～08, 9.未記入(BLANK)
        $opt_occur_place = $extra = array();
        for ($i = 1; $i < 9; $i++) {
            $opt_occur_place[$i] = sprintf("%02d", $i);
        }
        $opt_occur_place[] = "BLANK";
        $value = ($row["OCCUR_PLACE"] == "" || $row["OCCUR_PLACE"] == "BLANK") ? "BLANK" : $row["OCCUR_PLACE"];
        foreach ($opt_occur_place as $key => $val) {
            $extra[] = "id=\"OCCUR_PLACE".$val."\"";
        }
        createRadio2($objForm, $arg, "OCCUR_PLACE", $value, $extra, $opt_occur_place, get_count($opt_occur_place), $label_array);
        //テキスト
        $extra = "";
        $arg["data"]["OCCUR_PLACE_TEXT"] = knjCreateTextBox($objForm, $row["OCCUR_PLACE_TEXT"], "OCCUR_PLACE_TEXT", 40, 60, $extra);

        /**************/
        /*    処置    */
        /**************/

        //処置ラベル取得
        $query = knjf150aQuery::getNameMst('F209');
        $label_array = makeLabel($db, $query);

        //処置１ラジオボタン 01～08, 9.未記入(BLANK)
        $opt_treatment1 = $extra = array();
        for ($i = 1; $i < 9; $i++) {
            $opt_treatment1[$i] = sprintf("%02d", $i);
        }
        $opt_treatment1[] = "BLANK";
        $value = ($row["TREATMENT1"] == "" || $row["TREATMENT1"] == "BLANK") ? "BLANK" : $row["TREATMENT1"];
        foreach ($opt_treatment1 as $key => $val) {
            $extra[] = "id=\"TREATMENT1".$val."\"";
        }
        createRadio2($objForm, $arg, "TREATMENT1", $value, $extra, $opt_treatment1, get_count($opt_treatment1), $label_array);
        //テキスト
        $extra = "";
        $arg["data"]["TREATMENT1_TEXT"] = knjCreateTextBox($objForm, $row["TREATMENT1_TEXT"], "TREATMENT1_TEXT", 40, 60, $extra);

        //処置２ラジオボタン 01～08, 9.未記入(BLANK)
        $opt_treatment2 = $extra = array();
        for ($i = 1; $i < 9; $i++) {
            $opt_treatment2[$i] = sprintf("%02d", $i);
        }
        $opt_treatment2[] = "BLANK";
        $value = ($row["TREATMENT2"] == "" || $row["TREATMENT2"] == "BLANK") ? "BLANK" : $row["TREATMENT2"];
        foreach ($opt_treatment2 as $key => $val) {
            $extra[] = "id=\"TREATMENT2".$val."\"";
        }
        createRadio2($objForm, $arg, "TREATMENT2", $value, $extra, $opt_treatment2, get_count($opt_treatment2), $label_array);
        //テキスト
        $extra = "";
        $arg["data"]["TREATMENT2_TEXT"] = knjCreateTextBox($objForm, $row["TREATMENT2_TEXT"], "TREATMENT2_TEXT", 40, 60, $extra);

        //処置３ラジオボタン 01～08, 9.未記入(BLANK)
        $opt_treatment3 = $extra = array();
        for ($i = 1; $i < 9; $i++) {
            $opt_treatment3[$i] = sprintf("%02d", $i);
        }
        $opt_treatment3[] = "BLANK";
        $value = ($row["TREATMENT3"] == "" || $row["TREATMENT3"] == "BLANK") ? "BLANK" : $row["TREATMENT3"];
        foreach ($opt_treatment3 as $key => $val) {
            $extra[] = "id=\"TREATMENT3".$val."\"";
        }
        createRadio2($objForm, $arg, "TREATMENT3", $value, $extra, $opt_treatment3, get_count($opt_treatment3), $label_array);
        //テキスト
        $extra = "";
        $arg["data"]["TREATMENT3_TEXT"] = knjCreateTextBox($objForm, $row["TREATMENT3_TEXT"], "TREATMENT3_TEXT", 40, 60, $extra);

        /**********************************/
        /*    在室時間・連絡・特記事項    */
        /**********************************/

        //在室時間ラベル取得
        $query = knjf150aQuery::getNameMst('F212');
        $label_array = makeLabel($db, $query);
        //在室時間ラジオボタン 01～09, 10.未記入(BLANK)
        $opt_resttime = $extra = array();
        for ($i = 1; $i < 10; $i++) {
            $opt_resttime[$i] = sprintf("%02d", $i);
        }
        $opt_resttime[] = "BLANK";
        $value = ($row["RESTTIME"] == "" || $row["RESTTIME"] == "BLANK") ? "BLANK" : $row["RESTTIME"];
        foreach ($opt_resttime as $key => $val) {
            $extra[] = "id=\"RESTTIME".$val."\"";
        }
        createRadio2($objForm, $arg, "RESTTIME", $value, $extra, $opt_resttime, get_count($opt_resttime), $label_array);

        //連絡ラベル取得
        $query = knjf150aQuery::getNameMst('F213');
        $label_array = makeLabel($db, $query);

        //連絡チェックボックス 01～09, 10.未記入(BLANK)
        $opt_contact = array();
        for ($i = 1; $i < 10; $i++) {
            $opt_contact[$i] = sprintf("%02d", $i);
        }
        knjCreateHidden($objForm, "SEQ15_REMARK1_OPT", implode(",", $opt_contact));

        $opt_contact[] = "BLANK";

        $extra = " onclick=\"OptionUse3(this, 'SEQ15_REMARK1', '');\"";
        $aryValue = array();
        makeChceckbox($objForm, $arg, "SEQ15_REMARK1", $row["SEQ15_REMARK1"], $extra, $opt_contact, $label_array, $aryValue);

        //テキスト
        $extra = "";
        $arg["data"]["CONTACT_TEXT"] = knjCreateTextBox($objForm, $row["CONTACT_TEXT"], "CONTACT_TEXT", 40, 60, $extra);

        //特記事項テキスト
        $moji = 44;
        $gyou = 8;
        $extra = "";
        $arg["data"]["SPECIAL_NOTE"] = knjCreateTextArea($objForm, "SPECIAL_NOTE", $gyou, $moji * 2 + 1, "soft", $extra, $row["SPECIAL_NOTE"]);
        $arg["data"]["SPECIAL_NOTE_COMMENT"] = "(全角{$moji}文字X{$gyou}行まで)";

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf150aSubForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //登録ボタン
    $update = ($model->cmd == "subform2") ? "insert" : "update";
    $extra = "onclick=\"return btn_submit('".$update."');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", $extra);
    if ($model->cmd != "subform2") {
        //取消ボタン
        $extra = "onclick=\"return btn_submit('subform2_clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    }
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "TYPE", $model->type);
    knjCreateHidden($objForm, "cmd_keep", $model->cmd);

    //印刷用
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJF150A");
    knjCreateHidden($objForm, "PRINT_VISIT_DATE", $model->visit_date);
    knjCreateHidden($objForm, "PRINT_VISIT_HOUR", $model->visit_hour);
    knjCreateHidden($objForm, "PRINT_VISIT_MINUTE", $model->visit_minute);
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) {
            $ext = $extra[$i-1];
        } else {
            $ext = $extra;
        }

        $objForm->ae(array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));

        $arg["data"][$name.$multi[$i]]  = $objForm->ge($name, $multi[$i]);
    }
}

//ラベル作成
function makeLabel(&$db, $query)
{
    $label_array = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $label_array[$row["VALUE"]] = $row["LABEL2"];
    }
    $result->free();
    $label_array["BLANK"] = "未記入";

    return $label_array;
}

//ラジオ作成（ラベル付）
function createRadio2(&$objForm, &$arg, $name, $value, $extra, $multi, $count, $label_array)
{
    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) {
            $ext = $extra[$i-1];
        } else {
            $ext = $extra;
        }

        $objForm->ae(array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));


        $label = "<LABEL for=\"".$name.$multi[$i]."\">".$label_array[$multi[$i]]."</LABEL>";

        $arg["data"][$name.$multi[$i]]  = $objForm->ge($name, $multi[$i]).$label;
    }
}

//チェックボックス作成（ラベル付）
function makeChceckbox(&$objForm, &$arg, $name, $value, $extra, $opt, $label_array, &$aryValue)
{
    $aryValue = explode(",", $value);

    foreach ($opt as $idx) {
        $checked = "";
        $fieldName = $name."_".$idx;
        $label = "<LABEL for=\"".$fieldName."\">".$label_array[$idx]."</LABEL>";

        $disabled = "";
        if ($value == "") {
            $disabled = " disabled";
        }

        if ($idx == "BLANK" && $value == "") {
            $checked = " checked";
            $disabled = "";
        } else {
            if (in_array($idx, $aryValue)) {
                $checked = " checked";
            }
        }

        $setExtra = "id=\"".$fieldName."\"".$checked.$extra.$disabled;
        $arg["data"][$fieldName] = knjCreateCheckBox($objForm, $fieldName, "1", $setExtra).$label;
    }
}

//曜日作成
function makeWeekday(&$arg, $name, $value)
{
    $weekday = array("日", "月", "火", "水", "木", "金", "土");
    list($year, $month, $day) = explode('/', $value);
    $week = ($value) ? '('.$weekday[date("w", mktime(0, 0, 0, $month, $day, $year))].')' : "";

    //曜日
    $arg["data"][$name."_WEEKDAY"] = $week;
    $arg["data"][$name."_WEEKDAY_ID"] = mb_strtolower($name."_WEEKDAY_ID");
}
