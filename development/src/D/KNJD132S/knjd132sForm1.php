<?php

require_once('for_php7.php');

class knjd132sForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd132sindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学期コンボ
        $query = knjd132sQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if (((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) && $model->cmd != 'attend') {
            $setwk = $db->getRow(knjd132sQuery::getHreportremarkDat($model, $model->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
            $model->field["ATTENDREC_REMARK"] = $setwk["ATTENDREC_REMARK"];
            $setwk = $db->getRow(knjd132sQuery::getHreportremarkDetailDat($model, $model->field["SEMESTER"], "01"), DB_FETCHMODE_ASSOC);
            $model->field["REMARK1"] = $setwk["REMARK1"];
            $setwk = $db->getRow(knjd132sQuery::getHreportremarkDetailDat($model, $model->field["SEMESTER"], "02"), DB_FETCHMODE_ASSOC);
            $model->field["REMARK2"] = $setwk["REMARK1"];
            $setwk = $db->getRow(knjd132sQuery::getHreportremarkDetailDat($model, $model->field["SEMESTER"], "03"), DB_FETCHMODE_ASSOC);
            $model->field["REMARK3"] = $setwk["REMARK1"];
            $setwk = $db->getRow(knjd132sQuery::getHreportremarkDetailDat($model, $model->field["SEMESTER"], "04"), DB_FETCHMODE_ASSOC);
            $model->field["REMARK4"] = $setwk["REMARK1"];

            $arg["NOT_WARNING"] = 1;
        }
        $row =& $model->field;


        //通知票所見データ
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $row["REMARK1"], $model);
        setInputChkHidden($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $arg);

        $arg["data"]["REMARK2"] = getTextOrArea($objForm, "REMARK2", $model->getPro["REMARK2"]["moji"], $model->getPro["REMARK2"]["gyou"], $row["REMARK2"], $model);
        setInputChkHidden($objForm, "REMARK2", $model->getPro["REMARK2"]["moji"], $model->getPro["REMARK2"]["gyou"], $arg);

        $arg["data"]["REMARK3"] = getTextOrArea($objForm, "REMARK3", $model->getPro["REMARK3"]["moji"], $model->getPro["REMARK3"]["gyou"], $row["REMARK3"], $model);
        setInputChkHidden($objForm, "REMARK3", $model->getPro["REMARK3"]["moji"], $model->getPro["REMARK3"]["gyou"], $arg);

        $arg["data"]["REMARK4"] = getTextOrArea($objForm, "REMARK4", $model->getPro["REMARK4"]["moji"], $model->getPro["REMARK4"]["gyou"], $row["REMARK4"], $model);
        setInputChkHidden($objForm, "REMARK4", $model->getPro["REMARK4"]["moji"], $model->getPro["REMARK4"]["gyou"], $arg);

        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);
        setInputChkHidden($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $arg);

        /**********/
        /* ボタン */
        /**********/

        $setdis = "";
        if ($model->exp_year == "") {
            $setdis = " disabled ";
        }
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"".$setdis;
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前後の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        //取消ボタン
        $extra = " onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //別画面表示用ボタン
        //学級活動の委員会選択
        $target = "REMARK1";
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
        $arg["button"]["btn_committee1"] = knjCreateBtn($objForm, "btn_committee", "委員会選択", $extra);
        //生徒会活動の委員会選択
        $target = "REMARK2";
        $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"".$setdis;
        $arg["button"]["btn_committee2"] = knjCreateBtn($objForm, "btn_sikaku_ansyo", "委員会選択", $extra);
        //クラブ活動
        $target = "REMARK3";
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"".$setdis;
        $arg["button"]["btn_club"] = knjCreateBtn($objForm, "btn_club", "部活動選択", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useQualifiedMst", $model->Properties["useQualifiedMst"]);

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
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd132sForm1.html", $arg);
    }
}

//コンボボックス作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
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

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg) {
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea".$setHiddenStr);
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

//テキストボックスorテキストエリア作成
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
        $extra = "id=\"".$name."\" style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\" onPaste=\"return showPaste(this);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "id=\"".$name."\" onPaste=\"return showPaste(this);\" onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
