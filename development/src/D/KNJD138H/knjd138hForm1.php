<?php

require_once('for_php7.php');

class knjd138hForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd138hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $opt = array();
        $result = $db->query(knjd138hQuery::getSemester($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        $extra = "onChange=\"return btn_submit('edit');\"";
        $arg["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //警告メッセージを表示しない場合
        $setData = array();
        $setData2 = array();
        $setData3 = array();
        $setData4 = array();
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $setData  = $db->getRow(knjd138hQuery::getTrainRow($model, $model->schregno), DB_FETCHMODE_ASSOC);
            $setData2 = $db->getRow(knjd138hQuery::getTrain2Row($model, $model->schregno), DB_FETCHMODE_ASSOC);
            $setData3 = $db->getRow(knjd138hQuery::getTrain3Row($model, $model->schregno), DB_FETCHMODE_ASSOC);
            $setData4 = $db->getRow(knjd138hQuery::getTrainRow($model, $model->schregno, $model->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $setData  =& $model->field;
            $setData2 =& $model->field;
            $setData3 =& $model->field;
            $setData4 =& $model->field;
        }
        
        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学習活動
        $extra = "style=\"height:118px;\"";
        $arg["data"]["TOTALSTUDYTIME"] = getTextOrArea($objForm, "TOTALSTUDYTIME", $model->getPro["TOTALSTUDYTIME"]["moji"], $model->getPro["TOTALSTUDYTIME"]["gyou"], $setData["TOTALSTUDYTIME"], $model);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYTIME"]["moji"]."文字X".$model->getPro["TOTALSTUDYTIME"]["gyou"]."行まで)";

        //評価
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->getPro["REMARK1"]["moji"], $model->getPro["REMARK1"]["gyou"], $setData2["REMARK1"], $model);
        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->getPro["REMARK1"]["moji"]."文字X".$model->getPro["REMARK1"]["gyou"]."行まで)";

        //特別活動等の記録
        $arg["data"]["SPECIALACTREMARK"] = getTextOrArea($objForm, "SPECIALACTREMARK", $model->getPro["SPECIALACTREMARK"]["moji"], $model->getPro["SPECIALACTREMARK"]["gyou"], $setData3["SPECIALACTREMARK"], $model);
        $arg["data"]["SPECIALACTREMARK_COMMENT"] = "(全角".$model->getPro["SPECIALACTREMARK"]["moji"]."文字X".$model->getPro["SPECIALACTREMARK"]["gyou"]."行まで)";

        //出欠席の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $setData4["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->getPro["ATTENDREC_REMARK"]["moji"]."文字X".$model->getPro["ATTENDREC_REMARK"]["gyou"]."行まで)";

        //担任欄
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $setData4["COMMUNICATION"], $model);
        $arg["data"]["COMMUNICATION_COMMENT"] = "(全角".$model->getPro["COMMUNICATION"]["moji"]."文字X".$model->getPro["COMMUNICATION"]["gyou"]."行まで)";

        makeBtn($objForm, $arg, $model, $db);

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
        View::toHTML($model, "knjd138hForm1.html", $arg);
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
        $extra = "style=\"height:".$height."px;\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {

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

    //部活動参照
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_BUKATU/knjx_bukatuindex.php?CALL_PRG="."KNJD138H"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&SETFIELD=SPECIALACTREMARK',0,0,600,500);\"";
    $arg["button"]["btn_club"] = KnjCreateBtn($objForm, "btn_club", "部活動参照", $extra);

    //委員会参照
    $extra = "onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_IINKAI/knjx_iinkaiindex.php?CALL_PRG="."KNJD138H"."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&SETFIELD=SPECIALACTREMARK',0,0,600,500);\"";
    $arg["button"]["btn_committee"] = KnjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);

    //CSV処理
    $studySize = ($model->getPro["TOTALSTUDYTIME"]["moji"] * $model->getPro["TOTALSTUDYTIME"]["gyou"]);
    $fieldSize .= "TOTALSTUDYTIME={$studySize}=学習内容（通年）,";
    $remark1Size = ($model->getPro["REMARK1"]["moji"] * $model->getPro["REMARK1"]["gyou"]);
    $fieldSize .= "REMARK1={$remark1Size}=評価（通年）,";
    $spActRemarkSize = ($model->getPro["SPECIALACTREMARK"]["moji"] * $model->getPro["SPECIALACTREMARK"]["gyou"]);
    $fieldSize .= "SPECIALACTREMARK={$spActRemarkSize}=特別活動等の記録（通年）,";
    $attendrecRemarkSize = ($model->getPro["ATTENDREC_REMARK"]["moji"] * $model->getPro["ATTENDREC_REMARK"]["gyou"]);
    $fieldSize .= "ATTENDREC_REMARK={$attendrecRemarkSize}=出欠席の記録備考,";
    $commuSize = ($model->getPro["COMMUNICATION"]["moji"] * $model->getPro["COMMUNICATION"]["gyou"]);
    $fieldSize .= "COMMUNICATION={$commuSize}=担任欄,";

    //一括更新ボタン
    $link  = REQUESTROOT."/D/KNJD138H/knjd138hindex.php?cmd=replace&SCHREGNO=".$model->schregno;
    $extra = "style=\"width:80px\" onclick=\"Page_jumper('$link');\"";
    $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);

    //CSVボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_D138H/knjx_d138hindex.php?FIELDSIZE=".$fieldSize."&SCHOOL_KIND=H','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);
}
?>
