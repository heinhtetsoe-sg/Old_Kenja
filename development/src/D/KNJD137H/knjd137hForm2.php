<?php

require_once('for_php7.php');

class knjd137hForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd137hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $query = knjd137hQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //警告メッセージを表示しない場合
        $row = array();
        if (((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) && $model->cmd != "relode") {
            $row = $db->getRow(knjd137hQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
            $row2 = $db->getRow(knjd137hQuery::getTrainRow2($model,'01','01'), DB_FETCHMODE_ASSOC);
            if($row2['REMARK1']){
                $row['SPECIAL1'] = $row2['REMARK1'];
            }
            $row2 = $db->getRow(knjd137hQuery::getTrainRow2($model,'01','02'), DB_FETCHMODE_ASSOC);
            if($row2['REMARK1']){
                $row['SPECIAL2'] = $row2['REMARK1'];
            }
            $row2 = $db->getRow(knjd137hQuery::getTrainRow2($model,'01','03'), DB_FETCHMODE_ASSOC);
            if($row2['REMARK1']){
                $row['SPECIAL3'] = $row2['REMARK1'];
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //自立活動
        $extra=' disabled="disabled"';
        $arg["data"]["REMARK3"] = getTextOrArea($objForm, "REMARK3", $model->getPro["REMARK3"]["moji"], $model->getPro["REMARK3"]["gyou"], $row["REMARK3"], $model,$extra);
        $arg["data"]["REMARK3_COMMENT"] = "(全角".$model->getPro["REMARK3"]["moji"]."文字X".$model->getPro["REMARK3"]["gyou"]."行まで)";

        //総合的な学習の時間（学習内容）
        $arg["TOTALSTUDYTITLE"] = $model->TotalStudyStr;
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $row["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYTIME"]["moji"]."文字X".$model->getPro["TOTALSTUDYTIME"]["gyou"]."行まで)";

        //特別活動（部活動）
        $arg["data"]["SPECIAL1"] = getTextOrArea($objForm, "SPECIAL1", $model->getPro["SPECIAL1"]["moji"], $model->getPro["SPECIAL1"]["gyou"], $row["SPECIAL1"], $model);
        $arg["data"]["SPECIAL1_COMMENT"] = "(全角".$model->getPro["SPECIAL1"]["moji"]."文字X".$model->getPro["SPECIAL1"]["gyou"]."行まで)";

        //特別活動（委員会）
        $arg["data"]["SPECIAL2"] = getTextOrArea($objForm, "SPECIAL2", $model->getPro["SPECIAL2"]["moji"], $model->getPro["SPECIAL2"]["gyou"], $row["SPECIAL2"], $model);
        $arg["data"]["SPECIAL2_COMMENT"] = "(全角".$model->getPro["SPECIAL2"]["moji"]."文字X".$model->getPro["SPECIAL2"]["gyou"]."行まで)";

        //特別活動（その他）
        $arg["data"]["SPECIAL3"] = getTextOrArea($objForm, "SPECIAL3", $model->getPro["SPECIAL3"]["moji"], $model->getPro["SPECIAL3"]["gyou"], $row["SPECIAL3"], $model);
        $arg["data"]["SPECIAL3_COMMENT"] = "(全角".$model->getPro["SPECIAL3"]["moji"]."文字X".$model->getPro["SPECIAL3"]["gyou"]."行まで)";
        
        //総合所見
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        $arg["data"]["COMMUNICATION_COMMENT"] = "(全角".$model->getPro["COMMUNICATION"]["moji"]."文字X".$model->getPro["COMMUNICATION"]["gyou"]."行まで)";
        
        //出欠備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->getPro["ATTENDREC_REMARK"]["moji"]."文字X".$model->getPro["ATTENDREC_REMARK"]["gyou"]."行まで)";

        /**********/
        /* ボタン */
        /**********/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update2');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update2");

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "totalstudytime_gyou", $model->getPro["TOTALSTUDYTIME"]["gyou"]);
        knjCreateHidden($objForm, "totalstudytime_moji", $model->getPro["TOTALSTUDYTIME"]["moji"]);
        knjCreateHidden($objForm, "remark3_gyou", $model->getPro["REMARK3"]["gyou"]);
        knjCreateHidden($objForm, "remark3_moji", $model->getPro["REMARK3"]["moji"]);
        knjCreateHidden($objForm, "special1_gyou", $model->getPro["SPECIAL1"]["gyou"]);
        knjCreateHidden($objForm, "special1_moji", $model->getPro["SPECIAL1"]["moji"]);
        knjCreateHidden($objForm, "special2_gyou", $model->getPro["SPECIAL2"]["gyou"]);
        knjCreateHidden($objForm, "special2_moji", $model->getPro["SPECIAL2"]["moji"]);
        knjCreateHidden($objForm, "special3_gyou", $model->getPro["SPECIAL3"]["gyou"]);
        knjCreateHidden($objForm, "special3_moji", $model->getPro["SPECIAL3"]["moji"]);
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
        View::toHTML($model, "knjd137hForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
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

function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $inExtra='') {
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
        $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\" onPaste=\"return showPaste(this);\"".$inExtra;
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onPaste=\"return showPaste(this);\" onkeypress=\"btn_keypress();\"".$inExtra;
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
