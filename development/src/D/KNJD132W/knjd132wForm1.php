<?php

require_once('for_php7.php');

class knjd132wForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd132windex.php", "", "edit");
        
        //DB接続
        $db = Query::dbCheckOut();
        
        $query = knjd132wQuery::getConditionDatRemark1($model);
        $ConditionDatRemark1 = $db->getOne($query);
        switch($ConditionDatRemark1){
            case '1':
                $arg['SHOW_TOTALSTUDYTIME'] = 1;
                $arg['SHOW_CLUB_AND_COMMITTEE'] = 1;
                $arg['SHOW_COMMUNICATION'] = 1;
                $arg['SHOW_ATTENDREC_REMARK'] = 1;
                $arg["data"]["PATTERN"] = 'A';
                break;
            case '2':
                $arg['SHOW_TOTALSTUDYTIME'] = 1;
                $arg['SHOW_CLUB_AND_COMMITTEE'] = 1;
                $arg['SHOW_COMMUNICATION'] = 1;
                $arg['SHOW_ATTENDREC_REMARK'] = 1;
                $arg["data"]["PATTERN"] = 'B';
                break;
            case '3':
                $arg['SHOW_REMARK1'] = 1;
                $arg['SHOW_COMMUNICATION'] = 1;
                $arg["data"]["PATTERN"] = 'C';
                break;
            case '4':
                $arg['SHOW_COMMUNICATION'] = 1;
                $arg['SHOW_ATTENDREC_REMARK'] = 1;
                $arg["data"]["PATTERN"] = 'D';
                break;
            case '5':
                $arg['SHOW_SEMESTER'] = 1;
                $arg['SHOW_ATTENDREC_REMARK'] = 1;
                $arg["data"]["PATTERN"] = 'E';
                break;
            case '6':
                $arg['SHOW_TOTALSTUDYTIME'] = 1;
                $arg['SHOW_CLUB_AND_COMMITTEE'] = 1;
                $arg['SHOW_REMARK1'] = 1;
                $arg["data"]["PATTERN"] = 'F';
                break;
            
        }

       if ($arg['SHOW_SEMESTER'] == "1") {
            //学期
            $query = knjd132wQuery::getSemester();
            $extra = "onchange=\"return btn_submit('semester');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);
        }
 
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjd132wQuery::getTrainRow($model->schregno, $model),DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //通知票所見データ
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $row["TOTALSTUDYTIME"], $model);
        $arg["data"]["CLUB"] = getTextOrArea($objForm, "CLUB", $model->getPro["CLUB"]["moji"], $model->getPro["CLUB"]["gyou"], $row["CLUB"], $model);
        $arg["data"]["COMMITTEE"] = getTextOrArea($objForm, "COMMITTEE", $model->getPro["COMMITTEE"]["moji"], $model->getPro["COMMITTEE"]["gyou"], $row["COMMITTEE"], $model);
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $row["REMARK1"], $model);
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);

        //通知票所見データコメント
        setInputChkHidden($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $arg);
        setInputChkHidden($objForm, "CLUB", $model->getPro["CLUB"]["moji"], $model->getPro["CLUB"]["gyou"], $arg);
        setInputChkHidden($objForm, "COMMITTEE", $model->getPro["COMMITTEE"]["moji"], $model->getPro["COMMITTEE"]["gyou"], $arg);
        setInputChkHidden($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $arg);
        setInputChkHidden($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $arg);
        setInputChkHidden($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $arg);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML5($model, "knjd132wForm1.html", $arg);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SCHREGNOS");
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {
    //部活動参照
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_club"] = knjCreateBtn($objForm, "btn_club", "部活動参照", $extra);
    //委員会参照
    $extra = "onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_committee"] = knjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);

    //更新
    $extra = " onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
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
        $extra = "style=\"height:".$height."px;\" id=\"".$name."\"";
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
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
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
