<?php

require_once('for_php7.php');

class knjd138jForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd138jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $opt = array();
        $result = $db->query(knjd138jQuery::getSemester($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        $extra = "onChange=\"return btn_submit('edit');\"";
        $arg["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //出力項目取得
        $query = knjd138jQuery::getNameMst($model, "D034");
        $result = $db->query($query);
        $model->itemArray = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArray[$setItem["NAMECD2"]] = $setItem;
        }

        //記録の取得
        $RowSpecial = $row = array();
        $result = $db->query(knjd138jQuery::getHrepSpecial($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $RowSpecial[$row["CODE"]] = $row["REMARK1"];
        }
        $result->free();

        //詳細データより取得
        $Rowd = $row = array();
        $result = $db->query(knjd138jQuery::getDetailDat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Rowd["REMARK03_".$row["CODE"]] = $row["REMARK1"];
        }
        $result->free();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knjd138jQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
            $Row = $db->getRow(knjd138jQuery::getTrainRow($model, "1"), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
            $Row =& $model->Field;
            $Rowd =& $model->Field;
            foreach ($model->itemArray as $key => $val) {
                $RowSpecial[$key] = $model->Field["REMARK".$key];
            }
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学習活動
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $Row["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYTIME"]["moji"]."文字X".$model->getPro["TOTALSTUDYTIME"]["gyou"]."行まで)";

        //観点
        $arg["data"]["REMARK03_03"] = getTextOrArea($objForm, "REMARK03_03", $model->getPro["REMARK03_03"]["moji"], $model->getPro["REMARK03_03"]["gyou"], $Rowd["REMARK03_03"], $model);
        $arg["data"]["REMARK03_03_COMMENT"] = "(全角".$model->getPro["REMARK03_03"]["moji"]."文字X".$model->getPro["REMARK03_03"]["gyou"]."行まで)";

        //評価
        $arg["data"]["REMARK03_04"] = getTextOrArea($objForm, "REMARK03_04", $model->getPro["REMARK03_04"]["moji"], $model->getPro["REMARK03_04"]["gyou"], $Rowd["REMARK03_04"], $model);
        $arg["data"]["REMARK03_04_COMMENT"] = "(全角".$model->getPro["REMARK03_04"]["moji"]."文字X".$model->getPro["REMARK03_04"]["gyou"]."行まで)";

        //行動の状況ボタン
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG="."KNJD138J"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."',0,0,600,500);\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "行動の状況", $extra);

        //特別活動等の記録ボタン
        $specialCnt = 1;
        foreach ($model->itemArray as $key => $val) {
            $setData = array();

            if ($model->Properties["reportSpecialSize01_".$key]) {
                list($moji, $gyou) = preg_split("/\*/", $model->Properties["reportSpecialSize01_".$key]);
                $model->getPro["reportSpecial".$key."_moji"] = (int)trim($moji);
                $model->getPro["reportSpecial".$key."_gyou"] = (int)trim($gyou);
            } else {
                $model->getPro["reportSpecial".$key."_moji"] = 33;
                $model->getPro["reportSpecial".$key."_gyou"] = 1;
            }

            if ($model->getPro["reportSpecial".$key."_gyou"] > 1) {
                //textArea
                $height = $model->getPro["reportSpecial".$key."_gyou"] * 13.5 + ($model->getPro["reportSpecial".$key."_gyou"] -1 ) * 3 + 5;
                $extra = "style=\"height:".$height."px;\" onPaste=\"return showPaste(this);\""; 
                $setData["RECORD_VAL"] = knjCreateTextArea($objForm, "REMARK".$key, $model->getPro["reportSpecial".$key."_gyou"], ($moji * 2 + 1), "soft", $extra, $RowSpecial[$key]);
            } else {
                //textbox
                $moji = $model->getPro["reportSpecial".$key."_moji"];
                $extra = "onPaste=\"return showPaste(this);\"";
                $setData["RECORD_VAL"] = knjCreateTextBox($objForm, $RowSpecial[$key], "REMARK".$key, ($moji * 2), $moji, $extra);
            }
            $setData["RECORD_COMMENT"] = "(全角".$model->getPro["reportSpecial".$key."_moji"]."文字X".$model->getPro["reportSpecial".$key."_gyou"]."行まで)";
            $setData["RECORD_NAME"] = $val["NAME1"];
            if ($specialCnt == "1") {
                $setSpecialCnt = get_count($model->itemArray);
                $setData["SPECIAL_SPAN"] = "<th rowspan=\"{$setSpecialCnt}\" width=\"30\" class=\"no_search\" nowrap>特<br>別<br>活<br>動<br>等<br>の<br>記<br>録</th>";
            }

            //CVS処理用
            $specialSize = ($model->getPro["reportSpecial".$key."_moji"] * $model->getPro["reportSpecial".$key."_gyou"]);
            $fieldSize3 .= "REMARK{$key}={$specialSize}={$val["NAME1"]}（通年）,";

            $arg["data2"][] = $setData;
            $specialCnt++;
        }
        //部活動参照
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_BUKATU/knjx_bukatuindex.php?CALL_PRG="."KNJD138J"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&SETFIELD=REMARK03',0,0,600,500);\"";
        $arg["button"]["btn_club"] = KnjCreateBtn($objForm, "btn_club", "部活動参照", $extra);

        //委員会参照
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_IINKAI/knjx_iinkaiindex.php?CALL_PRG="."KNJD138J"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&SETFIELD=REMARK02',0,0,600,500);\"";
        $arg["button"]["btn_committee"] = KnjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);

        //学校からの所見
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        $arg["data"]["COMMUNICATION_COMMENT"] = "(全角".$model->getPro["COMMUNICATION"]["moji"]."文字X".$model->getPro["COMMUNICATION"]["gyou"]."行まで)";

        //出欠の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->getPro["ATTENDREC_REMARK"]["moji"]."文字X".$model->getPro["ATTENDREC_REMARK"]["gyou"]."行まで)";

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
        $fieldSize2 .= "TOTALSTUDYTIME={$studySize}=学習活動（通年）,";
        $remark03_03Size = ($model->getPro["REMARK03_03"]["moji"] * $model->getPro["REMARK03_03"]["gyou"]);
        $fieldSize2 .= "REMARK03_03={$remark03_03Size}=観点（通年）,";
        $remark03_04Size = ($model->getPro["REMARK03_04"]["moji"] * $model->getPro["REMARK03_04"]["gyou"]);
        $fieldSize2 .= "REMARK03_04={$remark03_04Size}=評価（通年）,";

        $attendrecRemarkSize = ($model->getPro["ATTENDREC_REMARK"]["moji"] * $model->getPro["ATTENDREC_REMARK"]["gyou"]);
        $fieldSize .= "ATTENDREC_REMARK={$attendrecRemarkSize}=出席の記録備考,";
        $commuSize = ($model->getPro["COMMUNICATION"]["moji"] * $model->getPro["COMMUNICATION"]["gyou"]);
        $fieldSize .= "COMMUNICATION={$commuSize}=学校からの所見,";

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_D138J/knjx_d138jindex.php?FIELDSIZE=".$fieldSize."&FIELDSIZE2=".$fieldSize2."&FIELDSIZE3=".$fieldSize3."&SCHOOL_KIND=J','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "totalstudytime_gyou", $model->getPro["TOTALSTUDYTIME"]["gyou"]);
        knjCreateHidden($objForm, "totalstudytime_moji", $model->getPro["TOTALSTUDYTIME"]["moji"]);
        knjCreateHidden($objForm, "remark1_gyou", $model->getPro["REMARK1"]["gyou"]);
        knjCreateHidden($objForm, "remark1_moji", $model->getPro["REMARK1"]["moji"]);
        knjCreateHidden($objForm, "remark2_gyou", $model->getPro["REMARK2"]["gyou"]);
        knjCreateHidden($objForm, "remark2_moji", $model->getPro["REMARK2"]["moji"]);
        knjCreateHidden($objForm, "attemdrec_remark_gyou", $model->getPro["ATTENDREC_REMARK"]["gyou"]);
        knjCreateHidden($objForm, "attemdrec_remark_moji", $model->getPro["ATTENDREC_REMARK"]["moji"]);
        knjCreateHidden($objForm, "communication_gyou", $model->getPro["COMMUNICATION"]["gyou"]);
        knjCreateHidden($objForm, "communication_moji", $model->getPro["COMMUNICATION"]["moji"]);
        knjCreateHidden($objForm, "remark01_moji", $model->remark01_moji);
        knjCreateHidden($objForm, "remark01_gyou", $model->remark01_gyou);
        knjCreateHidden($objForm, "remark02_moji", $model->remark02_moji);
        knjCreateHidden($objForm, "remark02_gyou", $model->remark02_gyou);
        knjCreateHidden($objForm, "remark03_moji", $model->remark03_moji);
        knjCreateHidden($objForm, "remark03_gyou", $model->remark03_gyou);
        knjCreateHidden($objForm, "remark04_moji", $model->remark04_moji);
        knjCreateHidden($objForm, "remark04_gyou", $model->remark04_gyou);
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
        View::toHTML($model, "knjd138jForm1.html", $arg);
    }
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
        $extra = "style=\"height:".$height."px;\" onPaste=\"return showPaste(this);\""; 
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onPaste=\"return showPaste(this);\" onkeypress=\"btn_keypress();\""; 
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
