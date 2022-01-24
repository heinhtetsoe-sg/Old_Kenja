<?php

require_once('for_php7.php');

class knjd132bForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd132bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //校種取得
        $schoolKind = $db->getOne(knjd132bQuery::getSchoolKind($model));

        //学期コンボ
        $query = knjd132bQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('semester');\"";
        if (!$model->field["SEMESTER"]) {
            $model->field["SEMESTER"] = CTRL_SEMESTER;
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        $row = array();
        if ($model->cmd == "edit") {
            // 特別活動の記録
            $query = knjd132bQuery::getHreportremarkDetailDat($model, "01", "01");
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["DETAIL_REMARK1"] = $row["REMARK1"];
            $model->field["DETAIL_REMARK2"] = $row["REMARK2"];
            // 特別活動の記録(状況)
            $query = knjd132bQuery::getHreportremarkDetailDat($model, "01", "02");
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["DETAIL_REMARK1_CHK"] = $row["REMARK1"];
            $model->field["DETAIL_REMARK2_CHK"] = $row["REMARK2"];
            // 道徳、特記すべき事項
            $query = knjd132bQuery::getHreportremarkDat($model, "9");
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["REMARK1"] = $row["REMARK1"];
            $model->field["COMMUNICATION"] = $row["COMMUNICATION"];
            // 出欠の記録備考
            $query = knjd132bQuery::getHreportremarkDat($model, $model->field["SEMESTER"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["ATTENDREC_REMARK"] = $row["ATTENDREC_REMARK"];
        }
        if ($model->cmd == "semester") {
            // 出欠の記録備考
            $query = knjd132bQuery::getHreportremarkDat($model, $model->field["SEMESTER"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["ATTENDREC_REMARK"] = $row["ATTENDREC_REMARK"];
        }
        $arg["NOT_WARNING"] = 1;

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //特別活動の記録ボタン
        $moji = $model->getPro["DETAIL_REMARK1"]["moji"];
        $gyou = $model->getPro["DETAIL_REMARK1"]["gyou"];
        $ext = "id=\"DETAIL_REMARK1\"";
        $arg["data"]["DETAIL_REMARK1"] = getTextOrArea($objForm, "DETAIL_REMARK1", $moji, $gyou, $model->field["DETAIL_REMARK1"], $model, $ext);
        $arg["data"]["DETAIL_REMARK1_COMMENT"] = getTextAreaComment($moji, $gyou);
        knjCreateHidden($objForm, "DETAIL_REMARK1_KETA", $moji*2);
        knjCreateHidden($objForm, "DETAIL_REMARK1_GYO", $gyou);
        KnjCreateHidden($objForm, "DETAIL_REMARK1_STAT", "statusarea1");
        $extra="";
        $check = ($model->field["DETAIL_REMARK1_CHK"] == "1") ? " checked " : "";
        $arg["data"]["DETAIL_REMARK1_CHK"] = knjCreateCheckBox($objForm, "DETAIL_REMARK1_CHK", "1", $check.$extra, "");

        $moji = $model->getPro["DETAIL_REMARK2"]["moji"];
        $gyou = $model->getPro["DETAIL_REMARK2"]["gyou"];
        $ext = "id=\"DETAIL_REMARK2\"";
        $arg["data"]["DETAIL_REMARK2"] = getTextOrArea($objForm, "DETAIL_REMARK2", $moji, $gyou, $model->field["DETAIL_REMARK2"], $extra, $ext);
        $arg["data"]["DETAIL_REMARK2_COMMENT"] = getTextAreaComment($moji, $gyou);
        knjCreateHidden($objForm, "DETAIL_REMARK2_KETA", $moji*2);
        knjCreateHidden($objForm, "DETAIL_REMARK2_GYO", $gyou);
        KnjCreateHidden($objForm, "DETAIL_REMARK2_STAT", "statusarea2");
        $extra="";
        $check = ($model->field["DETAIL_REMARK2_CHK"] == "1") ? " checked " : "";
        $arg["data"]["DETAIL_REMARK2_CHK"] = knjCreateCheckBox($objForm, "DETAIL_REMARK2_CHK", "1", $check.$extra, "");

        //部活動参照
        $extra = "onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_club"] = knjCreateBtn($objForm, "btn_club", "部活動選択", $extra);
        //委員会参照
        $extra = "onclick=\"return btn_submit('subform2_1');\"";
        $arg["button"]["btn_committee_remark1"] = knjCreateBtn($objForm, "btn_committee_remark1", "委員会選択", $extra);
        //委員会参照
        $extra = "onclick=\"return btn_submit('subform2_2');\"";
        $arg["button"]["btn_committee_remark2"] = knjCreateBtn($objForm, "btn_committee_remark2", "委員会選択", $extra);

        //道徳
        $moji = $model->getPro["REMARK1"]["moji"];
        $gyou = $model->getPro["REMARK1"]["gyou"];
        $ext = "id=\"REMARK1\"";
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $moji, $gyou, $model->field["REMARK1"], $model, $ext);
        $arg["data"]["REMARK1_COMMENT"] = getTextAreaComment($moji, $gyou);
        knjCreateHidden($objForm, "REMARK1_KETA", $moji*2);
        knjCreateHidden($objForm, "REMARK1_GYO", $gyou);
        KnjCreateHidden($objForm, "REMARK1_STAT", "statusarea3");

        //行動の記録ボタン
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG="."KNJD132B"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."',0,0,600,500);\"";
        $arg["button"]["btn_form1"] = KnjCreateBtn($objForm, "btn_form1", "行動の記録", $extra);

        //出欠の記録備考
        $moji = $model->getPro["ATTENDREC_REMARK"]["moji"];
        $gyou = $model->getPro["ATTENDREC_REMARK"]["gyou"];
        $ext = "id=\"ATTENDREC_REMARK\"";
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $moji, $gyou, $model->field["ATTENDREC_REMARK"], $model, $ext);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = getTextAreaComment($moji, $gyou);
        knjCreateHidden($objForm, "ATTENDREC_REMARK_KETA", $moji*2);
        knjCreateHidden($objForm, "ATTENDREC_REMARK_GYO", $gyou);
        KnjCreateHidden($objForm, "ATTENDREC_REMARK_STAT", "statusarea4");

        //出欠備考参照ボタン
        $sdate = $model->exp_year.'-04-01';
        $edate = ($model->exp_year+1).'-03-31';
        if ($model->schregno) {
            if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
                $extra = "onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
                $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "まとめ出欠備考参照", $extra);
            } else {
                $extra = "onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,0,420,300);return ;\"";
                $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
            }
        } else {
            $extra = "onclick=\"alert('データを指定してください。')\"";
            if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
                $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "まとめ出欠備考参照", $extra);
            } else {
                $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
            }
        }

        //特記すべき事項
        $moji = $model->getPro["COMMUNICATION"]["moji"];
        $gyou = $model->getPro["COMMUNICATION"]["gyou"];
        $ext = "id=\"COMMUNICATION\"";
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $moji, $gyou, $model->field["COMMUNICATION"], $model, $ext);
        $arg["data"]["COMMUNICATION_COMMENT"] = getTextAreaComment($moji, $gyou);
        knjCreateHidden($objForm, "COMMUNICATION_KETA", $moji*2);
        knjCreateHidden($objForm, "COMMUNICATION_GYO", $gyou);
        KnjCreateHidden($objForm, "COMMUNICATION_STAT", "statusarea5");

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

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0); ";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list'); ";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML5($model, "knjd132bForm1.html", $arg);
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

function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $ext_outstyle) {
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
        $extra = "style=\"height:".$height."px;\" ".$ext_outstyle;
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" ".$ext_outstyle;
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
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
