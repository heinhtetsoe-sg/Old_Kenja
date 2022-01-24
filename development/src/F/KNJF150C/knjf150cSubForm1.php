<?php

require_once('for_php7.php');

class knjf150cSubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjf150cindex.php", "", "subform1");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //エラーチェック用初期化。各項目作成時にセット
        $model->errorCheck = array();

        //DB接続
        $db = Query::dbCheckOut();

        //種別区分（内科）
        $model->type ='1';

        //警告メッセージを表示しない場合
        if(($model->cmd == "subform1A") || ($model->cmd == "subform1_clear")){
            if (isset($model->schregno) && !isset($model->warning)){
                $query = knjf150cQuery::getRow($model);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } else {
                $row =& $model->field;
            }
        } else {
            $row =& $model->field;
        }

        //extra
        $extra_int = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $extra_Decimal = "onblur=\"checkDecimal(this)\"";

        //生徒情報
        $stdInfo = $db->getRow(knjf150cQuery::getHrName($model), DB_FETCHMODE_ASSOC);
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $stdInfo["HR_NAME"].$attendno.'　'.$name;

        //来室日付作成
        $value = ($row["VISIT_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["VISIT_DATE"]);
        $arg["data"]["SEQ01_REMARK1"] = View::popUpCalendar($objForm, "SEQ01_REMARK1", $value);

        //来室時間（時）
        $value = ($row["VISIT_HOUR"] == "") ? $model->hour : $row["VISIT_HOUR"];
        $extra = "onChange=\"setTime(this, 'SEQ06_REMARK1');\"";
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ01_REMARK2", $value , $extra, 1);

        //来室時間（分）
        $value = ($row["VISIT_MINUTE"] == "") ? $model->minute : $row["VISIT_MINUTE"];
        $extra = "onChange=\"setTime(this, 'SEQ06_REMARK2');\"";
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ01_REMARK3", $value, $extra, 1);

        //場合
        $query = knjf150cQuery::getNameMst("F224");
        makeCmb($objForm, $arg, $db, $query, "SEQ01_REMARK4", $row["SEQ01_REMARK4"], "", 1);

        //授業名
        $query = knjf150cQuery::getCreditMst($stdInfo);
        makeCmb($objForm, $arg, $db, $query, "SEQ01_REMARK5", $row["SEQ01_REMARK5"], "", 1);

        //来室理由コンボ作成
        $query = knjf150cQuery::getNameMst('F200');
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SEQ02_REMARK1", $row["SEQ02_REMARK1"], $extra, 1);

        //来室理由テキスト
        $extra = "";
        $arg["data"]["SEQ02_REMARK_L1"] = knjCreateTextBox($objForm, $row["SEQ02_REMARK_L1"], "SEQ02_REMARK_L1", 100, 100, $extra);
        $model->errorCheck["SEQ02_REMARK_L1"] = array("LABEL" => "来室理由", "LEN" => 150);

        //就寝時
        $extra = "onfocus=\"setValue(this);\"";
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ03_REMARK1", $row["SEQ03_REMARK1"], $extra, 1);
        knjCreateHidden($objForm, "HID_SEQ03_REMARK1", $row["SEQ03_REMARK1"]);
        //就寝分
        $extra = "";
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ03_REMARK2", $row["SEQ03_REMARK2"], $extra, 1);

        //起床時
        $extra = "onfocus=\"setValue(this);\"";
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ03_REMARK3", $row["SEQ03_REMARK3"], $extra, 1);
        knjCreateHidden($objForm, "HID_SEQ03_REMARK3", $row["SEQ03_REMARK3"]);
        //起床分
        $extra = "";
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ03_REMARK4", $row["SEQ03_REMARK4"], $extra, 1);

        //食事チェックボックス 1:食べた 2:食べていない
        $extra = ($row["SEQ04_REMARK1"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ04_REMARK11\" onclick=\"setChkBox(this, 'SEQ04')\"";
        $arg["data"]["SEQ04_REMARK11"] = knjCreateCheckBox($objForm, "SEQ04_REMARK11", "1", $extra, "");
        $extra = ($row["SEQ04_REMARK1"] == "2") ? "checked" : "";
        $extra .= " id=\"SEQ04_REMARK12\" onclick=\"setChkBox(this, 'SEQ04')\"";
        $arg["data"]["SEQ04_REMARK12"] = knjCreateCheckBox($objForm, "SEQ04_REMARK12", "2", $extra, "");

        //食事の内容
        $extra = "";
        $arg["data"]["SEQ04_REMARK2"] = knjCreateTextBox($objForm, $row["SEQ04_REMARK2"], "SEQ04_REMARK2", 40, 40, $extra);
        $model->errorCheck["SEQ04_REMARK2"] = array("LABEL" => "食事内容", "LEN" => 60);

        //排便チェックボタン 1:普通 2:下痢 3:便秘 4:出ていない
        $extra = ($row["SEQ05_REMARK1"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ05_REMARK11\" onclick=\"setChkBox(this, 'SEQ05')\"";
        $arg["data"]["SEQ05_REMARK11"] = knjCreateCheckBox($objForm, "SEQ05_REMARK11", "1", $extra, "");
        $extra = ($row["SEQ05_REMARK1"] == "2") ? "checked" : "";
        $extra .= " id=\"SEQ05_REMARK12\" onclick=\"setChkBox(this, 'SEQ05')\"";
        $arg["data"]["SEQ05_REMARK12"] = knjCreateCheckBox($objForm, "SEQ05_REMARK12", "2", $extra, "");
        $extra = ($row["SEQ05_REMARK1"] == "3") ? "checked" : "";
        $extra .= " id=\"SEQ05_REMARK13\" onclick=\"setChkBox(this, 'SEQ05')\"";
        $arg["data"]["SEQ05_REMARK13"] = knjCreateCheckBox($objForm, "SEQ05_REMARK13", "3", $extra, "");
        $extra = ($row["SEQ05_REMARK1"] == "4") ? "checked" : "";
        $extra .= " id=\"SEQ05_REMARK14\" onclick=\"setChkBox(this, 'SEQ05')\"";
        $arg["data"]["SEQ05_REMARK14"] = knjCreateCheckBox($objForm, "SEQ05_REMARK14", "4", $extra, "");

        //バイタル１
        //時
        $extra = "";
        $value = ($row["SEQ06_REMARK1"] == "") ? $model->hour : $row["SEQ06_REMARK1"];
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ06_REMARK1", $value, $extra, 1);
        //分
        $extra = "";
        $value = ($row["SEQ06_REMARK2"] == "") ? $model->minute : $row["SEQ06_REMARK2"];
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ06_REMARK2", $value, $extra, 1);
        //体温1
        $extra = "";
        makeCmbArray($objForm, $arg, $model->BodyHeat1Array, "SEQ06_REMARK3", $row["SEQ06_REMARK3"], $extra, 1);
        //体温2
        $extra = "";
        makeCmbArray($objForm, $arg, $model->BodyHeat2Array, "SEQ06_REMARK4", $row["SEQ06_REMARK4"], $extra, 1);
        //脈拍
        $arg["data"]["SEQ06_REMARK5"] = knjCreateTextBox($objForm, $row["SEQ06_REMARK5"], "SEQ06_REMARK5", 3, 3, $extra_int);
        //血圧1
        $arg["data"]["SEQ06_REMARK6"] = knjCreateTextBox($objForm, $row["SEQ06_REMARK6"], "SEQ06_REMARK6", 3, 3, $extra_int);
        //血圧2
        $arg["data"]["SEQ06_REMARK7"] = knjCreateTextBox($objForm, $row["SEQ06_REMARK7"], "SEQ06_REMARK7", 3, 3, $extra_int);
        //SpO2
        $arg["data"]["SEQ06_REMARK8"] = knjCreateTextBox($objForm, $row["SEQ06_REMARK8"], "SEQ06_REMARK8", 2, 2, $extra_int);

        //バイタル２
        //時
        $extra = "";
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ07_REMARK1", $row["SEQ07_REMARK1"], $extra, 1);
        //分
        $extra = "";
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ07_REMARK2", $row["SEQ07_REMARK2"], $extra, 1);
        //体温1
        $extra = "";
        makeCmbArray($objForm, $arg, $model->BodyHeat1Array, "SEQ07_REMARK3", $row["SEQ07_REMARK3"], $extra, 1);
        //体温2
        $extra = "";
        makeCmbArray($objForm, $arg, $model->BodyHeat2Array, "SEQ07_REMARK4", $row["SEQ07_REMARK4"], $extra, 1);
        //脈拍
        $arg["data"]["SEQ07_REMARK5"] = knjCreateTextBox($objForm, $row["SEQ07_REMARK5"], "SEQ07_REMARK5", 3, 3, $extra_int);
        //血圧1
        $arg["data"]["SEQ07_REMARK6"] = knjCreateTextBox($objForm, $row["SEQ07_REMARK6"], "SEQ07_REMARK6", 3, 3, $extra_int);
        //血圧2
        $arg["data"]["SEQ07_REMARK7"] = knjCreateTextBox($objForm, $row["SEQ07_REMARK7"], "SEQ07_REMARK7", 3, 3, $extra_int);
        //SpO2
        $arg["data"]["SEQ07_REMARK8"] = knjCreateTextBox($objForm, $row["SEQ07_REMARK8"], "SEQ07_REMARK8", 2, 2, $extra_int);

        //処置
        //授業
        $extra = ($row["SEQ08_REMARK1"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK1\"";
        $arg["data"]["SEQ08_REMARK1"] = knjCreateCheckBox($objForm, "SEQ08_REMARK1", "1", $extra, "");

        //ホットパック
        $extra = ($row["SEQ08_REMARK2"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK2\"";
        $arg["data"]["SEQ08_REMARK2"] = knjCreateCheckBox($objForm, "SEQ08_REMARK2", "1", $extra, "");

        //アイシング
        $extra = ($row["SEQ08_REMARK3"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK3\"";
        $arg["data"]["SEQ08_REMARK3"] = knjCreateCheckBox($objForm, "SEQ08_REMARK3", "1", $extra, "");

        //水分補給
        $extra = ($row["SEQ08_REMARK4"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ08_REMARK4\"";
        $arg["data"]["SEQ08_REMARK4"] = knjCreateCheckBox($objForm, "SEQ08_REMARK4", "1", $extra, "");

        //休養
        $extra = ($row["SEQ09_REMARK1"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ09_REMARK1\"";
        $arg["data"]["SEQ09_REMARK1"] = knjCreateCheckBox($objForm, "SEQ09_REMARK1", "1", $extra, "");
        //来室校時
        $query = knjf150cQuery::getNameMstPeriod();
        makeCmb($objForm, $arg, $db, $query, "SEQ09_REMARK2", $row["SEQ09_REMARK2"], "", 1);

        //早退
        $extra = ($row["SEQ09_REMARK3"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ09_REMARK3\"";
        $arg["data"]["SEQ09_REMARK3"] = knjCreateCheckBox($objForm, "SEQ09_REMARK3", "1", $extra, "");
        //時
        $extra = "onChange=\"setTime(this, 'SEQ98_REMARK1');\"";
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ09_REMARK4", $row["SEQ09_REMARK4"], $extra, 1);
        //分
        $extra = "onChange=\"setTime(this, 'SEQ98_REMARK2');\"";
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ09_REMARK5", $row["SEQ09_REMARK5"], $extra, 1);

        //医療機関
        $extra = ($row["SEQ09_REMARK6"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ09_REMARK6\"";
        $arg["data"]["SEQ09_REMARK6"] = knjCreateCheckBox($objForm, "SEQ09_REMARK6", "1", $extra, "");
        //病院名テキストボックス
        $arg["data"]["SEQ09_REMARK7"] = knjCreateTextBox($objForm, $row["SEQ09_REMARK7"], "SEQ09_REMARK7", 20, 20, "");
        $model->errorCheck["SEQ09_REMARK7"] = array("LABEL" => "病院名", "LEN" => 30);

        //その他
        $extra = ($row["SEQ09_REMARK8"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ09_REMARK8\"";
        $arg["data"]["SEQ09_REMARK8"] = knjCreateCheckBox($objForm, "SEQ09_REMARK8", "1", $extra, "");
        //その他テキストボックス
        $arg["data"]["SEQ09_REMARK9"] = knjCreateTextBox($objForm, $row["SEQ09_REMARK9"], "SEQ09_REMARK9", 20, 20, "");
        $model->errorCheck["SEQ09_REMARK9"] = array("LABEL" => "その他", "LEN" => 20);

        //退出時
        $extra = "";
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ98_REMARK1", $row["SEQ98_REMARK1"], $extra, 1);
        //退出分
        $extra = "";
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ98_REMARK2", $row["SEQ98_REMARK2"], $extra, 1);

        //学校管理下災害
        $extra = ($row["SEQ97_REMARK1"] == "1") ? "checked" : "";
        $extra .= " id=\"SEQ97_REMARK1\"";
        if ($row["SEQ97_REMARK3"] || $row["SEQ97_REMARK4"]) {
            $extra .= " disabled";
            knjCreateHidden($objForm, "SEQ97_REMARK1", $row["SEQ97_REMARK1"]);
            $arg["data"]["SEQ97_REMARK1"] = knjCreateCheckBox($objForm, "", "1", $extra, "");
            knjCreateHidden($objForm, "SEQ97_REMARK3", $row["SEQ97_REMARK3"]);
            knjCreateHidden($objForm, "SEQ97_REMARK4", $row["SEQ97_REMARK4"]);
        } else {
            $arg["data"]["SEQ97_REMARK1"] = knjCreateCheckBox($objForm, "SEQ97_REMARK1", "1", $extra, "");
        }

        //特記事項テキストボックス
        $arg["data"]["SEQ99_REMARK_L1"] = knjCreateTextBox($objForm, $row["SEQ99_REMARK_L1"], "SEQ99_REMARK_L1", 100, 100, "");
        $model->errorCheck["SEQ99_REMARK_L1"] = array("LABEL" => "特記事項", "LEN" => 150);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf150cSubForm1.html", $arg);
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

//コンボ作成配列私
function makeCmbArray(&$objForm, &$arg, $dataArray, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    foreach ($dataArray as $key => $val) {

        $opt[] = array('label' => $val["label"],
                       'value' => $val["value"]);

        if ($value === $val["value"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //登録ボタン
    $update = ($model->cmd == "subform1") ? "insert" : "update";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", "onclick=\"return btn_submit('".$update."');\"");
    if($model->cmd != "subform1"){
        //取消ボタン
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('subform1_clear');\"");
    }
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
    //終了ボタン
    if ($model->sendSubmit != "") {
        $link = REQUESTROOT."/F/KNJF150D/knjf150dindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
    } else {
        $extra = "onclick=\"return btn_submit('edit');\"";
    }
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

    //欠課仮登録ボタン
    if($model->Properties["useKnjf150_2Button"] == 1) {
        $extra = "onclick=\"loadwindow('../KNJF150C_2/knjf150c_2index.php?CALL_INFO=KNJF150C_1&SCHREGNO={$model->schregno}',0,0,800,600);return;\"";
        $arg["button"]["btn_knjf150c_2"] = KnjCreateBtn($objForm, "btn_knjf150c_2", "欠課仮登録", $extra);
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
    knjCreateHidden($objForm, "PRGID", "KNJF150C");
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
