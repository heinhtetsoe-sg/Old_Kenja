<?php

require_once('for_php7.php');

class knjf150bSubForm5
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform5", "POST", "knjf150bindex.php", "", "subform5");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //種別区分（健康相談活動）
        $model->type ='5';

        //警告メッセージを表示しない場合
        if(($model->cmd == "subform5A") || ($model->cmd == "subform5_clear")){
            if (isset($model->schregno) && !isset($model->warning)){
                $row = $db->getRow(knjf150bQuery::getRow($model), DB_FETCHMODE_ASSOC);
            } else {
                $row =& $model->field;
            }
        } else {
            $row =& $model->field;
        }

        //extra
        $extra_int = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $extra_flote = " onblur=\"this.value=toFlote(this.value)\"";

        //生徒情報
        $hr_name = $db->getOne(knjf150bQuery::getHrName($model));
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
        $query = knjf150bQuery::getNameMstPeriod();
        makeCmb($objForm, $arg, $db, $query, "VISIT_PERIODCD", $row["VISIT_PERIODCD"], "", 1);

        //来室理由１コンボ作成
        $query = knjf150bQuery::getNameMst('F219');
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON1", $row["VISIT_REASON1"], "", 1);
        //来室理由２コンボ作成
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON2", $row["VISIT_REASON2"], "", 1);
        //来室理由３コンボ作成
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON3", $row["VISIT_REASON3"], "", 1);

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

        //処置１コンボ作成
        $query = knjf150bQuery::getNameMst('F220');
        makeCmb($objForm, $arg, $db, $query, "TREATMENT1", $row["TREATMENT1"], "", 1);
        //処置２コンボ作成
        makeCmb($objForm, $arg, $db, $query, "TREATMENT2", $row["TREATMENT2"], "", 1);
        //処置３コンボ作成
        makeCmb($objForm, $arg, $db, $query, "TREATMENT3", $row["TREATMENT3"], "", 1);

        //休養・相談時間コンボ作成
        $query = knjf150bQuery::getNameMst('F212');
        makeCmb($objForm, $arg, $db, $query, "RESTTIME", $row["RESTTIME"], "", 1);

        //退出時間（時）
        $arg["data"]["LEAVE_HOUR"] = knjCreateTextBox($objForm, $row["LEAVE_HOUR"], "LEAVE_HOUR", 2, 2, $extra_int);

        //退出時間（分）
        $arg["data"]["LEAVE_MINUTE"] = knjCreateTextBox($objForm, $row["LEAVE_MINUTE"], "LEAVE_MINUTE", 2, 2, $extra_int);

        //退出校時
        $query = knjf150bQuery::getNameMstPeriod();
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

        //連絡コンボ作成
        $query = knjf150bQuery::getNameMst('F213');
        makeCmb($objForm, $arg, $db, $query, "CONTACT", $row["CONTACT"], "", 1);

        //病院名テキストボックス
        $arg["data"]["HOSPITAL"] = knjCreateTextBox($objForm, $row["HOSPITAL"], "HOSPITAL", 20, 20, "");

        //同伴者テキストボックス
        $arg["data"]["COMPANION"] = knjCreateTextBox($objForm, $row["COMPANION"], "COMPANION", 20, 20, "");

        //同伴者区分コンボ作成
        $query = knjf150bQuery::getNameMst('F218');
        makeCmb($objForm, $arg, $db, $query, "COMPANION_DIV", $row["COMPANION_DIV"], "", 1);

        //診断名テキストボックス
        $arg["data"]["DIAGNOSIS"] = knjCreateTextBox($objForm, $row["DIAGNOSIS"], "DIAGNOSIS", 20, 20, "");

        //特記事項テキストボックス
        $arg["data"]["SPECIAL_NOTE"] = knjCreateTextBox($objForm, $row["SPECIAL_NOTE"], "SPECIAL_NOTE", 100, 100, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf150bSubForm5.html", $arg);
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

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //登録ボタン
    $update = ($model->cmd == "subform5") ? "insert" : "update";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", "onclick=\"return btn_submit('".$update."');\"");
    if($model->cmd != "subform5"){
        //取消ボタン
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('subform5_clear');\"");
    }
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");

    //欠課仮登録ボタン
    if($model->Properties["useKnjf150_2Button"] == 1) {
        $extra = "onclick=\"loadwindow('../KNJF150B_2/knjf150b_2index.php?CALL_INFO=KNJF150B_5&SCHREGNO={$model->schregno}',0,0,800,600);return;\"";
        $arg["button"]["btn_knjf150b_2"] = KnjCreateBtn($objForm, "btn_knjf150b_2", "欠課仮登録", $extra);
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
    knjCreateHidden($objForm, "PRGID", "KNJF150B");
    knjCreateHidden($objForm, "PRINT_VISIT_DATE", $model->visit_date);
    knjCreateHidden($objForm, "PRINT_VISIT_HOUR", $model->visit_hour);
    knjCreateHidden($objForm, "PRINT_VISIT_MINUTE", $model->visit_minute);
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) $ext = $extra[$i-1];
        else $ext = $extra;

        $objForm->ae( array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));

        $arg["data"][$name.$multi[$i]]  = $objForm->ge($name, $multi[$i]);
    }
}
?>
