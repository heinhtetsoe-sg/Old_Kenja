<?php

require_once('for_php7.php');

class knjf150aSubForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjf150aindex.php", "", "subform1");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //種別区分（内科）
        $model->type ='1';

        //データ取得
        if (($model->cmd == "subform1A") || ($model->cmd == "subform1_clear")) {
            if (isset($model->schregno) && !isset($model->warning)) {
                $row = $db->getRow(knjf150aQuery::getRow($model), DB_FETCHMODE_ASSOC);
            } else {
                $row =& $model->field;
            }
        } else {
            $row =& $model->field;
        }

        //submitしたら直近のcmdをセット
        if ($model->cmd == "subform1B") {
            $model->cmd = $model->cmd_keep;
        }

        //extra
        $extra_int = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $extra_Decimal = "onblur=\"checkDecimal(this)\"";

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
        $extra = "onblur=\"isDate(this); calendarSubmit('subform1B', 'on')\"";
        $date_textbox = knjCreateTextBox($objForm, $value, "VISIT_DATE", 12, 12, $extra);
        //来室日付（カレンダー）
        global $sess;
        $extra = "onclick=\"calendarSubmit('subform1B', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=VISIT_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['VISIT_DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
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

        /****************************/
        /*    来室理由・いつから    */
        /****************************/

        //症状・来室理由のテキスト入力する項目
        knjCreateHidden($objForm, "VISIT_REASON_LIST", "10");

        //症状・来室理由１コンボ
        $query = knjf150aQuery::getNameMst('F200');
        $extra = "onChange=\"OptionUse(this, 'VISIT_REASON_LIST', 'VISIT_REASON1_TEXT');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON1", $row["VISIT_REASON1"], $extra, 1);
        //テキスト
        $extra = ($row["VISIT_REASON1"] == "10") ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["VISIT_REASON1_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON1_TEXT"], "VISIT_REASON1_TEXT", 40, 60, $extra);

        //症状・来室理由２コンボ作成
        $extra = "onChange=\"OptionUse(this, 'VISIT_REASON_LIST', 'VISIT_REASON2_TEXT');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON2", $row["VISIT_REASON2"], $extra, 1);
        //テキスト
        $extra = ($row["VISIT_REASON2"] == "10") ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["VISIT_REASON2_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON2_TEXT"], "VISIT_REASON2_TEXT", 40, 60, $extra);

        //症状・来室理由３コンボ作成
        $extra = "onChange=\"OptionUse(this, 'VISIT_REASON_LIST', 'VISIT_REASON3_TEXT');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON3", $row["VISIT_REASON3"], $extra, 1);
        //テキスト
        $extra = ($row["VISIT_REASON3"] == "10") ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["VISIT_REASON3_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON3_TEXT"], "VISIT_REASON3_TEXT", 40, 60, $extra);

        //いつからのテキスト入力する項目
        knjCreateHidden($objForm, "SINCE_WHEN_LIST", "4,5");
        //いつからラジオボタン 1～5, 6.未記入(BLANK)
        $opt_since_when = $extra = array();
        for ($i = 1; $i < 6; $i++) {
            $opt_since_when[$i] = $i;
        }
        $opt_since_when[] = "BLANK";
        $value = ($row["SINCE_WHEN"] == "" || $row["SINCE_WHEN"] == "BLANK") ? "BLANK" : $row["SINCE_WHEN"];
        $disable = " onclick=\"OptionUse(this, 'SINCE_WHEN_LIST', 'SINCE_WHEN_TEXT');\"";
        foreach ($opt_since_when as $key => $val) {
            $extra[] = "id=\"SINCE_WHEN".$val."\"".$disable;
        }
        createRadio($objForm, $arg, "SINCE_WHEN", $value, $extra, $opt_since_when, get_count($opt_since_when));
        //テキスト
        $extra = (in_array($row["SINCE_WHEN"], array('4','5'))) ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["SINCE_WHEN_TEXT"] = knjCreateTextBox($objForm, $row["SINCE_WHEN_TEXT"], "SINCE_WHEN_TEXT", 40, 60, $extra);
        //テキストのあとに文言付加
        if ($row["SINCE_WHEN"] == "4") {
            $arg["data"]["SINCE_WHEN_TEXT_ADD"] = "限目頃から";
        }

        /****************/
        /*    体調等    */
        /****************/

        //昨日はよく眠れたか？ラジオボタン 1:よく眠れた 2:あまり眠れなかった 3.未記入(BLANK)
        $opt_condition1 = array(1 => 1, 2 => 2, 3 => "BLANK");
        $value = ($row["CONDITION1"] == "" || $row["CONDITION1"] == "BLANK") ? "BLANK" : $row["CONDITION1"];
        $extra = array("id=\"CONDITION11\"", "id=\"CONDITION12\"", "id=\"CONDITION1BLANK\"");
        createRadio($objForm, $arg, "CONDITION1", $value, $extra, $opt_condition1, get_count($opt_condition1));

        //睡眠時間
        $arg["data"]["SLEEPTIME"] = knjCreateTextBox($objForm, $row["SLEEPTIME"], "SLEEPTIME", 2, 2, $extra_int);
        $arg["data"]["SLEEPTIME_M"] = knjCreateTextBox($objForm, $row["SLEEPTIME_M"], "SLEEPTIME_M", 2, 2, $extra_int);

        //何時に寝たか？
        $arg["data"]["BEDTIME_H"] = knjCreateTextBox($objForm, $row["BEDTIME_H"], "BEDTIME_H", 2, 2, $extra_int);
        $arg["data"]["BEDTIME_M"] = knjCreateTextBox($objForm, $row["BEDTIME_M"], "BEDTIME_M", 2, 2, $extra_int);

        //現在の便通は？ラジオボタン 1:普通 2:下痢 3:便秘 4.未記入(BLANK)
        $opt_condition3 = array(1 => 1, 2 => 2, 3 => 3, 4 => "BLANK");
        $value = ($row["CONDITION3"] == "" || $row["CONDITION3"] == "BLANK") ? "BLANK" : $row["CONDITION3"];
        $extra = array("id=\"CONDITION31\"", "id=\"CONDITION32\"", "id=\"CONDITION33\"", "id=\"CONDITION3BLANK\"");
        createRadio($objForm, $arg, "CONDITION3", $value, $extra, $opt_condition3, get_count($opt_condition3));

        //朝食は食べたか？ラジオボタン 1:食べた 2:食べていない 3:いつも食べない 9.未記入(BLANK)
        $opt_condition4 = array(1 => 1, 2 => 2, 3 => 3, 4 => "BLANK");
        $value = ($row["CONDITION4"] == "" || $row["CONDITION4"] == "BLANK") ? "BLANK" : $row["CONDITION4"];
        $extra = array("id=\"CONDITION41\"", "id=\"CONDITION42\"", "id=\"CONDITION43\"", "id=\"CONDITION4BLANK\"");
        createRadio($objForm, $arg, "CONDITION4", $value, $extra, $opt_condition4, get_count($opt_condition4));

        //具合が悪くなった原因は？チェックボックス 1～8, 9.未記入(BLANK)
        $label_array = array(
            "01"    => "生活習慣（食生活、睡眠、排便など）",
            "02"    => "寝冷え・風邪",
            "03"    => "疲れ",
            "04"    => "月経",
            "05"    => "心のストレス・心配事",
            "06"    => "運動したから",
            "07"    => "その他",
            "08"    => "わからない",
            "BLANK" => "未記入",
        );

        $opt_condition7 = array();
        for ($idx = 1; $idx <= $model->selectDetailSeq["12"]["count"]; $idx++) {
            $opt_condition7[$idx] = sprintf("%02d", $idx);
        }
        knjCreateHidden($objForm, "SEQ12_REMARK1_OPT", implode(",", $opt_condition7));
        knjCreateHidden($objForm, "SEQ12_REMARK1_USE_TEXT", "07");

        $opt_condition7[] = "BLANK";

        $extra = " onclick=\"OptionUse3(this, 'SEQ12_REMARK1', 'CONDITION7_TEXT');\"";
        $aryValue = array();
        makeChceckbox($objForm, $arg, "SEQ12_REMARK1", $row["SEQ12_REMARK1"], $extra, $opt_condition7, $label_array, $aryValue);

        //テキスト
        $extra = (in_array("07", $aryValue)) ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["CONDITION7_TEXT"] = knjCreateTextBox($objForm, $row["CONDITION7_TEXT"], "CONDITION7_TEXT", 40, 60, $extra);

        //今、どうしたいか？チェックボックス 1～7, 8.未記入(BLANK)
        $label_array = array(
            "01"    => "授業に出たい",
            "02"    => "湯たんぽを貸してほしい",
            "03"    => "相談したい",
            "04"    => "休養したい",
            "05"    => "病院に行きたい",
            "06"    => "早退したい",
            "07"    => "その他",
            "BLANK" => "未記入",
        );

        $opt_condition8 = array();
        for ($idx = 1; $idx <= $model->selectDetailSeq["13"]["count"]; $idx++) {
            $opt_condition8[$idx] = sprintf("%02d", $idx);
        }
        knjCreateHidden($objForm, "SEQ13_REMARK1_OPT", implode(",", $opt_condition8));
        knjCreateHidden($objForm, "SEQ13_REMARK1_USE_TEXT", "07");

        $opt_condition8[] = "BLANK";

        $extra = " onclick=\"OptionUse3(this, 'SEQ13_REMARK1', 'CONDITION8_TEXT');\"";
        $aryValue = array();
        makeChceckbox($objForm, $arg, "SEQ13_REMARK1", $row["SEQ13_REMARK1"], $extra, $opt_condition8, $label_array, $aryValue);

        //テキスト
        $extra = (in_array("07", $aryValue)) ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["CONDITION8_TEXT"] = knjCreateTextBox($objForm, $row["CONDITION8_TEXT"], "CONDITION8_TEXT", 40, 60, $extra);

        //学校生活は？ラジオボタン 1:楽しい 2:普通 3:楽しくない 4.未記入(BLANK)
        $opt_condition9 = array(1 => 1, 2 => 2, 3 => 3, 4 => "BLANK");
        $value = ($row["CONDITION9"] == "" || $row["CONDITION9"] == "BLANK") ? "BLANK" : $row["CONDITION9"];
        $extra = array("id=\"CONDITION91\"", "id=\"CONDITION92\"", "id=\"CONDITION93\"", "id=\"CONDITION9BLANK\"");
        createRadio($objForm, $arg, "CONDITION9", $value, $extra, $opt_condition9, get_count($opt_condition9));

        //家庭生活は？ラジオボタン 1:楽しい 2:普通 3:楽しくない 4.未記入(BLANK)
        $opt_condition10 = array(1 => 1, 2 => 2, 3 => 3, 4 => "BLANK");
        $value = ($row["CONDITION10"] == "" || $row["CONDITION10"] == "BLANK") ? "BLANK" : $row["CONDITION10"];
        $extra = array("id=\"CONDITION101\"", "id=\"CONDITION102\"", "id=\"CONDITION103\"", "id=\"CONDITION10BLANK\"");
        createRadio($objForm, $arg, "CONDITION10", $value, $extra, $opt_condition10, get_count($opt_condition10));

        //気になる～があるか？ラジオボタン 1:ある 2:ない 3.未記入(BLANK)
        $opt_condition11 = array(1 => 1, 2 => 2, 3 => "BLANK");
        $value = ($row["CONDITION11"] == "" || $row["CONDITION11"] == "BLANK") ? "BLANK" : $row["CONDITION11"];
        $disable = " onclick=\"OptionUse2(this, 'SEQ14_REMARK1', '10');\"";
        $extra = array("id=\"CONDITION111\"".$disable, "id=\"CONDITION112\"".$disable, "id=\"CONDITION11BLANK\"".$disable);
        createRadio($objForm, $arg, "CONDITION11", $value, $extra, $opt_condition11, get_count($opt_condition11));

        //「ある」と答えた人チェックボックス 1～10, 11.未記入(BLANK)
        $label_array = array(
            "01"    => "体や病気",
            "02"    => "勉強",
            "03"    => "受験や進路",
            "04"    => "友達やクラス",
            "05"    => "学校や先生のこと",
            "06"    => "部活動",
            "07"    => "男女交際",
            "08"    => "家庭のこと",
            "09"    => "自分のこと",
            "10"    => "その他",
            "BLANK" => "未記入",
        );

        $opt_condition12 = array();
        for ($idx = 1; $idx <= $model->selectDetailSeq["14"]["count"]; $idx++) {
            $opt_condition12[$idx] = sprintf("%02d", $idx);
        }
        knjCreateHidden($objForm, "SEQ14_REMARK1_OPT", implode(",", $opt_condition12));
        knjCreateHidden($objForm, "SEQ14_REMARK1_USE_TEXT", "10");

        $opt_condition12[] = "BLANK";

        $extra  = " onclick=\"OptionUse3(this, 'SEQ14_REMARK1', 'CONDITION12_TEXT');\"";
        $extra .= ($row["CONDITION11"] == "1") ? "" : " disabled";
        $aryValue = array();
        makeChceckbox($objForm, $arg, "SEQ14_REMARK1", $row["SEQ14_REMARK1"], $extra, $opt_condition12, $label_array, $aryValue);

        //テキスト
        $extra = (in_array("10", $aryValue)) ? "" : "disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["CONDITION12_TEXT"] = knjCreateTextBox($objForm, $row["CONDITION12_TEXT"], "CONDITION12_TEXT", 40, 60, $extra);

        /**************************/
        /*    体温・脈拍・血圧    */
        /**************************/

        //体温テキストボックス
        $arg["data"]["TEMPERATURE1"] = knjCreateTextBox($objForm, $row["TEMPERATURE1"], "TEMPERATURE1", 4, 4, $extra_Decimal);
        //測定時間（時）
        $arg["data"]["MEASURE_HOUR1"] = knjCreateTextBox($objForm, $row["MEASURE_HOUR1"], "MEASURE_HOUR1", 2, 2, $extra_int);
        //測定時間（分）
        $arg["data"]["MEASURE_MINUTE1"] = knjCreateTextBox($objForm, $row["MEASURE_MINUTE1"], "MEASURE_MINUTE1", 2, 2, $extra_int);

        //脈拍テキストボックス
        $arg["data"]["PULSE"] = knjCreateTextBox($objForm, $row["PULSE"], "PULSE", 3, 3, $extra_int);

        //血圧（下）テキストボックス
        $arg["data"]["BLOOD_PRESSURE_L"] = knjCreateTextBox($objForm, $row["BLOOD_PRESSURE_L"], "BLOOD_PRESSURE_L", 3, 3, $extra_int);
        //血圧（上）テキストボックス
        $arg["data"]["BLOOD_PRESSURE_H"] = knjCreateTextBox($objForm, $row["BLOOD_PRESSURE_H"], "BLOOD_PRESSURE_H", 3, 3, $extra_int);

        /**************/
        /*    処置    */
        /**************/

        //処置ラベル取得
        $query = knjf150aQuery::getNameMst('F208');
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
        $gyou = 4;
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
        View::toHTML($model, "knjf150aSubForm1.html", $arg);
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
    $update = ($model->cmd == "subform1") ? "insert" : "update";
    $extra = "onclick=\"return btn_submit('".$update."');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", $extra);
    if ($model->cmd != "subform1") {
        //取消ボタン
        $extra = "onclick=\"return btn_submit('subform1_clear');\"";
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
