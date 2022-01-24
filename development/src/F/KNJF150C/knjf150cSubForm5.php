<?php

require_once('for_php7.php');

class knjf150cSubForm5
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform5", "POST", "knjf150cindex.php", "", "subform5");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //エラーチェック用初期化。各項目作成時にセット
        $model->errorCheck = array();

        //種別区分（健康相談活動）
        $model->type ='5';

        //警告メッセージを表示しない場合
        if(($model->cmd == "subform5A") || ($model->cmd == "subform5_clear")){
            if (isset($model->schregno) && !isset($model->warning)){
                $row = $db->getRow(knjf150cQuery::getRow($model), DB_FETCHMODE_ASSOC);
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
        $stdInfo = $db->getRow(knjf150cQuery::getHrName($model), DB_FETCHMODE_ASSOC);
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $stdInfo["HR_NAME"].$attendno.'　'.$name;

        //来室日付作成
        $value = ($row["VISIT_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["VISIT_DATE"]);
        $arg["data"]["SEQ01_REMARK1"] = View::popUpCalendar($objForm, "SEQ01_REMARK1", $value);

        //来室時間（時）
        $extra = "";
        $value = ($row["VISIT_HOUR"] == "") ? $model->hour : $row["VISIT_HOUR"];
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ01_REMARK2", $value, $extra, 1);

        //来室時間（分）
        $extra = "";
        $value = ($row["VISIT_MINUTE"] == "") ? $model->minute : $row["VISIT_MINUTE"];
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ01_REMARK3", $value, $extra, 1);

        //場合
        $query = knjf150cQuery::getNameMst("F224");
        makeCmb($objForm, $arg, $db, $query, "SEQ01_REMARK4", $row["SEQ01_REMARK4"], "", 1);

        //授業名
        $query = knjf150cQuery::getCreditMst($stdInfo);
        makeCmb($objForm, $arg, $db, $query, "SEQ01_REMARK5", $row["SEQ01_REMARK5"], "", 1);

        //来室理由コンボ作成
        $query = knjf150cQuery::getNameMst('F219');
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SEQ02_REMARK1", $row["SEQ02_REMARK1"], $extra, 1);

        //来室理由テキスト
        $extra = "";
        $arg["data"]["SEQ02_REMARK_L1"] = knjCreateTextArea($objForm, "SEQ02_REMARK_L1", "10", "100", "hard", $extra, $row["SEQ02_REMARK_L1"]);
        $model->errorCheck["SEQ02_REMARK_L1"] = array("LABEL" => "来室理由", "LEN" => 1500);

        //退出時
        $extra = "";
        makeCmbArray($objForm, $arg, $model->hourArray, "SEQ98_REMARK1", $row["SEQ98_REMARK1"], $extra, 1);
        //退出分
        $extra = "";
        makeCmbArray($objForm, $arg, $model->minutesArray, "SEQ98_REMARK2", $row["SEQ98_REMARK2"], $extra, 1);

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
        View::toHTML($model, "knjf150cSubForm5.html", $arg);
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
    if ($model->sendSubmit != "") {
        $link = REQUESTROOT."/F/KNJF150D/knjf150dindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
    } else {
        $extra = "onclick=\"return btn_submit('edit');\"";
    }
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

    //欠課仮登録ボタン
    if($model->Properties["useKnjf150_2Button"] == 1) {
        $extra = "onclick=\"loadwindow('../KNJF150C_2/knjf150c_2index.php?CALL_INFO=KNJF150C_5&SCHREGNO={$model->schregno}',0,0,800,600);return;\"";
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
