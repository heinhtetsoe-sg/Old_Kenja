<?php

require_once('for_php7.php');

class knjd135jForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd135jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学期コンボ
        $query = knjd135jQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if (((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) && $model->cmd != 'attend') {
            $row  = array();
            $row9 = array();
            $remark = array();
            
            foreach($model->detail as $key => $value){
                if($value[0]==''){
                    $value[0] = $model->field["SEMESTER"]; 
                }
                $query = knjd135jQuery::getHreportremarkDetailDat($model, $value[0], $value[1], $value[2]);
	            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $remark['DETAIL_'.$row["DIV"].'_'.$row["CODE"].'_REMARK1'] = $row["REMARK1"];
            }

            $row  = $db->getRow(knjd135jQuery::getHreportremarkDat($model), DB_FETCHMODE_ASSOC);
            $row9 = $db->getRow(knjd135jQuery::getHreportremarkDat($model, '9'), DB_FETCHMODE_ASSOC);

            $arg["NOT_WARNING"] = 1;
        } else {
            $remark =& $model->field;
            $row["ATTENDREC_REMARK"] = $model->field["ATTENDREC_REMARK"];
            $row9["REMARK1"]         = $model->field["MORAL"];
        }

        //総合的な学習の時間の型表記
        if ($model->Properties["tutisyoSougouHyoukaTunen"] == 1) {
            $arg["COMMENT"] = '（ 追記 ）';
        } else {
            $arg["COMMENT"] = "";
        }
        
        //学校を判断
        if ($model->schoolName == "kyoto") {
            $arg["MORAL"] = "1";
        }

        /************/
        /* テキスト */
        /************/
        foreach ($model->detail as $key) {
            list ($semester, $div, $code, $field, $default_moji, $default_gyou, $comment) = $key;
            $name    = 'DETAIL_'.$div.'_'.$code.'_'.$field;
            $arg["data"][$name] = getTextOrArea($objForm, $name, $model->getPro[$name]["moji"], $model->getPro[$name]["gyou"], $remark[$name], $model);
            $arg["data"][$name."_COMMENT"] = "(全角".$model->getPro[$name]["moji"]."文字X".$model->getPro[$name]["gyou"]."行まで)";
        }
        
        //道徳
        $arg["data"]["MORAL"] = getTextOrArea($objForm, "MORAL", $model->getPro["MORAL"]["moji"], $model->getPro["MORAL"]["gyou"], $row9["REMARK1"], $model);
        $arg["data"]["MORAL_COMMENT"] = "(全角".$model->getPro["MORAL"]["moji"]."文字X".$model->getPro["MORAL"]["gyou"]."行まで)";

        //出欠の記録備考取得
        if ($model->cmd === 'attend') {
            $attend_remark = "";
            $query = knjd135jQuery::getAttendSemesRemarkDat($model);
            $result = $db->query($query);
            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($attend_remark == "") {
                    $attend_remark .= $row1["REMARK1"];
                } else {
                    if ($row1["REMARK1"] != "") {
                        $attend_remark .= "／".$row1["REMARK1"];
                    }
                }
            }
            $row["ATTENDREC_REMARK"] = $attend_remark;
        }
        //出欠の記録備考 
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->getPro["ATTENDREC_REMARK"]["moji"]."文字X".$model->getPro["ATTENDREC_REMARK"]["gyou"]."行まで)";


        /**********/
        /* ボタン */
        /**********/
        //まとめ出欠備考ボタン
        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $extra = $disabled ." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('attend');\"";
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR=".CTRL_YEAR."&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["button"]["btn_attendremark"] = KnjCreateBtn($objForm, "btn_attendremark", $setname, $extra);
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
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_D135J/knjx_d135jindex.php?SCHOOL_KIND={$model->schKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);


        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");

        foreach ($model->detail as $key) {
            list ($semester, $div, $code, $field, $default_moji, $default_gyou, $comment) = $key;
            $name = 'DETAIL_'.$div.'_'.$code.'_'.$field;
            knjCreateHidden($objForm, $name."_gyou", $model->getPro[$name]["gyou"]);
            knjCreateHidden($objForm, $name."_moji", $model->getPro[$name]["moji"]);
        }
        knjCreateHidden($objForm, "attendrec_remark_gyou", $model->getPro["ATTENDREC_REMARK"]["gyou"]);
        knjCreateHidden($objForm, "attendrec_remark_moji", $model->getPro["ATTENDREC_REMARK"]["moji"]);
        knjCreateHidden($objForm, "moral_gyou", $model->getPro["MORAL"]["gyou"]);
        knjCreateHidden($objForm, "moral_moji", $model->getPro["MORAL"]["moji"]);
        
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
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd135jForm1.html", $arg);
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
