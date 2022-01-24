<?php

require_once('for_php7.php');

class knjd137jForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd137jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $query = knjd137jQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        $row = array();
        $kanten = array();
        $sintai = array();
        if (((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) && $model->cmd != "relode") {
            $row = $db->getRow(knjd137jQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
            $kanten = $db->getRow(knjd137jQuery::getViewpoint($model), DB_FETCHMODE_ASSOC);
            $sintai = $db->getRow(knjd137jQuery::getPhysicalrecords($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
            $kanten["REMARK1"] = $model->field["RECORD_VAL02"];
            $sintai["REMARK1"] = $model->field["RECORD_VAL01"];
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //総合的な学習の時間
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $row["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYTIME"]["moji"]."文字X".$model->getPro["TOTALSTUDYTIME"]["gyou"]."行まで)";

        //特別活動の記録ボタン
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_HREPORTREMARK_D_2/knjd_hreportremark_d_2index.php?CALL_PRG="."KNJD137J"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&send_knjdHreportRemark_d2_UseText=".$model->Properties["knjdHreportRemark_d2_UseText"]."',0,0,650,600);\"";
        $arg["button"]["btn_form3"] = KnjCreateBtn($objForm, "btn_form2", "特別活動の記録", $extra);

        //行動の記録ボタン
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG="."KNJD137J"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."',0,0,600,500);\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "行動の記録", $extra);

        //出欠の記録備考 
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->getPro["ATTENDREC_REMARK"]["moji"]."文字X".$model->getPro["ATTENDREC_REMARK"]["gyou"]."行まで)";

        //担任からの所見
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        $arg["data"]["COMMUNICATION_COMMENT"] = "(全角".$model->getPro["COMMUNICATION"]["moji"]."文字X".$model->getPro["COMMUNICATION"]["gyou"]."行まで)";

        //観点
        if ($model->cmd == "relode") {
            $sep = "";
            $query = knjd137jQuery::getJviewGradeMst($model);
            $result = $db->query($query);
            $kanten["REMARK1"] = "";
            while ($relodeRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $kanten["REMARK1"] .= $sep.$relodeRow["VIEWNAME"];
                $sep = "\n";
            }
        }
        $arg["data2"]["RECORD_VAL02"] = getTextOrArea($objForm, "RECORD_VAL02", $model->getPro["RECORD_VAL02"]["moji"], $model->getPro["RECORD_VAL02"]["gyou"],$kanten["REMARK1"], $model);           
        $arg["data2"]["RECORD_VAL02_COMMENT"] = "(全角".$model->getPro["RECORD_VAL02"]["moji"]."文字X".$model->getPro["RECORD_VAL02"]["gyou"]."行まで)";
        //観点マスタより読込
        $extra = "onclick=\"return btn_submit('relode');\"";
        $arg["data2"]["btn_relode"] = knjCreateBtn($objForm, "btn_relode", "観点マスタより読込", $extra);

        //身体の記録
        $arg["data2"]["RECORD_VAL01"] = getTextOrArea($objForm, "RECORD_VAL01", $model->getPro["RECORD_VAL01"]["moji"], $model->getPro["RECORD_VAL01"]["gyou"],$sintai["REMARK1"], $model);           
        $arg["data2"]["RECORD_VAL01_COMMENT"] = "(全角".$model->getPro["RECORD_VAL01"]["moji"]."文字X".$model->getPro["RECORD_VAL01"]["gyou"]."行まで)";

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
        $studySize = ($model->getPro["TOTALSTUDYTIME"]["moji"] * $model->getPro["TOTALSTUDYTIME"]["gyou"]);
        $fieldSize .= "TOTALSTUDYTIME={$studySize}=総合的な学習の時間,";
        $attendrecRemarkSize = ($model->getPro["ATTENDREC_REMARK"]["moji"] * $model->getPro["ATTENDREC_REMARK"]["gyou"]);
        $fieldSize .= "ATTENDREC_REMARK={$attendrecRemarkSize}=出席の記録備考,";
        $commuSize = ($model->getPro["COMMUNICATION"]["moji"] * $model->getPro["COMMUNICATION"]["gyou"]);
        $fieldSize .= "COMMUNICATION={$commuSize}=担任からの所見,";

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_HREPORTREMARK_DAT/knjx_hreportremark_datindex.php?FIELDSIZE=".$fieldSize."&SCHOOL_KIND=J','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "totalstudytime_gyou", $model->getPro["TOTALSTUDYTIME"]["gyou"]);
        knjCreateHidden($objForm, "totalstudytime_moji", $model->getPro["TOTALSTUDYTIME"]["moji"]);
        knjCreateHidden($objForm, "attendrec_remark_gyou", $model->getPro["ATTENDREC_REMARK"]["gyou"]);
        knjCreateHidden($objForm, "attendrec_remark_moji", $model->getPro["ATTENDREC_REMARK"]["moji"]);
        knjCreateHidden($objForm, "communication_gyou", $model->getPro["COMMUNICATION"]["gyou"]);
        knjCreateHidden($objForm, "communication_moji", $model->getPro["COMMUNICATION"]["moji"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        
        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        /**********/
        /* ボタン */
        /**********/
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

        //DB切断
        Query::dbCheckIn($db);

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd137jForm1.html", $arg);
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
        $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\" onPaste=\"return showPaste(this);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onPaste=\"return showPaste(this);\" onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
