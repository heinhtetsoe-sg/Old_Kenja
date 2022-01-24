<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010bForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje010bindex.php", "", "edit");
        $db = Query::dbCheckOut();

        $model->schoolKind = $db->getOne(knje010bQuery::getSchoolKind($model));

        if (!isset($model->warning) && $model->cmd != "reload2" && $model->cmd != "reload3") {
            $query = knje010bQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //読込みボタンが押された時の通知書より読込む
        if ($model->cmd == 'reload3') {
            $totalstudytimeArray = array();
            $totalstudyactArray  = array();
            $query = knje010bQuery::get_hreportremark_dat($model);
            $result = $db->query($query);
            while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($total_row["TOTALSTUDYTIME"] != '') {
                    $totalstudytimeArray[] = $total_row["TOTALSTUDYTIME"];
                }
                if ($total_row["TOTALSTUDYACT"] != '') {
                    $totalstudyactArray[] = $total_row["TOTALSTUDYACT"];
                }
            }
            $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
            $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
        }

        /******************/
        /* コンボボックス */
        /******************/
        $disabled = " disabled ";
        $opt = array();
        $opt[] = array("label" => "全て",
                       "value" => "0000");
        $model->readYear = $model->readYear ? $model->readYear : "0000";
        $query = knje010bQuery::selectQueryAnnual($model);
        $result = $db->query($query);
        while ($readRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => (int) $readRow["GRADE"] ."学年　".$readRow["YEAR"] ."年度",
                           "value" => $readRow["YEAR"]
                          );
            $disabled = "";
        }
        $arg["btn_readYear"] = knjCreateCombo(&$objForm, "READ_YEAR", $model->readYear, $opt, $disabled, 1);

        /******************/
        /* テキストエリア */
        /******************/
        makeHexamEntRemarkDat(&$objForm, &$arg, $db, &$model);

        //活動内容
        $height = $model->totalstudyact_gyou * 13.5 + ($model->totalstudyact_gyou -1 ) * 3 + 5;
        $extra = "style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
        $arg["TOTALSTUDYACT"] = KnjCreateTextArea(&$objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);
        $arg["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
        //評価
        $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1 ) * 3 + 5;
        $extra = "style=\"height:{$height}px;\" onPaste=\"return showKotei(this);\" ";
        $arg["TOTALSTUDYVAL"] = KnjCreateTextArea(&$objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
        $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        //備考
        $extra = "style=\"height:77px;\" onPaste=\"return showKotei(this);\" ";
        $arg["REMARK"] = KnjCreateTextArea(&$objForm, "REMARK", 5, 83, "soft", $extra, $row["REMARK"]);
        if ($model->schoolName == 'tottori') {
            $arg["REMARK_TYUI"] = "(全角41文字X4行まで)";
        } else {
            $arg["REMARK_TYUI"] = "(全角41文字X5行まで)";
        }

        /********************/
        /* チェックボックス */
        /********************/
        //学習成績概評チェックボックス
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "COMMENTEX_A_CD",
                            "checked"   => ($row["COMMENTEX_A_CD"]==1)? true:false,
                            "value"     => 1,
                            "extrahtml" => "id=\"comment\""));
        $arg["COMMENTEX_A_CD"] = $objForm->ge("COMMENTEX_A_CD");

        /**********/
        /* ボタン */
        /**********/
        //生徒指導要録より読込ボタンを作成する
        $extra = $disabled."onclick=\" return btn_submit('reload2');\"";
        $arg["btn_reload2"] = KnjCreateBtn(&$objForm, "btn_reload2", "生徒指導要録より読込", $extra);
        //部活動参照ボタンを作成する
        $extra = "onclick=\" return btn_submit('subform3');\"";
        $arg["btn_club"] = KnjCreateBtn(&$objForm, "btn_club", "部活動参照", $extra);
        //記録備考参照ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $extra = "onclick=\" return btn_submit('subform8');\"";
            $arg["btn_subform8"] = KnjCreateBtn(&$objForm, "btn_subform8", "記録備考参照", $extra);
        }
        //資格参照
        $extra = "onclick=\"return btn_submit('subform5');\"";
        $arg["button"]["SIKAKU_SANSYO"] = knjCreateBtn(&$objForm, "SIKAKU_SANSYO", "資格参照", $extra);
        //指導上参考となる諸事項
        $query = knje010bQuery::cntSchregBaseMst($model);
        $base_cnt = $db->getOne($query);
        if ($base_cnt > 0) {
            $extra = "onclick=\"return btn_submit('subform6');\" style=\"width:180px;\"";
            $arg["button"]["KYU_SYOUSASYO"] = knjCreateBtn(&$objForm, "KYU_SYOUSASYO", "指導上参考となる諸事項", $extra);
        }
        //成績参照ボタンを作成する
        $extra = "onclick=\"return btn_submit('form3_first');\" style=\"width:70px\"";
        $arg["btn_form3"] = knjCreateBtn(&$objForm, "btn_form3", "成績参照", $extra);
        //通知書より読込み
        $extra = "onclick=\"return btn_submit('reload3');\"";
        $arg["btn_reload3"] = knjCreateBtn(&$objForm, "btn_reload3", "通知票より読込", $extra);
        //指導要録参照画面ボタンを作成する
        if ($model->Properties["sidouyourokuSansyou"] == 1) {
            $extra = "onclick=\"return btn_submit('form7_first');\" style=\"width:90px\"";
        } else {
            $extra = "onclick=\"return btn_submit('form4_first');\" style=\"width:90px\"";
        }
        $arg["btn_form4"] = knjCreateBtn(&$objForm, "btn_form4", "指導要録参照", $extra);
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn(&$objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');
        //取消しボタンを作成する
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = knjCreateBtn(&$objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_back"] = knjCreateBtn(&$objForm, "btn_back", "終 了", $extra);

        //セキュリティーチェック
        $securityCnt = $db->getOne(knje010bQuery::getSecurityHigh());
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //データCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX190/knjx190index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE010B&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check1"] = knjCreateBtn(&$objForm, "btn_check1", "データ".$csvSetName, $extra);
            //ヘッダデータCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX191/knjx191index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE010B&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check2"] = knjCreateBtn(&$objForm, "btn_check2", "ヘッダデータ".$csvSetName, $extra);
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden(&$objForm, "cmd");
        knjCreateHidden(&$objForm, "nextURL", $model->nextURL);
        knjCreateHidden(&$objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden(&$objForm, "mode", $model->mode);
        knjCreateHidden(&$objForm, "GRD_YEAR", $model->grd_year);
        knjCreateHidden(&$objForm, "GRD_SEMESTER", $model->grd_semester);
        knjCreateHidden(&$objForm, "PROGRAMID", PROGRAMID);
        knjCreateHidden(&$objForm, "useSyojikou3", $model->Properties["useSyojikou3"]);
        knjCreateHidden(&$objForm, "LEFT_GRADE", $model->grade);

        if(get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent(0);";
        } elseif($model->cmd =="reset") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010bForm1.html", $arg);
    }
}

function makeHexamEntRemarkDat(&$objForm, &$arg, $db, &$model) {
    $model->schArray = array();
    $disabled = "disabled";
    $query = knje010bQuery::selectQueryAnnual($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->schArray[$row["GRADE"]] = array("YEAR"  => $row["YEAR"],
                                                "ANNUAL" => $row["ANNUAL"]);
    }
    $result->free();

    $opt = array();
    $query = knje010bQuery::getGdat($model);

    $result = $db->query($query);
    while ($gRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("GRADE" => $gRow["GRADE"], "GRADE_CD" => $gRow["GRADE_CD"]);
    }
    $result->free();

    $train_ref_readTrainRef = "";
    $totalremark_readTrainRef = "";
    $train_ref_kaigyo = "";
    $totalremark_kaigyo = "";
    $gradeCnt = 0;
    $hiddenYear = "";
    $yearSep = "";
    foreach ($opt as $key) {
        $grade = (int) $key["GRADE_CD"];
        $disabled = is_array($model->schArray[$key["GRADE"]]) ? "" : " disabled ";
        $year = $model->schArray[$key["GRADE"]]["YEAR"];
        if ($year) {
            $hiddenYear .= $yearSep.$year;
            $yearSep = ",";
        }
        $isReadData = false;
        if (!isset($model->warning)) {
            if ($model->cmd == "reload2" && ($model->readYear == "0000" || $model->readYear == $year)) {
                $query = knje010bQuery::selectQuery_Htrainremark_Dat($model, $year);
                $isReadData = true;
            } else {
                $query = knje010bQuery::selectQueryForm2($model, $year);
            }
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if ($model->cmd == "reload2" && $model->Properties["useSyojikou3"] == "1") {
                $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
                $row["TRAIN_REF2"] = $model->field2[$year]["TRAIN_REF2"];
                $row["TRAIN_REF3"] = $model->field2[$year]["TRAIN_REF3"];
            }

            //指導要録データ、調査書旧データ
            if ($model->Properties["useSyojikou3"] == "1") {
                $query = knje010bQuery::sansyou_data($model, $year);
                $sansyou = $db->getRow($query, DB_FETCHMODE_ASSOC);

                $train_ref_readTrainRef .= $train_ref_kaigyo.$sansyou["TRAIN_REF"];
                $train_ref_kaigyo = $train_ref_readTrainRef ? "\n-------------------------\n" : "";

                $totalremark_readTrainRef .= $totalremark_kaigyo.$sansyou["TOTALREMARK"];
                $totalremark_kaigyo = $totalremark_readTrainRef ? "\n-------------------------\n" : "";
            }

            if ($model->cmd == "reload2" && !$isReadData) {
                $row["ATTENDREC_REMARK"] = $model->field2[$year]["ATTENDREC_REMARK"];
                $row["SPECIALACTREC"] = $model->field2[$year]["SPECIALACTREC"];
            }
        } else {
            $row = $model->field2[$year];
            $row["TRAIN_REF1"] = $model->field2[$year]["TRAIN_REF"];
        }
        //出欠の記録備考
        $extra = $disabled." onPaste=\"return show(this, ".$gradeCnt.");\" ";
        $arg["ATTENDREC_REMARK".$grade] = KnjCreateTextArea(&$objForm, "ATTENDREC_REMARK-".$year, ($model->attendrec_remark_gyou + 1), ($model->attendrec_remark_moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
        $arg["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";
        //出欠備考参照ボタン
        $sdate = ($year) ? $year.'-04-01' : "";
        $edate = ($year) ? ($year+1).'-03-31' : "";
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["SANSYO".$grade] = KnjCreateBtn(&$objForm, "SANSYO".$grade, "まとめ出欠備考参照", $extra);
        } else {
            $extra = $disabled."onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["SANSYO".$grade] = KnjCreateBtn(&$objForm, "SANSYO".$grade, "日々出欠備考参照", $extra);
        }
        //要録の出欠備考参照ボタン
        $extra = $disabled."onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$year}&SCHREGNO={$model->schregno}',0,document.documentElement.scrollTop || document.body.scrollTop,360,180);return;\" style=\"width:210px;\"";
        $arg["YOROKU_SANSYO".$grade] = KnjCreateBtn(&$objForm, "YOROKU_SANSYO".$grade, "要録の出欠の記録備考参照", $extra);
        //委員会参照ボタンを作成する
        $extra = "onclick=\" return btn_submit('subform4');\"";
        $extra = $disabled."onclick=\"loadwindow('knje010bindex.php?cmd=subform4&YEAR={$year}&SCHREGNO={$model->schregno}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        $arg["btn_committee".$grade] = KnjCreateBtn(&$objForm, "btn_committee".$grade, "委員会参照", $extra);

        //特別活動の記録
        $extra = $disabled." onPaste=\"return show(this, ".$gradeCnt.");\" ";
        $arg["SPECIALACTREC".$grade]    = KnjCreateTextArea(&$objForm, "SPECIALACTREC-".$year, ($model->specialactrec_gyou + 1), ($model->specialactrec_moji * 2 + 1), "soft", $extra, $row["SPECIALACTREC"]);
        $arg["SPECIALACTREC_TYUI"] = "(全角{$model->specialactrec_moji}文字{$model->specialactrec_gyou}行まで)";

        //指導上参考となる諸事項]
        if ($model->Properties["useSyojikou3"] == "1") {
            $height1 = $model->train_ref1_gyou * 13.5 + ($model->train_ref1_gyou -1 ) * 3 + 5;
            $height2 = $model->train_ref2_gyou * 13.5 + ($model->train_ref2_gyou -1 ) * 3 + 5;
            $height3 = $model->train_ref3_gyou * 13.5 + ($model->train_ref3_gyou -1 ) * 3 + 5;
            $extra1 = $disabled."style=\"height:{$height1}px;\" onPaste=\"return show(this, ".$gradeCnt.");\" ";
            $extra2 = $disabled."style=\"height:{$height2}px;\" onPaste=\"return show(this, ".$gradeCnt.");\" ";
            $extra3 = $disabled."style=\"height:{$height3}px;\" onPaste=\"return show(this, ".$gradeCnt.");\" ";
            $arg["TRAIN_REF".$grade."_1"] = KnjCreateTextArea(&$objForm, "TRAIN_REF1-".$year, ($model->train_ref1_gyou + 1), ($model->train_ref1_moji * 2 + 1), "soft", $extra1, $row["TRAIN_REF1"]);
            $arg["TRAIN_REF".$grade."_2"] = KnjCreateTextArea(&$objForm, "TRAIN_REF2-".$year, ($model->train_ref2_gyou + 1), ($model->train_ref2_moji * 2 + 1), "soft", $extra2, $row["TRAIN_REF2"]);
            $arg["TRAIN_REF".$grade."_3"] = KnjCreateTextArea(&$objForm, "TRAIN_REF3-".$year, ($model->train_ref3_gyou + 1), ($model->train_ref3_moji * 2 + 1), "soft", $extra3, $row["TRAIN_REF3"]);

            $extra = "style=\"background-color:#D0D0D0;height:60px;\"";
            $arg["TRAIN_REF"] = KnjCreateTextArea(&$objForm, "TRAIN_REF", 5, 83, "soft", $extra, $train_ref_readTrainRef);
            $extra = "style=\"background-color:#D0D0D0;height:60px;\"";
            $arg["TOTALREMARK"] = KnjCreateTextArea(&$objForm, "TOTALREMARK", 5, 83, "soft", $extra, $totalremark_readTrainRef);
            $arg["useSyojikou3"] = $model->Properties["useSyojikou3"];
            $arg["COLSPAN2"] = "colspan=\"3\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"4\"";
            $arg["TRAIN_REF1_COMMENT"] = "(全角{$model->train_ref1_moji}文字{$model->train_ref1_gyou}行まで)";
            $arg["TRAIN_REF2_COMMENT"] = "(全角{$model->train_ref2_moji}文字{$model->train_ref2_gyou}行まで)";
            $arg["TRAIN_REF3_COMMENT"] = "(全角{$model->train_ref3_moji}文字{$model->train_ref3_gyou}行まで)";
        } else {
            $extra = $disabled."style=\"height:105px;\" onPaste=\"return show(this, ".$gradeCnt.");\" ";
            $arg["COLSPAN_TRAIN_REF"] = "colspan=\"2\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"2\"";
            if ($model->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                $arg["TRAIN_REF".$grade."_1"] = KnjCreateTextArea(&$objForm, "TRAIN_REF1-".$year, 7, 117, "soft", $extra, $row["TRAIN_REF"]);
                $arg["TRAIN_REF_COMMENT"] = "(全角58文字X7行まで)";
            } else {
                $arg["TRAIN_REF".$grade."_1"] = KnjCreateTextArea(&$objForm, "TRAIN_REF1-".$year, 5, 83, "soft", $extra, $row["TRAIN_REF"]);
                $arg["TRAIN_REF_COMMENT"] = "(全角41文字X5行まで)";
            }
        }
        $gradeCnt++;
    }
    knjCreateHidden(&$objForm, "hiddenYear", $hiddenYear);
}
?>
