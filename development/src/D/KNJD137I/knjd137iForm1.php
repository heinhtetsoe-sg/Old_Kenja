<?php

require_once('for_php7.php');

class knjd137iForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd137iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学年判断用
        $grade_cd = $db->getOne(knjd137iQuery::getGradeCd($model));

        //学期コンボ
        $query = knjd137iQuery::getPrintSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PRINT_SEMESTER"], "PRINT_SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        $row = array();
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knjd137iQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //外国語活動の記録
        $arg["data"]["FOREIGNLANGACT"] = getTextOrArea($objForm, "FOREIGNLANGACT", $model->getPro["FOREIGNLANGACT"]["moji"], $model->getPro["FOREIGNLANGACT"]["gyou"], $row["FOREIGNLANGACT"], $model);
        $arg["data"]["FOREIGNLANGACT_COMMENT"] = "(全角".$model->getPro["FOREIGNLANGACT"]["moji"]."文字X".$model->getPro["FOREIGNLANGACT"]["gyou"]."行まで)";
        $arg["foreign"] = ($grade_cd == '05' || $grade_cd == '06') ? 1 : "";

        //総合的な学習の時間
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $row["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYTIME"]["moji"]."文字X".$model->getPro["TOTALSTUDYTIME"]["gyou"]."行まで)";

        //生活・特別活動のようすボタン
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG="."KNJD137I"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."',0,0,600,500);\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "生活・特別活動のようす", $extra);

        //出欠の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->getPro["ATTENDREC_REMARK"]["moji"]."文字X".$model->getPro["ATTENDREC_REMARK"]["gyou"]."行まで)";

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

        //自立活動
        $arg["data"]["REMARK3"] = getTextOrArea($objForm, "REMARK3", $model->getPro["REMARK3"]["moji"], $model->getPro["REMARK3"]["gyou"], $row["REMARK3"], $model);
        $arg["data"]["REMARK3_COMMENT"] = "(全角".$model->getPro["REMARK3"]["moji"]."文字X".$model->getPro["REMARK3"]["gyou"]."行まで)";

        //学校から
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
        View::toHTML($model, "knjd137iForm1.html", $arg);
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
    if ($name == "PRINT_SEMESTER") {
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
