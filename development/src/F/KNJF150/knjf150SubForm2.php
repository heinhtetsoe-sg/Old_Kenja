<?php

require_once('for_php7.php');

class knjf150SubForm2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjf150index.php", "", "subform2");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //種別区分（外科）
        $model->type ='2';

        //警告メッセージを表示しない場合
        if (($model->cmd == "subform2A") || ($model->cmd == "subform2_clear")) {
            if (isset($model->schregno) && !isset($model->warning)) {
                $row = $db->getRow(knjf150Query::getRow($model), DB_FETCHMODE_ASSOC);
            } else {
                $row =& $model->field;
            }
        } else {
            $row =& $model->field;
        }

        // 宮城県は「処置結果」「連絡」「医療機関」「体温」
        if ($model->schoolName == "miyagiken") {
            $arg["COLSPAN"] = "colspan=\"4\"";
        } else {
            $arg["not_miyagiken"] = "1";
        }

        //extra
        $extra_int = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $extra_flote = " onblur=\"this.value=toFlote(this.value)\"";

        //生徒情報
        $hr_name = $db->getOne(knjf150Query::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //来室日付作成
        $value = ($row["VISIT_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["VISIT_DATE"]);
        $arg["data"]["VISIT_DATE"] = View::popUpCalendar($objForm, "VISIT_DATE", $value);

        //来室時間（時）
        $arg["data"]["VISIT_HOUR"] = knjCreateTextBox($objForm, $row["VISIT_HOUR"], "VISIT_HOUR", 2, 2, $extra_int);

        //来室時間（分）
        $arg["data"]["VISIT_MINUTE"] = knjCreateTextBox($objForm, $row["VISIT_MINUTE"], "VISIT_MINUTE", 2, 2, $extra_int);

        //来室校時
        $query = knjf150Query::getNameMstPeriod();
        makeCmb($objForm, $arg, $db, $query, "VISIT_PERIODCD", $row["VISIT_PERIODCD"], "", 1);

        //けがの場所テキスト入力可コード格納
        $f207Text = array();
        $query = knjf150Query::getNameMst('F207');
        $result = $db->query($query);
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row1["NAMESPARE2"] == "1") {
                $f207Text[] = $row1["VALUE"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "f207Text", implode(',', $f207Text));

        //けがの場所１コンボ作成
        $query = knjf150Query::getNameMst('F207');
        $extra = "onclick=\"OptionUse2(this, 'f207Text');\"";
        makeCmb($objForm, $arg, $db, $query, "INJURY_PART1", $row["INJURY_PART1"], $extra, 1);
        //けがの場所１テキスト
        $extra = (in_array($row["INJURY_PART1"], $f207Text)) ? "" : "disabled";
        $arg["data"]["INJURY_PART1_TEXT"] = knjCreateTextBox($objForm, $row["INJURY_PART1_TEXT"], "INJURY_PART1_TEXT", 60, 90, $extra);

        //けがの場所２コンボ作成
        $extra = "onclick=\"OptionUse2(this, 'f207Text');\"";
        makeCmb($objForm, $arg, $db, $query, "INJURY_PART2", $row["INJURY_PART2"], $extra, 1);
        //けがの場所２テキスト
        $extra = (in_array($row["INJURY_PART2"], $f207Text)) ? "" : "disabled";
        $arg["data"]["INJURY_PART2_TEXT"] = knjCreateTextBox($objForm, $row["INJURY_PART2_TEXT"], "INJURY_PART2_TEXT", 60, 90, $extra);

        //けがの場所３コンボ作成
        $extra = "onclick=\"OptionUse2(this, 'f207Text');\"";
        makeCmb($objForm, $arg, $db, $query, "INJURY_PART3", $row["INJURY_PART3"], $extra, 1);
        //けがの場所３テキスト
        $extra = (in_array($row["INJURY_PART3"], $f207Text)) ? "" : "disabled";
        $arg["data"]["INJURY_PART3_TEXT"] = knjCreateTextBox($objForm, $row["INJURY_PART3_TEXT"], "INJURY_PART3_TEXT", 60, 90, $extra);

        //来室理由テキスト入力可コード格納
        $f201Text = array();
        $query = knjf150Query::getNameMst('F201');
        $result = $db->query($query);
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row1["NAMESPARE2"] == "1") {
                $f201Text[] = $row1["VALUE"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "f201Text", implode(',', $f201Text));

        //来室理由１コンボ作成
        $query = knjf150Query::getNameMst('F201');
        $extra = "onclick=\"OptionUse2(this, 'f201Text');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON1", $row["VISIT_REASON1"], $extra, 1);
        //来室理由１テキスト
        $extra = (in_array($row["VISIT_REASON1"], $f201Text)) ? "" : "disabled";
        $arg["data"]["VISIT_REASON1_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON1_TEXT"], "VISIT_REASON1_TEXT", 60, 90, $extra);

        //来室理由２コンボ作成
        $extra = "onclick=\"OptionUse2(this, 'f201Text');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON2", $row["VISIT_REASON2"], $extra, 1);
        //来室理由２テキスト
        $extra = (in_array($row["VISIT_REASON2"], $f201Text)) ? "" : "disabled";
        $arg["data"]["VISIT_REASON2_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON2_TEXT"], "VISIT_REASON2_TEXT", 60, 90, $extra);

        //来室理由３コンボ作成
        $extra = "onclick=\"OptionUse2(this, 'f201Text');\"";
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON3", $row["VISIT_REASON3"], $extra, 1);
        //来室理由３テキスト
        $extra = (in_array($row["VISIT_REASON3"], $f201Text)) ? "" : "disabled";
        $arg["data"]["VISIT_REASON3_TEXT"] = knjCreateTextBox($objForm, $row["VISIT_REASON3_TEXT"], "VISIT_REASON3_TEXT", 60, 90, $extra);

        //発生日付作成
        $value = ($row["OCCUR_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["OCCUR_DATE"]);
        $arg["data"]["OCCUR_DATE"] = View::popUpCalendar($objForm, "OCCUR_DATE", $value);

        //発生時間（時）
        $arg["data"]["OCCUR_HOUR"] = knjCreateTextBox($objForm, $row["OCCUR_HOUR"], "OCCUR_HOUR", 2, 2, $extra_int);

        //発生時間（分）
        $arg["data"]["OCCUR_MINUTE"] = knjCreateTextBox($objForm, $row["OCCUR_MINUTE"], "OCCUR_MINUTE", 2, 2, $extra_int);

        //発生時の行動コンボ作成
        $cmd = ($model->cmd == "subform2A" || $model->cmd == "subform2_clear") ? "subform2B" : "subform2";
        $extra = "onchange=\"return btn_submit('".$cmd."');\"";
        $query = knjf150Query::getNameMst('F216');
        makeCmb($objForm, $arg, $db, $query, "OCCUR_ACT", $row["OCCUR_ACT"], $extra, 1);

        //行動詳細コンボ作成
        $detail = $db->getRow(knjf150Query::getNameMst('F216', $row["OCCUR_ACT"]), DB_FETCHMODE_ASSOC);
        if ($row["OCCUR_ACT"] && ($detail["NAMESPARE1"] == "1")) {
            $query = knjf150Query::getNameMst('B001');
            $extra = (get_count($db->getCol($query))) ? "" : " style=\"width:120px;\"";
            makeCmb($objForm, $arg, $db, $query, "OCCUR_ACT_DETAIL", $row["OCCUR_ACT_DETAIL"], $extra, 1);
        } elseif ($row["OCCUR_ACT"] && ($detail["NAMESPARE1"] == "2")) {
            $query = knjf150Query::getClubMst();
            $extra = (get_count($db->getCol($query))) ? "" : " style=\"width:120px;\"";
            makeCmb($objForm, $arg, $db, $query, "OCCUR_ACT_DETAIL", $row["OCCUR_ACT_DETAIL"], $extra, 1);
        } elseif ($row["OCCUR_ACT"]) {
            $query = knjf150Query::getNameMst('F217', $row["OCCUR_ACT"]);
            $extra = (get_count($db->getCol($query))) ? "" : " style=\"width:120px;\"";
            makeCmb($objForm, $arg, $db, $query, "OCCUR_ACT_DETAIL", $row["OCCUR_ACT_DETAIL"], $extra, 1);
        } else {
            $opt[] = array('label' => "", 'value' => "");
            $arg["data"]["OCCUR_ACT_DETAIL"] = knjCreateCombo($objForm, "OCCUR_ACT_DETAIL", $row["OCCUR_ACT_DETAIL"], $opt, "disabled style=\"width:120px;\"", 1);
        }

        //発生状況テキストボックス
        $arg["data"]["OCCUR_SITUATION"] = knjCreateTextBox($objForm, $row["OCCUR_SITUATION"], "OCCUR_SITUATION", 60, 60, "");

        //発生場所コンボ作成
        $query = knjf150Query::getNameMst('F206');
        if ($model->schoolName != "miyagiken") {
            $f206Text = array();
            $result = $db->query($query);
            while ($row6 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row6["NAMESPARE2"] == "1") {
                    $f206Text[] = $row6["VALUE"];
                }
            }
            $result->free();
            knjCreateHidden($objForm, "f206Text", implode(',', $f206Text));
            $extra = "onclick=\"OptionUse2(this, 'f206Text');\"";
        } else {
            $extra = "";
        }
        makeCmb($objForm, $arg, $db, $query, "OCCUR_PLACE", $row["OCCUR_PLACE"], $extra, 1);

        //宮城県以外のみテキスト表示
        if ($model->schoolName != "miyagiken") {
            //休養時間テキスト
            $extra = (in_array($row["OCCUR_PLACE"], $f206Text, true)) ? "" : "disabled";
            $arg["data"]["OCCUR_PLACE_TEXT2"] = knjCreateTextBox($objForm, $row["OCCUR_PLACE_TEXT2"], "OCCUR_PLACE_TEXT2", 20, 10, $extra);
        }

        //処置テキスト入力可コード格納
        $f209Text = array();
        $query = knjf150Query::getNameMst('F209');
        $result = $db->query($query);
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row1["NAMESPARE2"] == "1") {
                $f209Text[] = $row1["VALUE"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "f209Text", implode(',', $f209Text));

        //処置１コンボ作成
        $query = knjf150Query::getNameMst('F209');
        $extra = "onclick=\"OptionUse2(this, 'f209Text');\"";
        makeCmb($objForm, $arg, $db, $query, "TREATMENT1", $row["TREATMENT1"], $extra, 1);
        $extra = (in_array($row["TREATMENT1"], $f209Text)) ? "" : "disabled";
        $arg["data"]["TREATMENT1_TEXT"] = knjCreateTextBox($objForm, $row["TREATMENT1_TEXT"], "TREATMENT1_TEXT", 60, 90, $extra);
        //処置２コンボ作成
        $extra = "onclick=\"OptionUse2(this, 'f209Text');\"";
        makeCmb($objForm, $arg, $db, $query, "TREATMENT2", $row["TREATMENT2"], $extra, 1);
        $extra = (in_array($row["TREATMENT2"], $f209Text)) ? "" : "disabled";
        $arg["data"]["TREATMENT2_TEXT"] = knjCreateTextBox($objForm, $row["TREATMENT2_TEXT"], "TREATMENT2_TEXT", 60, 90, $extra);
        //処置３コンボ作成
        $extra = "onclick=\"OptionUse2(this, 'f209Text');\"";
        makeCmb($objForm, $arg, $db, $query, "TREATMENT3", $row["TREATMENT3"], $extra, 1);
        $extra = (in_array($row["TREATMENT3"], $f209Text)) ? "" : "disabled";
        $arg["data"]["TREATMENT3_TEXT"] = knjCreateTextBox($objForm, $row["TREATMENT3_TEXT"], "TREATMENT3_TEXT", 60, 90, $extra);

        //休養時間コンボ作成
        $query = knjf150Query::getNameMst('F212');
        if ($model->schoolName != "miyagiken") {
            $f212Text = array();
            $result = $db->query($query);
            while ($row12 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row12["NAMESPARE2"] == "1") {
                    $f212Text[] = $row12["VALUE"];
                }
            }
            $result->free();
            knjCreateHidden($objForm, "f212Text", implode(',', $f212Text));
            $extra = "onclick=\"OptionUse2(this, 'f212Text');\"";
        } else {
            $extra = "";
        }
        makeCmb($objForm, $arg, $db, $query, "RESTTIME", $row["RESTTIME"], $extra, 1);
        //宮城県以外のみ表示
        if ($model->schoolName != "miyagiken") {
            //休養時間テキスト
            $extra = (in_array($row["RESTTIME"], $f212Text, true)) ? "" : "disabled";
            $arg["data"]["RESTTIME_TEXT"] = knjCreateTextBox($objForm, $row["RESTTIME_TEXT"], "RESTTIME_TEXT", 20, 10, $extra);
        }

        //退出時間（時）
        $arg["data"]["LEAVE_HOUR"] = knjCreateTextBox($objForm, $row["LEAVE_HOUR"], "LEAVE_HOUR", 2, 2, $extra_int);

        //退出時間（分）
        $arg["data"]["LEAVE_MINUTE"] = knjCreateTextBox($objForm, $row["LEAVE_MINUTE"], "LEAVE_MINUTE", 2, 2, $extra_int);

        //退出校時
        $query = knjf150Query::getNameMstPeriod();
        makeCmb($objForm, $arg, $db, $query, "LEAVE_PERIODCD", $row["LEAVE_PERIODCD"], "", 1);

        //処理結果（休養）チェックボックス
        $extra = ($row["RESULT_REST"] == "1") ? "checked" : "";
        $extra .= " id=\"RESULT_REST\"";
        $arg["data"]["RESULT_REST"] = knjCreateCheckBox($objForm, "RESULT_REST", "1", $extra, "");

        //処理結果（早退）チェックボックス
        $extra = ($row["RESULT_EARLY"] == "1") ? "checked" : "";
        $extra .= " id=\"RESULT_EARLY\"";
        $arg["data"]["RESULT_EARLY"] = knjCreateCheckBox($objForm, "RESULT_EARLY", "1", $extra, "");

        //処理結果（医療機関）チェックボックス
        $extra = ($row["RESULT_MEDICAL"] == "1") ? "checked" : "";
        $extra .= " id=\"RESULT_MEDICAL\"";
        $arg["data"]["RESULT_MEDICAL"] = knjCreateCheckBox($objForm, "RESULT_MEDICAL", "1", $extra, "");

        //処理結果（教室へ戻る）チェックボックス(※宮城県以外のみ表示。表示制御はHTMLにて制御)
        $extra = ($row["RESULT_RETCLS"] == "1") ? "checked" : "";
        $extra .= " id=\"RESULT_RETCLS\"";
        $arg["data"]["RESULT_RETCLS"] = knjCreateCheckBox($objForm, "RESULT_RETCLS", "1", $extra, "");

        //連絡コンボ作成
        $query = knjf150Query::getNameMst('F213');
        makeCmb($objForm, $arg, $db, $query, "CONTACT", $row["CONTACT"], "", 1);

        //連絡コンボ２作成
        $query = knjf150Query::getNameMst('F213');
        makeCmb($objForm, $arg, $db, $query, "CONTACT2", $row["CONTACT2"], "", 1);

        //連絡コンボ３作成
        $query = knjf150Query::getNameMst('F213');
        makeCmb($objForm, $arg, $db, $query, "CONTACT3", $row["CONTACT3"], "", 1);

        //病院名テキストボックス
        $arg["data"]["HOSPITAL"] = knjCreateTextBox($objForm, $row["HOSPITAL"], "HOSPITAL", 20, 20, "");

        //同伴者テキストボックス
        $arg["data"]["COMPANION"] = knjCreateTextBox($objForm, $row["COMPANION"], "COMPANION", 20, 20, "");

        //同伴者区分コンボ作成
        $query = knjf150Query::getNameMst('F218');
        makeCmb($objForm, $arg, $db, $query, "COMPANION_DIV", $row["COMPANION_DIV"], "", 1);

        //診断名テキストボックス
        $arg["data"]["DIAGNOSIS"] = knjCreateTextBox($objForm, $row["DIAGNOSIS"], "DIAGNOSIS", 20, 20, "");

        //特記事項テキスト
        if ($model->schoolName == "fukuiken") {
            $arg["data"]["SPECIAL_NOTE"] = knjCreateTextArea($objForm, "SPECIAL_NOTE", "2", "100", "", $extra, $row["SPECIAL_NOTE"]);
        } else {
            $arg["data"]["SPECIAL_NOTE"] = knjCreateTextBox($objForm, $row["SPECIAL_NOTE"], "SPECIAL_NOTE", 100, 100, "");
        }

        //体調１ラジオボタン 1:はい 2:いいえ 3:余り眠れない 9.未記入
        $opt_condition1 = array(1 => 1, 2 => 2, 3 => 3, 4 => 9);
        $value = ($row["CONDITION1"] == "" || $row["CONDITION1"] == "9") ? "9" : $row["CONDITION1"];
        $extra = array("id=\"CONDITION11\"", "id=\"CONDITION12\"", "id=\"CONDITION13\"", "id=\"CONDITION19\"");
        createRadio($objForm, $arg, "CONDITION1", $value, $extra, $opt_condition1, get_count($opt_condition1));

        //睡眠時間
        $arg["data"]["SLEEPTIME"] = knjCreateTextBox($objForm, $row["SLEEPTIME"], "SLEEPTIME", 2, 2, $extra_int);

        //体調３ラジオボタン 1:出た 2:出ていない 3:便秘 9.未記入
        $opt_condition3 = array(1 => 1, 2 => 2, 3 => 3, 4 => 9);
        $value = ($row["CONDITION3"] == "" || $row["CONDITION3"] == "9") ? "9" : $row["CONDITION3"];
        $extra = array("id=\"CONDITION31\"", "id=\"CONDITION32\"", "id=\"CONDITION33\"", "id=\"CONDITION39\"");
        createRadio($objForm, $arg, "CONDITION3", $value, $extra, $opt_condition3, get_count($opt_condition3));

        //体調４ラジオボタン 1:食べた 2:食べていない 3:いつも食べない 9.未記入
        $opt_condition4 = array(1 => 1, 2 => 2, 3 => 3, 4 => 9);
        $value = ($row["CONDITION4"] == "" || $row["CONDITION4"] == "9") ? "9" : $row["CONDITION4"];
        $disabled = "onclick=\"OptionUse('this');\"";
        $extra = array("id=\"CONDITION41\"".$disabled, "id=\"CONDITION42\"".$disabled, "id=\"CONDITION43\"".$disabled, "id=\"CONDITION49\"".$disabled);
        createRadio($objForm, $arg, "CONDITION4", $value, $extra, $opt_condition4, get_count($opt_condition4));

        //食事の内容
        $extra = ($row["CONDITION4"] == "1") ? "" : " disabled STYLE=\"background-color:#D3D3D3\"";
        $arg["data"]["MEAL"] = knjCreateTextBox($objForm, $row["MEAL"], "MEAL", 20, 20, $extra);

        //体調５ラジオボタン 1:ある 2:ない 9.未記入
        $opt_condition5 = array(1 => 1, 2 => 2, 3 => 9);
        $value = ($row["CONDITION5"] == "" || $row["CONDITION5"] == "9") ? "9" : $row["CONDITION5"];
        $extra = array("id=\"CONDITION51\"", "id=\"CONDITION52\"", "id=\"CONDITION59\"");
        createRadio($objForm, $arg, "CONDITION5", $value, $extra, $opt_condition5, get_count($opt_condition5));

        //体調６テキストボックス
        $arg["data"]["CONDITION6"] = knjCreateTextBox($objForm, $row["CONDITION6"], "CONDITION6", 40, 40, "");

        //体温１テキストボックス
        $arg["data"]["TEMPERATURE1"] = knjCreateTextBox($objForm, $row["TEMPERATURE1"], "TEMPERATURE1", 4, 4, $extra_flote);

        //測定時間（時）１
        $arg["data"]["MEASURE_HOUR1"] = knjCreateTextBox($objForm, $row["MEASURE_HOUR1"], "MEASURE_HOUR1", 2, 2, $extra_int);

        //測定時間（分）１
        $arg["data"]["MEASURE_MINUTE1"] = knjCreateTextBox($objForm, $row["MEASURE_MINUTE1"], "MEASURE_MINUTE1", 2, 2, $extra_int);

        //体温２テキストボックス
        $arg["data"]["TEMPERATURE2"] = knjCreateTextBox($objForm, $row["TEMPERATURE2"], "TEMPERATURE2", 4, 4, $extra_flote);

        //測定時間（時）２
        $arg["data"]["MEASURE_HOUR2"] = knjCreateTextBox($objForm, $row["MEASURE_HOUR2"], "MEASURE_HOUR2", 2, 2, $extra_int);

        //測定時間（分）２
        $arg["data"]["MEASURE_MINUTE2"] = knjCreateTextBox($objForm, $row["MEASURE_MINUTE2"], "MEASURE_MINUTE2", 2, 2, $extra_int);

        //体温３テキストボックス
        $arg["data"]["TEMPERATURE3"] = knjCreateTextBox($objForm, $row["TEMPERATURE3"], "TEMPERATURE3", 4, 4, $extra_flote);

        //測定時間（時）３
        $arg["data"]["MEASURE_HOUR3"] = knjCreateTextBox($objForm, $row["MEASURE_HOUR3"], "MEASURE_HOUR3", 2, 2, $extra_int);

        //測定時間（分）３
        $arg["data"]["MEASURE_MINUTE3"] = knjCreateTextBox($objForm, $row["MEASURE_MINUTE3"], "MEASURE_MINUTE3", 2, 2, $extra_int);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf150SubForm2.html", $arg);
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
    if ($model->sendProgramId == 'KNJF150G') {
        //終了ボタン
        $extra = "onclick=\"location.href='" . REQUESTROOT ."/F/KNJF150G/knjf150gindex.php?cmd=edit'\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
    } else {
        //登録ボタン
        $update = ($model->cmd == "subform2") ? "insert" : "update";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", "onclick=\"return btn_submit('".$update."');\"");
        if ($model->cmd != "subform2") {
            //取消ボタン
            $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('subform2_clear');\"");
        }
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
        //終了ボタン
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");

        //欠課仮登録ボタン
        if ($model->Properties["useKnjf150_2Button"] == 1) {
            $extra = "onclick=\"loadwindow('../KNJF150_2/knjf150_2index.php?CALL_INFO=KNJF150_2&SCHREGNO={$model->schregno}',0,0,800,600);return;\"";
            $arg["button"]["btn_knjf150_2"] = KnjCreateBtn($objForm, "btn_knjf150_2", "欠課仮登録", $extra);
        }
    }
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "TYPE", $model->type);

    //印刷用
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJF150");
    knjCreateHidden($objForm, "PRINT_VISIT_DATE", $model->visit_date);
    knjCreateHidden($objForm, "PRINT_VISIT_HOUR", $model->visit_hour);
    knjCreateHidden($objForm, "PRINT_VISIT_MINUTE", $model->visit_minute);
    knjCreateHidden($objForm, "SEND_PROGRAMID", $model->sendProgramId);
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
