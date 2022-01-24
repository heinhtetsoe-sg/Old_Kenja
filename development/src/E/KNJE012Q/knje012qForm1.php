<?php

require_once('for_php7.php');

class knje012qForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje012qindex.php", "", "edit");
        $db = Query::dbCheckOut();

        if (!isset($model->warning)) {
            $query = knje012qQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //年次取得
        $query = knje012qQuery::getGradeCd($model);
        $getGdat = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->gradecd = "";
        $model->gradecd = $getGdat["GRADE_CD"];
        $model->schoolKind = "";
        $model->schoolKind = $getGdat["SCHOOL_KIND"];

        /******************/
        /* テキストエリア */
        /******************/
        makeHexamEntRemarkDat($objForm, $arg, $db, $model);

        //備考
        if (in_array($model->cmd, array('torikomi0', 'torikomi1', 'torikomi2'))) {
            $row["REMARK"] = $model->field["REMARK"];
        }
        $height = $model->remark_gyou * 13.5 + ($model->remark_gyou -1 ) * 3 + 5;
        $extra = "style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
        $arg["REMARK"] = KnjCreateTextArea($objForm, "REMARK", $model->remark_gyou, ($model->remark_moji * 2 + 1), "soft", $extra, $row["REMARK"]);
        $arg["REMARK_TYUI"] = "(全角{$model->remark_moji}文字X{$model->remark_gyou}行まで)";
        knjCreateHidden($objForm, "tyousasyoRemarkFieldSize_J", $model->Properties["tyousasyoRemarkFieldSize_J"]);

        /********************/
        /* チェックボックス */
        /********************/
        //特記事項なしチェックボックス
        $extra  = ($model->field["NO_COMMENTS"] == "1") ? "checked" : "";
        $extra .= " id=\"NO_COMMENTS\" onclick=\"return CheckRemark();\"";
        $arg["NO_COMMENTS"] = knjCreateCheckBox($objForm, "NO_COMMENTS", "1", $extra, "");

        //特記事項なし
        knjCreateHidden($objForm, "NO_COMMENTS_LABEL", $model->no_comments_label);
        $arg["NO_COMMENTS_LABEL"] = $model->no_comments_label;

        /**********/
        /* ボタン */
        /**********/
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "updEdit", "update");
        //取消しボタンを作成する
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "nextURL", $model->nextURL);
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "mode", $model->mode);
        knjCreateHidden($objForm, "GRD_YEAR", $model->grd_year);
        knjCreateHidden($objForm, "GRD_SEMESTER", $model->grd_semester);
        knjCreateHidden($objForm, "PROGRAMID", PROGRAMID);
        knjCreateHidden($objForm, "LEFT_GRADE", $model->grade);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if(get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent2(0);";
        } elseif($model->cmd =="reset") {
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

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje012qForm1.html", $arg);
    }
}

function makeHexamEntRemarkDat(&$objForm, &$arg, $db, &$model) {
    $model->schArray = array();
    $disabled = "disabled";
    $query = knje012qQuery::selectQueryAnnual($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->schArray[$row["GRADE"]] = array("YEAR"  => $row["YEAR"],
                                                "ANNUAL" => $row["ANNUAL"]);
    }
    $result->free();

    $opt = array();
    $query = knje012qQuery::getGdat($model);

    $result = $db->query($query);
    while ($gRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("GRADE" => $gRow["GRADE"], "GRADE_CD" => $gRow["GRADE_CD"]);
    }
    $result->free();

    $gradeCnt = 0;
    $hiddenYear = "";
    $yearSep = "";
    foreach ($opt as $key) {
        $grade = (int) $key["GRADE_CD"];
        $disabled = is_array($model->schArray[$key["GRADE"]]) ? "" : " disabled ";
        $year = $model->schArray[$key["GRADE"]]["YEAR"];

        //表示用の年度をセット
        if ($year != "" && $grade != "") {
            $arg["YEAR".$grade] = '('.$year.'年度)';
        }

        if ($year) {
            $hiddenYear .= $yearSep.$year;
            $yearSep = ",";
        }
        if (!isset($model->warning)) {
            if (!in_array($model->cmd, array('torikomi0', 'torikomi1', 'torikomi2'))) {
                $query = knje012qQuery::selectQueryForm2($model, $year);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } else {
                $row = $model->field2[$year];
            }
        } else {
            $row = $model->field2[$year];
        }

        //出欠の記録備考取込（対応する学年のみ）
        if ($model->cmd === "torikomi".$gradeCnt) {
            $set_remark = knje012qQuery::getSemesRemark($model, $db, $year);
            $row["ATTENDREC_REMARK"] = $set_remark;
        } elseif ($model->cmd !== "edit" && $model->cmd !== "reset" && $model->cmd !== "updEdit") {
            //対応しない学年の時は画面の値をセット
            $row["ATTENDREC_REMARK"] = $model->field2[$year]["ATTENDREC_REMARK"];
        }

        //出欠の記録備考
        $extra = $disabled." onPaste=\"return showPaste(this, ".$gradeCnt.");\" ";
        $arg["ATTENDREC_REMARK".$grade] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK-".$year, ($model->attendrec_remark_gyou + 1), ($model->attendrec_remark_moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
        $arg["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";

        //出欠の記録参照ボタン
        if ($model->Properties["tyousasyoSyukketsuKirokuBtn_J"] == 1) {
            $extra = $disabled."onclick=\"loadwindow('knje012qindex.php?cmd=syukketsuKirokuSansyo&YEAR={$year}&SCHREGNO={$model->schregno}&GRADE_CD={$grade}',0,document.documentElement.scrollTop || document.body.scrollTop,600,280);\"";
            $arg["btn_syukketsu_sansyo".$grade] = knjCreateBtn($objForm, "btn_syukketsu_sansyo".$grade, "出欠の記録参照", $extra);
            $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
        }

        //出欠備考参照ボタン
        $sdate = ($year) ? $year.'-04-01' : "";
        $edate = ($year) ? ($year+1).'-03-31' : "";
        //和暦表示フラグ
        $warekiFlg = "";
        if ($model->Properties["useWarekiHyoji"] == 1) {
            $warekiFlg = "1";
        }
        if ($model->Properties["useAttendSemesRemarkDat_J"] == 1) {
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat_J"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $setcmd = "torikomi".$gradeCnt;
                $extra = $disabled ." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('".$setcmd."');\"";
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO".$grade] = KnjCreateBtn($objForm, "SANSYO".$grade, $setname, $extra);
        }

        $gradeCnt++;
    }
    knjCreateHidden($objForm, "hiddenYear", $hiddenYear);
}
?>
