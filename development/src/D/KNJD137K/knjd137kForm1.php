<?php

require_once('for_php7.php');

class knjd137kForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd137kindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学年判断用
        $grade_cd = $db->getOne(knjd137kQuery::getGradeCd($model));

        //学期コンボ
        $query = knjd137kQuery::getPrintSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PRINT_SEMESTER"], "PRINT_SEMESTER", $extra, 1, "");

        //記録の取得
        $RowSpecial = $row = array();
        $result = $db->query(knjd137kQuery::getHrepSpecial($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $RowSpecial[$row["DIV"]."_".$row["CODE"]] = $row["REMARK1"];
        }

        //警告メッセージを表示しない場合
        $row = array();
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knjd137kQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
            foreach ($model->itemArrayD038 as $key => $val) {
                $RowSpecial[$key] = $model->field["REMARK1_".$key];
            }
            foreach ($model->itemArrayD034 as $key => $val) {
                $RowSpecial[$key] = $model->field["REMARK1_".$key];
            }
            $RowSpecial["04_01"] = $model->field["REMARK1_04_01"];
            $RowSpecial["03_01"] = $model->field["REMARK1_03_01"];
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        if ($grade_cd == '01' || $grade_cd == '02') {
            $arg["ITEM_DIS"] = "";
            $model->grade01_02Flg = true;
        } else {
            $arg["ITEM_DIS"] = "1";
            $model->grade01_02Flg = false;
        }

        //道徳
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $row["REMARK1"], $model);
        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->getPro["REMARK1"]["moji"]."文字X".$model->getPro["REMARK1"]["gyou"]."行まで)";

        // 外国語
        $specialCnt = 1;
        foreach ($model->itemArrayD038 as $key => $val) {
            $setData = array();

            //タイトルセット
            if ($specialCnt == "1") {
                $setData["ROWSPAN"] = get_count($model->itemArrayD038) + 1;
                $setData["RECORD_NAME"] = "観点";
                $setData["RECORD_VAL"]  = "学習の様子";

                $arg["data2"][] = $setData;
                $specialCnt++;
                $setData = array();
            }
            $setData["NO_TITLE"] = 1;

            //文字、行
            $moji = $model->getPro["reportSpecial".$key]["moji"];
            $gyou = $model->getPro["reportSpecial".$key]["gyou"];

            if ($gyou > 1) {
                //textArea
                $height = $gyou * 13.5 + ($gyou -1 ) * 3 + 5;
                $extra = "style=\"height:".$height."px;\" onPaste=\"return showPaste(this);\""; 
                $setData["RECORD_VAL"] = knjCreateTextArea($objForm, "REMARK1_".$key, $gyou, ($moji * 2 + 1), "soft", $extra, $RowSpecial[$key]);
            } else {
                //textbox
                $extra = "onPaste=\"return showPaste(this);\"";
                $setData["RECORD_VAL"] = knjCreateTextBox($objForm, $RowSpecial[$key], "REMARK1_".$key, ($moji * 2), $moji, $extra);
            }
            $setData["RECORD_COMMENT"] = "(全角".$moji."文字X".$gyou."行まで)";
            $setData["RECORD_NAME"]    = $val["NAME1"];

            $arg["data2"][] = $setData;
        }

        //総合的な学習の時間(テーマ)
        $arg["data"]["REMARK1_04_01"] = getTextOrArea($objForm, "REMARK1_04_01", $model->getPro["reportSpecial04_01"]["moji"], $model->getPro["reportSpecial04_01"]["gyou"], $RowSpecial["04_01"], $model);
        $arg["data"]["REMARK1_04_01_COMMENT"] = "(全角".$model->getPro["reportSpecial04_01"]["moji"]."文字X".$model->getPro["reportSpecial04_01"]["gyou"]."行まで)";
        //総合的な学習の時間(活動の様子)
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $row["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYTIME"]["moji"]."文字X".$model->getPro["TOTALSTUDYTIME"]["gyou"]."行まで)";

        //自立活動(学習内容)
        $arg["data"]["REMARK1_03_01"] = getTextOrArea($objForm, "REMARK1_03_01", $model->getPro["reportSpecial03_01"]["moji"], $model->getPro["reportSpecial03_01"]["gyou"], $RowSpecial["03_01"], $model);
        $arg["data"]["REMARK1_03_01_COMMENT"] = "(全角".$model->getPro["reportSpecial03_01"]["moji"]."文字X".$model->getPro["reportSpecial03_01"]["gyou"]."行まで)";
        //自立活動(学習の様子)
        $arg["data"]["REMARK3"] = getTextOrArea($objForm, "REMARK3", $model->getPro["REMARK3"]["moji"], $model->getPro["REMARK3"]["gyou"], $row["REMARK3"], $model);
        $arg["data"]["REMARK3_COMMENT"] = "(全角".$model->getPro["REMARK3"]["moji"]."文字X".$model->getPro["REMARK3"]["gyou"]."行まで)";

        //特別活動
        $specialCnt = 1;
        foreach ($model->itemArrayD034Sort as $sortKey => $sortVal) {
            $key = $sortVal;
            $val = $model->itemArrayD034[$sortVal];
            $setData = array();

            //タイトルセット
            if ($specialCnt == "1") {
                $setData["ROWSPAN"] = get_count($model->itemArrayD034) + 1;
                $setData["RECORD_NAME"] = "活動場面";
                $setData["RECORD_VAL"]  = "学習の記録";

                $arg["data3"][] = $setData;
                $specialCnt++;
                $setData = array();
            }
            $setData["NO_TITLE"] = 1;

            //文字、行
            $moji = $model->getPro["reportSpecial".$key]["moji"];
            $gyou = $model->getPro["reportSpecial".$key]["gyou"];

            if ($gyou > 1) {
                //textArea
                $height = $gyou * 13.5 + ($gyou -1 ) * 3 + 5;
                $extra = "style=\"height:".$height."px;\" onPaste=\"return showPaste(this);\""; 
                $setData["RECORD_VAL"] = knjCreateTextArea($objForm, "REMARK1_".$key, $gyou, ($moji * 2 + 1), "soft", $extra, $RowSpecial[$key]);
            } else {
                //textbox
                $extra = "onPaste=\"return showPaste(this);\"";
                $setData["RECORD_VAL"] = knjCreateTextBox($objForm, $RowSpecial[$key], "REMARK1_".$key, ($moji * 2), $moji, $extra);
            }
            $setData["RECORD_COMMENT"] = "(全角".$moji."文字X".$gyou."行まで)";
            $setData["RECORD_NAME"]    = $val["NAME1"];

            $arg["data3"][] = $setData;
        }

        //行動の記録ボタン
        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG="."KNJD137K"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."',0,0,600,500);\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "行動の記録", $extra);

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
        View::toHTML($model, "knjd137kForm1.html", $arg);
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
