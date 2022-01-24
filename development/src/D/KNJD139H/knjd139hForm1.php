<?php

require_once('for_php7.php');

class knjd139hForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd139hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //学期コンボ
        $query = knjd139hQuery::getSemester();
        $extra = ($model->schregno) ? "onChange=\"return btn_submit('edit');\"" : "disabled";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjd139hQuery::getHreportremarkDat($model->schregno, $model->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //出席の記録備考所見入力
        if ($model->attendrec_remark_gyou == "1") {
            $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextBox($objForm, $row["ATTENDREC_REMARK"], "ATTENDREC_REMARK", $model->attendrec_remark_moji*2, $model->attendrec_remark_moji*2, "");
        } else {
            $height = $model->attendrec_remark_gyou * 13.5 + ($model->attendrec_remark_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ($model->attendrec_remark_moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
        }
        $arg["data"]["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";

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

        View::toHTML($model, "knjd139hForm1.html", $arg);
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

    //出欠備考参照ボタン
    $year  = CTRL_YEAR;
    $sdate = CTRL_YEAR.'-04-01';
    $edate = (CTRL_YEAR+1).'-03-31';
    if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        $arg["button"]["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "まとめ出欠備考参照", $extra);
    } else {
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
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
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_D139H/knjx_d139hindex.php?SCHOOL_KIND={$model->schKind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
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
