<?php

require_once('for_php7.php');

class knjz069aForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz069aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $query = knjz069aQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //学年コンボ
        $query = knjz069aQuery::getGrade($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "");

        //Unit区分コンボ
        $query = knjz069aQuery::getUnit($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["UNIT_CD"], "UNIT_CD", $extra, 1, "");
        
        //警告メッセージを表示しない場合
        $row = array();
        if (!isset($model->warning)) {
            $row = $db->getRow(knjz069aQuery::getPypUnitRow($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //テキストの名前を取得する
        $textFieldName = "";
        
        //UNIT_THEME
        $arg["data"]["UNIT_THEME"] = getTextOrArea($objForm, "UNIT_THEME", $model->getPro["UNIT_THEME"]["moji"], $model->getPro["UNIT_THEME"]["gyou"], $row["UNIT_THEME"], $model);
        setInputChkHidden($objForm, "UNIT_THEME", $model->getPro["UNIT_THEME"]["moji"], $model->getPro["UNIT_THEME"]["gyou"], $arg);

        //UNIT_IDEA
        $arg["data"]["UNIT_IDEA"] = getTextOrArea($objForm, "UNIT_IDEA", $model->getPro["UNIT_IDEA"]["moji"], $model->getPro["UNIT_IDEA"]["gyou"], $row["UNIT_IDEA"], $model);
        setInputChkHidden($objForm, "UNIT_IDEA", $model->getPro["UNIT_IDEA"]["moji"], $model->getPro["UNIT_IDEA"]["gyou"], $arg);

        //出欠の備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);
        setInputChkHidden($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $arg);

        //特別活動・係
        $arg["data"]["SPECIALACTREMARK"] = getTextOrArea($objForm, "SPECIALACTREMARK", $model->getPro["SPECIALACTREMARK"]["moji"], $model->getPro["SPECIALACTREMARK"]["gyou"], $row["SPECIALACTREMARK"], $model);
        setInputChkHidden($objForm, "SPECIALACTREMARK", $model->getPro["SPECIALACTREMARK"]["moji"], $model->getPro["SPECIALACTREMARK"]["gyou"], $arg);

        //フィールドワーク等
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $row["REMARK1"], $model);
        setInputChkHidden($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $arg);

        /**********/
        /* ボタン */
        /**********/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_Z069A/knjx_z069aindex.php?SCHOOL_KIND={$model->schKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "CSV入出力", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML5($model, "knjz069aForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg) {
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
}

function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}

?>
