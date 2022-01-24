<?php

require_once('for_php7.php');

class knjd132hForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd132hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $query = knjd132hQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        $row = array();
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjd132hQuery::getHreportRemarkRow($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //テキストの名前を取得する
        $textFieldName = "";

        $arg["SCHOOLKIND_H"] = ($model->schKind == "H") ? "1" : "";

        //委員会・係・部活動の成果
        $arg["data"]["REMARK1_01_01"] = getTextOrArea($objForm, "REMARK1_01_01", $model->getPro["REMARK1_01_01"]["moji"], $model->getPro["REMARK1_01_01"]["gyou"], $row["REMARK1_01_01"], $model);
        setInputChkHidden($objForm, "REMARK1_01_01", $model->getPro["REMARK1_01_01"]["moji"], $model->getPro["REMARK1_01_01"]["gyou"], $arg);

        //資格取得・検定・表彰など
        $arg["data"]["REMARK1_01_02"] = getTextOrArea($objForm, "REMARK1_01_02", $model->getPro["REMARK1_01_02"]["moji"], $model->getPro["REMARK1_01_02"]["gyou"], $row["REMARK1_01_02"], $model);
        setInputChkHidden($objForm, "REMARK1_01_02", $model->getPro["REMARK1_01_02"]["moji"], $model->getPro["REMARK1_01_02"]["gyou"], $arg);

        //研究活動テーマ
        $arg["data"]["REMARK1_02_01"] = getTextOrArea($objForm, "REMARK1_02_01", $model->getPro["REMARK1_02_01"]["moji"], $model->getPro["REMARK1_02_01"]["gyou"], $row["REMARK1_02_01"], $model);
        setInputChkHidden($objForm, "REMARK1_02_01", $model->getPro["REMARK1_02_01"]["moji"], $model->getPro["REMARK1_02_01"]["gyou"], $arg);

        //通信欄
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        setInputChkHidden($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $arg);

        /**********/
        /* ボタン */
        /**********/
        //部活動選択ボタン
        $extra = $disabled ." onclick=\" return btn_submit('club');\"";
        $arg["button"]["btn_club"] = KnjCreateBtn($objForm, "btn_club", "部活動選択", $extra);
        //委員会選択ボタン
        $extra = $disabled ." onclick=\" return btn_submit('committee');\"";
        $arg["button"]["btn_committee"] = KnjCreateBtn($objForm, "btn_committee", "委員会選択", $extra);
        //資格参照
        $extra = $disabled ." onclick=\"return btn_submit('qualified');\"";
        $arg["button"]["SIKAKU_SANSYO"] = knjCreateBtn($objForm, "SIKAKU_SANSYO", "検定選択", $extra);
        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $extra = "onclick=\"return btn_submit('club_record');\"";
            $arg["button"]["btn_club_kirokubikou"] = knjCreateBtn($objForm, "btn_club_kirokubikou", "記録備考選択", $extra);
        }
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_D132H/knjx_d132hindex.php?SCHOOL_KIND={$model->schKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "CSV入出力", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
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
        View::toHTML5($model, "knjd132hForm1.html", $arg);
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
        $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\" id=\"".$name."\"";
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
