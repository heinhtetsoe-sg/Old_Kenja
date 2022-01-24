<?php

require_once('for_php7.php');

class knjd138pForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd138pindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $grade_cd = $db->getOne(knjd138pQuery::getGradeCd($model));

        if(isset($model->schregno) && $grade_cd == "") {
            $model->setWarning("MSG300",'小学生を選択して下さい。');
            unset($model->schregno);
            unset($model->name);
        }

        //学期コンボ
        $query = knjd138pQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        $row = array();
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $grade_cd = $db->getOne(knjd138pQuery::getGradeCd($model));
            $row = $db->getRow(knjd138pQuery::getTrainRow($model, $grade_cd), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //総合的な学習の時間
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $row["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYTIME"]["moji"]."文字X".$model->getPro["TOTALSTUDYTIME"]["gyou"]."行まで)";
        if ((int)$grade_cd >= 3) {
            $arg["study"] = 1;
        } else {
            $arg["notstudy"] = 1;
        
        }

        //特別活動のようすボタン
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_LM/knjd_behavior_lmindex.php?CALL_PRG="."KNJD138P"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHOOL_KIND=P&SCHREGNO=".$model->schregno."&GRADE=".$model->grade."&send_knjdBehaviorsd_UseText_P=".$model->Properties["knjdBehaviorsd_UseText_P"]."',0,0,800,500);\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "生活・特別活動のようす", $extra);

        //特別活動のようす(委員会活動)
        $arg["data"]["COMMITTEE"] = getTextOrArea($objForm, "COMMITTEE", $model->getPro["COMMITTEE"]["moji"], $model->getPro["COMMITTEE"]["gyou"], $row["COMMITTEE"], $model);
        $arg["data"]["COMMITTEE_COMMENT"] = "(全角".$model->getPro["COMMITTEE"]["moji"]."文字X".$model->getPro["COMMITTEE"]["gyou"]."行まで)";

        //特別活動のようす(係活動)
        $arg["data"]["REMARK2"] = getTextOrArea($objForm, "REMARK2", $model->getPro["REMARK2"]["moji"], $model->getPro["REMARK2"]["gyou"], $row["REMARK2"], $model);
        $arg["data"]["REMARK2_COMMENT"] = "(全角".$model->getPro["REMARK2"]["moji"]."文字X".$model->getPro["REMARK2"]["gyou"]."行まで)";

        //特別活動のようす(部活動)
        $arg["data"]["CLUB"] = getTextOrArea($objForm, "CLUB", $model->getPro["CLUB"]["moji"], $model->getPro["CLUB"]["gyou"], $row["CLUB"], $model);
        $arg["data"]["CLUB_COMMENT"] = "(全角".$model->getPro["CLUB"]["moji"]."文字X".$model->getPro["CLUB"]["gyou"]."行まで)";

        //総合所見
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        $arg["data"]["COMMUNICATION_COMMENT"] = "(全角".$model->getPro["COMMUNICATION"]["moji"]."文字X".$model->getPro["COMMUNICATION"]["gyou"]."行まで)";

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

        //CSV処理
        $fieldSize  = "";
        $studySize = ($model->getPro["TOTALSTUDYTIME"]["moji"] * $model->getPro["TOTALSTUDYTIME"]["gyou"]);
        $fieldSize .= "TOTALSTUDYTIME={$studySize}=総合的な学習の時間所見,";
        $remark1Size = ($model->getPro["COMMITTEE"]["moji"] * $model->getPro["COMMITTEE"]["gyou"]);
        $fieldSize .= "COMMITTEE={$remark1Size}=特別活動のようす(委員会活動)所見,";
        $remark2Size = ($model->getPro["REMARK2"]["moji"] * $model->getPro["REMARK2"]["gyou"]);
        $fieldSize .= "REMARK2={$remark2Size}=特別活動のようす(係活動)所見,";
        $remark3Size = ($model->getPro["CLUB"]["moji"] * $model->getPro["CLUB"]["gyou"]);
        $fieldSize .= "CLUB={$remark3Size}=特別活動のようす(クラブ活動)所見,";
        $commuSize = ($model->getPro["COMMUNICATION"]["moji"] * $model->getPro["COMMUNICATION"]["gyou"]);
        $fieldSize .= "COMMUNICATION={$commuSize}=通信欄所見,";

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_HREPORTREMARK_DAT/knjx_hreportremark_datindex.php?FIELDSIZE=".$fieldSize."&SCHOOL_KIND=P&SEND_PRGID=KNJD138P&SEND_AUTH=".AUTHORITY."&PROGRAMID=KNJD138P','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

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

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML5($model, "knjd138pForm1.html", $arg);
    }
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
        $extra = "style=\"height:".$height."px;\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
