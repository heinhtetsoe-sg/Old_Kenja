<?php

require_once('for_php7.php');

class knjd139jForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd139jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //学期コンボ
        $query = knjd139jQuery::getSemester();
        $extra = ($model->schregno) ? "onChange=\"return btn_submit('edit');\"" : "disabled";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjd139jQuery::getHreportremarkDat($model->schregno, $model->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
            $Row = $db->getRow(knjd139jQuery::getHreportremarkDat($model->schregno, "9"), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
            $Row =& $model->field;
        }

        //所見項目リスト
        $hreportremark = array("REMARK1"            =>  array($Row, $model->remark1_moji,          $model->remark1_gyou),
                               "REMARK2"            =>  array($Row, $model->remark2_moji,          $model->remark2_gyou),
                               "SPECIALACTREMARK"   =>  array($row, $model->specialactremark_moji, $model->specialactremark_gyou),
                               "ATTENDREC_REMARK"   =>  array($row, $model->attendrec_remark_moji, $model->attendrec_remark_gyou),
                               "COMMUNICATION"      =>  array($row, $model->communication_moji,    $model->communication_gyou));

        //所見テキストエリア
        foreach($hreportremark as $key => $val) {
            if ($val[2] == "1") {
                $arg["data"][$key] = knjCreateTextBox($objForm, $val[0][$key], $key, $val[1]*2, $val[1]*2, "");
            } else {
                $height = $val[2] * 13.5 + ($val[2] -1 ) * 3 + 5;
                $extra = "style=\"height:{$height}px;\" ";
                $arg["data"][$key] = KnjCreateTextArea($objForm, $key, $val[2], ($val[1] * 2 + 1), "soft", $extra, $val[0][$key]);
            }
            $arg["data"][$key."_TYUI"] = "(全角{$val[1]}文字{$val[2]}行まで)";
        }


        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden作成
        makeHidden($objForm, $model);

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

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjd139jForm1.html", $arg);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {
    //部活動参照ボタン
    $extra = ($model->schregno) ? "onclick=\"return btn_submit('subform1');\"" : "disabled";
    $arg["button"]["btn_club"] = knjCreateBtn($objForm, "btn_club", "部活動参照", $extra);
    //委員会参照ボタン
    $extra = ($model->schregno) ? "onclick=\"return btn_submit('subform2');\"" : "disabled";
    $arg["button"]["btn_committee"] = knjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);

    //出欠備考参照ボタン
    $year  = CTRL_YEAR;
    $sdate = CTRL_YEAR.'-04-01';
    $edate = (CTRL_YEAR+1).'-03-31';
    if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
        $extra = ($model->schregno) ? " onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"" : "disabled";
        $arg["button"]["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "まとめ出欠備考参照", $extra);
    } else {
        $extra = ($model->schregno) ? " onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"" : "disabled";
        $arg["button"]["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
    }

    //更新ボタン
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前後の生徒へ
    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    
    //CSVボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_D139J/knjx_d139jindex.php?SCHOOL_KIND={$model->schKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);
}

//コンボ作成
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
?>
