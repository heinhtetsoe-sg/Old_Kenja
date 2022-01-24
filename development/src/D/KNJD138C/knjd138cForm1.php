<?php

require_once('for_php7.php');

class knjd138cForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd138cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //最大学期
        $query = knjd138cQuery::getMaxSemester();
        $model->maxSeme = $db->getOne($query);

        //学期コンボ
        $query = knjd138cQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if (((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) && $model->cmd != 'attend') {
            $setwk = $db->getRow(knjd138cQuery::getHreportremarkDat($model, $model->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
            $model->field["ATTENDREC_REMARK"] = $setwk["ATTENDREC_REMARK"];
            $setwk = $db->getRow(knjd138cQuery::getHreportremarkDetailDat($model, $model->field["SEMESTER"], "01", "01"), DB_FETCHMODE_ASSOC);
            $model->field["REMARK_01_01"] = $setwk["REMARK1"];
            $setwk = $db->getRow(knjd138cQuery::getHreportremarkDetailDat($model, $model->field["SEMESTER"], "01", "02"), DB_FETCHMODE_ASSOC);
            $model->field["REMARK_01_02"] = $setwk["REMARK1"];
            $setwk = $db->getRow(knjd138cQuery::getHreportremarkDetailDat($model, $model->field["SEMESTER"], "01", "03"), DB_FETCHMODE_ASSOC);
            $model->field["REMARK_01_03"] = $setwk["REMARK1"];

            $arg["NOT_WARNING"] = 1;
        }
        $row =& $model->field;


        //通知票所見データ
        $maxSemeDisabled = $model->maxSeme == $model->field["SEMESTER"] ? "" : " disabled ";

        //学級活動
        $extra = " id=\"REMARK_01_01\" ";
        $checked = $model->field["REMARK_01_01"] == '1' ? " checked " : "";
        $arg["data"]["REMARK_01_01"] = knjCreateCheckBox($objForm, "REMARK_01_01", "1", $checked.$extra.$maxSemeDisabled);

        //生徒会活動
        $extra = " id=\"REMARK_01_01\" ";
        $checked = $model->field["REMARK_01_02"] == '1' ? " checked " : "";
        $arg["data"]["REMARK_01_02"] = knjCreateCheckBox($objForm, "REMARK_01_02", "1", $checked.$extra.$maxSemeDisabled);

        //学校行事
        $extra = " id=\"REMARK_01_03\" ";
        $checked = $model->field["REMARK_01_03"] == '1' ? " checked " : "";
        $arg["data"]["REMARK_01_03"] = knjCreateCheckBox($objForm, "REMARK_01_03", "1", $checked.$extra.$maxSemeDisabled);

        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model, "");
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
        View::toHTML5($model, "knjd138cForm1.html", $arg);
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
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $maxSemeDisabled) {
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
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra.$maxSemeDisabled, $val);
    } else {
        //textbox
        $extra = "id=\"".$name."\" onPaste=\"return showPaste(this);\" onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra.$maxSemeDisabled);
    }
    return $retArg;
}
?>
