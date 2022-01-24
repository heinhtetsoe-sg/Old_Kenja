<?php

require_once('for_php7.php');
class knja120dForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knja120dindex.php", "", "edit");
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            if ($model->cmd !== 'torikomi3' && $model->cmd !== 'reload' && $model->cmd !== 'reload2' && $model->cmd !== 'value_set') {
                $row = knja120dQuery::getTrainRow($model->schregno, $model->exp_year);
                $model->field["TOTALSTUDYACT_BG_COLOR_FLG"] = "";
                $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"] = "";
                $model->field["SPECIALACTREMARK_BG_COLOR_FLG"] = "";
                $model->field["TRAIN_REF1_BG_COLOR_FLG"] = "";
                $model->field["TRAIN_REF2_BG_COLOR_FLG"] = "";
                $model->field["TRAIN_REF3_BG_COLOR_FLG"] = "";
                $model->field["ATTENDREC_REMARK_BG_COLOR_FLG"] = "";
            } else {
                $row =& $model->field;
            }
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }
        //卒業可能な学年か判定
        $getData = knja120dQuery::getGraduationGrade($model);
        $model->GradGrade = "";
        $model->GradGrade = $getData["FLG"];
        $model->schoolKind = "";
        $model->schoolKind = $getData["SCHOOL_KIND"];

        $disabled = ($model->schregno) ? "" : "disabled";

        //調査書より読込ボタンを作成する
        if ($model->GradGrade == "1") {
            $setColor = "style=\"color:#1E90FF;font:bold\"";
            $arg["chousasho_yomikomi"] = "1";
            $extra = $disabled." onclick=\" return btn_submit('reload');\"".$setColor;
            $arg["btn_reload"] = KnjCreateBtn($objForm, "btn_reload", "調査書より読込", $extra);
            if ($model->cmd === 'reload') {
                $model->field["TOTALSTUDYACT_BG_COLOR_FLG"] = "1";
                $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"] = "1";
                $model->field["SPECIALACTREMARK_BG_COLOR_FLG"] = "1";
                $model->field["TRAIN_REF1_BG_COLOR_FLG"] = "1";
                $model->field["TRAIN_REF2_BG_COLOR_FLG"] = "1";
                $model->field["TRAIN_REF3_BG_COLOR_FLG"] = "1";
                $model->field["ATTENDREC_REMARK_BG_COLOR_FLG"] = "1";
                $getRow = knja120dQuery::getHexamEntremark($model);
                //総合的な学習の時間　活動、評価
                if ($model->Properties["tyousasyoSougouHyoukaNentani"] !== '1') {
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL"];
                } else {
                    //年単位の時
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT_YEAR"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL_YEAR"];
                }
                //特別活動
                $row["SPECIALACTREMARK"]   = $getRow["SPECIALACTREC"];
                //出欠の記録備考
                $row["ATTENDREC_REMARK"]    = $getRow["ATTENDREC_REMARK"];

                //総合所見(6分割から取込)
                $db = Query::dbCheckOut();
                $query = knja120dQuery::getHexamTrainRef($model);
                $result = $db->query($query);
                $totalRemark = "";
                $sep1 = $sep2 = $sep3 = "";
                $totalRemark1 = $totalRemark2 = $totalRemark3 = "";
                while ($getRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($getRow["REMARK"]) {
                        $seq = $getRow["TRAIN_SEQ"];
                        if ($seq == "101" || $seq == "102") {
                            $totalRemark1 .= $sep1 . $getRow["REMARK"];
                            $sep1 = "\n";
                        } else if ($seq == "103" || $seq == "104" || $seq == "105") {
                            $totalRemark2 .= $sep2 . $getRow["REMARK"];
                            $sep2 = "\n";
                        } else {
                            $totalRemark3 .= $sep3 . $getRow["REMARK"];
                            $sep3 = "\n";
                        }
                    }
                    $sep = "\n";
                }
                $row["TRAIN_REF1"]         = $totalRemark1;
                $row["TRAIN_REF2"]         = $totalRemark2;
                $row["TRAIN_REF3"]         = $totalRemark3;
                Query::dbCheckIn($db);
            }
        }

        //通知票　調査書取込
        if ($model->cmd === 'reload2') {
            $model->field["TOTALSTUDYACT_BG_COLOR_FLG"] = "1";
            $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"] = "1";
            //3年次　調査書
            if ($model->GradGrade == "1") {
                $getRow = array();
                $getRow = knja120dQuery::getHexamEntremark($model);
                //総合的な学習の時間　活動、評価
                if ($model->Properties["tyousasyoSougouHyoukaNentani"] !== '1') {
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL"];
                } else {
                    //年単位の時
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT_YEAR"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL_YEAR"];
                }
            //1, 2年次　通知票
            } else {
                $totalstudytimeArray = array();
                $totalstudyactArray  = array();
                $query = knja120dQuery::get_record_totalstudytime_dat($model);
                $db = Query::dbCheckOut();
                $result = $db->query($query);
                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($total_row["TOTALSTUDYTIME"] != '') {
                        $totalstudytimeArray[] = $total_row["TOTALSTUDYTIME"];
                    }
                    if ($total_row["TOTALSTUDYACT"] != '') {
                        $totalstudyactArray[] = $total_row["TOTALSTUDYACT"];
                    }
                }
                if (get_count($totalstudytimeArray) > 0) {
                    $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
                }
                if (get_count($totalstudyactArray) > 0) {
                    $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
                }
                Query::dbCheckIn($db);
            }
        }
        if ($model->cmd === 'reload3') {
            $model->field["TRAIN_REF1_BG_COLOR_FLG"] = "1";
            $model->field["TRAIN_REF2_BG_COLOR_FLG"] = "1";
            $model->field["TRAIN_REF3_BG_COLOR_FLG"] = "1";

            $getRow = array();
            $getRow = knja120dQuery::getHexamEntremark($model);
            //総合所見及び指導上参考となる諸事項
            $row["TRAIN_REF1"] = $getRow["TRAIN_REF1"];
            $row["TRAIN_REF2"] = $getRow["TRAIN_REF2"];
            $row["TRAIN_REF3"] = $getRow["TRAIN_REF3"];
        }

        //明治判定
        $meiji = knja120dQuery::getMeijiHantei();
        if ($meiji > 0) {
             $arg["meiji"] = 1;
        } else {
             $arg["not_meiji"] = 1;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //出欠の記録備考取込
        if ($model->cmd === 'torikomi3') {
            $set_remark = knja120dQuery::getSemesRemark($model);
            $row["ATTENDREC_REMARK"] = $set_remark;
            $model->field["ATTENDREC_REMARK_BG_COLOR_FLG"] = "1";
        }

        if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //活動内容
            if ($model->field["TOTALSTUDYACT_BG_COLOR_FLG"]) {
                $extra = "style=\"background-color:#FFCCFF\"";
            } else {
                $extra = "";
            }
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 8, 45, "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角22文字X8行まで)';

            //評価（明治:Catholic Spirit）
            if ($model->field["TOTALSTUDYVAL_BG_COLOR_FLG"]) {
                $extra = "style=\"height:90px;background-color:#FFCCFF\"";
            } else {
                $extra = "style=\"height:90px;\"";
            }
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, 45, "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角22文字X8行まで)';

            //出欠の記録備考
            if ($model->field["ATTENDREC_REMARK_BG_COLOR_FLG"]) {
                $extra = "style=\"background-color:#FFCCFF\"";
            } else {
                $extra = "";
            }
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 81, "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角40文字X2行まで)';
        } else {
            //活動内容
            if ($model->field["TOTALSTUDYACT_BG_COLOR_FLG"]) {
                $extra = "style=\"background-color:#FFCCFF\"";
            } else {
                $extra = "";
            }
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 5, 23, "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角11文字X4行まで)';

            //評価（明治:Catholic Spirit）
            if ($model->field["TOTALSTUDYVAL_BG_COLOR_FLG"]) {
                $extra = "style=\"height:90px;background-color:#FFCCFF\"";
            } else {
                $extra = "style=\"height:90px;\"";
            }
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", 6, 23, "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角11文字X6行まで)';

            //出欠の記録備考
            if ($model->field["ATTENDREC_REMARK_BG_COLOR_FLG"]) {
                $extra = "style=\"background-color:#FFCCFF\"";
            } else {
                $extra = "";
            }
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 41, "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角20文字X2行まで)';
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            //活動内容
            $height = $model->totalstudyact_gyou * 13.5 + ($model->totalstudyact_gyou -1 ) * 3 + 5;
            if ($model->field["TOTALSTUDYACT_BG_COLOR_FLG"]) {
                $extra = "style=\"height:{$height}px;background-color:#FFCCFF\"";
            } else {
                $extra = "style=\"height:{$height}px;\" ";
            }
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            //評価（明治:Catholic Spirit）
            $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1 ) * 3 + 5;
            if ($model->field["TOTALSTUDYVAL_BG_COLOR_FLG"]) {
                $extra = "style=\"height:{$height}px;background-color:#FFCCFF\"";
            } else {
                $extra = "style=\"height:{$height}px;\" ";
            }
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            //出欠の記録備考
            $height = $model->attendrec_remark_gyou * 13.5 + ($model->attendrec_remark_gyou -1 ) * 3 + 5;
            if ($model->field["ATTENDREC_REMARK_BG_COLOR_FLG"]) {
                $extra = "style=\"height:{$height}px;background-color:#FFCCFF\"";
            } else {
                $extra = "style=\"height:{$height}px;\" ";
            }
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ($model->attendrec_remark_moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";
        }

        if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            //特別活動所見
            if ($model->field["SPECIALACTREMARK_BG_COLOR_FLG"]) {
                $extra = "style=\"background-color:#FFCCFF\"";
            } else {
                $extra = "";
            }
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 45, "soft", $extra, $row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角22文字X10行まで)';
        } else {
            //特別活動所見
            $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1 ) * 3 + 5;
            if ($model->field["SPECIALACTREMARK_BG_COLOR_FLG"]) {
                $extra = "style=\"height:90px;background-color:#FFCCFF\"";
            } else {
                $extra = "style=\"height:90px;\"";
            }
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角11文字X6行まで)';
        }
        if ($model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            //特別活動所見
            $height = $model->specialactremark_gyou * 13.5 + ($model->specialactremark_gyou -1 ) * 3 + 5;
            if ($model->field["SPECIALACTREMARK_BG_COLOR_FLG"]) {
                $extra = "style=\"height:{$height}px;background-color:#FFCCFF\"";
            } else {
                $extra = "style=\"height:{$height}px;\" ";
            }
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", $model->specialactremark_gyou, ($model->specialactremark_moji * 2 + 1), "soft", $extra, $row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = "(全角{$model->specialactremark_moji}文字{$model->specialactremark_gyou}行まで)";
        }

        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK_DETAIL/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&OUTPUT_FIELD=TOTALREMARK&OUTPUT_HEIGHT=75&OUTPUT_WIDTH=320',0,document.documentElement.scrollTop || document.body.scrollTop,800,300);return;\"";
        $arg["button"]["SOUGOU_SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "過年度参照", $extra);

        //6分割
        $prgid = "KNJX_HEXAM_ENTREMARK_TRAINREF_SELECT";
        $extra = $disabled ." onclick=\"loadwindow('../../X/{$prgid}/index.php?GRADE_YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SEND_PRGID={$prgid}&SEND_AUTH={$model->auth}&TRAINREF_TARGET=' + document.forms[0].TRAINREF_TARGET.value, 0, document.documentElement.scrollTop || document.body.scrollTop,550,570);return;\"";
        $arg["button"]["TYOUSASYO_SENTAKU"] = KnjCreateBtn($objForm, "TYOUSASYO_SENTAKU", "調査書選択", $extra);

        $height1 = $model->train_ref1_gyou * 13.5 + ($model->train_ref1_gyou -1 ) * 3 + 5;
        $height2 = $model->train_ref2_gyou * 13.5 + ($model->train_ref2_gyou -1 ) * 3 + 5;
        $height3 = $model->train_ref3_gyou * 13.5 + ($model->train_ref3_gyou -1 ) * 3 + 5;
        if ($model->field["TRAIN_REF1_BG_COLOR_FLG"]) {
            $bgcolor = "background-color:#FFCCFF";
        } else {
            $bgcolor = "";
        }
        $arg["TRAIN_REF1"] = KnjCreateTextArea($objForm, "TRAIN_REF1", ($model->train_ref1_gyou + 1), ($model->train_ref1_moji * 2 + 1), "soft", "style=\"height:{$height1}px;{$bgcolor}\" onMouseUp=\" setTextFieldName('TRAIN_REF1');\"", $row["TRAIN_REF1"]);
        if ($model->field["TRAIN_REF2_BG_COLOR_FLG"]) {
            $bgcolor = "background-color:#FFCCFF";
        } else {
            $bgcolor = "";
        }
        $arg["TRAIN_REF2"] = KnjCreateTextArea($objForm, "TRAIN_REF2", ($model->train_ref2_gyou + 1), ($model->train_ref2_moji * 2 + 1), "soft", "style=\"height:{$height2}px;{$bgcolor}\" onMouseUp=\" setTextFieldName('TRAIN_REF2');\"", $row["TRAIN_REF2"]);
        if ($model->field["TRAIN_REF3_BG_COLOR_FLG"]) {
            $bgcolor = "background-color:#FFCCFF";
        } else {
            $bgcolor = "";
        }

        //特記事項なしチェックボックス
        $extra = " id=\"INS_COMMENTS\" onclick=\"return insertComment(this, 'TRAIN_REF3', 'INS_COMMENTS_LABEL');\"";
        $arg["INS_COMMENTS"] = knjCreateCheckBox($objForm, "INS_COMMENTS", "1", $extra, "");
        //特記事項なし
        $ins_comments_label = '特記事項なし';
        knjCreateHidden($objForm, "INS_COMMENTS_LABEL", $ins_comments_label);
        $arg["INS_COMMENTS_LABEL"] = $ins_comments_label;

        $arg["TRAIN_REF3"] = KnjCreateTextArea($objForm, "TRAIN_REF3", ($model->train_ref3_gyou + 1), ($model->train_ref3_moji * 2 + 1), "soft", "style=\"height:{$height3}px;{$bgcolor}\" onMouseUp=\" setTextFieldName('TRAIN_REF3');\"", $row["TRAIN_REF3"]);
        $arg["TRAIN_REF"]  = KnjCreateTextArea($objForm, "TRAIN_REF", 5, 83, "soft", "style=\"background-color:#D0D0D0;height:60px;\"", $row["TRAIN_REF"]);
        $arg["COLSPAN2"] = "colspan=\"2\"";
        $arg["COLSPAN_CHANGE"] = "colspan=\"3\"";
        $arg["TRAIN_REF1_COMMENT"] = "(全角{$model->train_ref1_moji}文字{$model->train_ref1_gyou}行まで)";
        $arg["TRAIN_REF2_COMMENT"] = "(全角{$model->train_ref2_moji}文字{$model->train_ref2_gyou}行まで)";
        $arg["TRAIN_REF3_COMMENT"] = "(全角{$model->train_ref3_moji}文字{$model->train_ref3_gyou}行まで)";

        //定型文選択ボタンを作成する
        if ($model->Properties["Teikei_Button_Hyouji"] == "1") {
            $extra = "onclick=\"return btn_submit('teikei_act');\"";
            $arg["button"]["btn_teikei_act"] = knjCreateBtn($objForm, "btn_teikei_act", "定型文選択", $extra);
            
            $extra = "onclick=\"return btn_submit('teikei_val');\"";
            $arg["button"]["btn_teikei_val"] = knjCreateBtn($objForm, "btn_teikei_val", "定型文選択", $extra);
        }

        //1,2年次：通知票取込、3年次:調査書取込ボタンを作成する（プロパティにてボタン表示非表示の切り替え）
        if ($model->GradGrade != "1" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
            $extra = $disabled ." onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", "通知票取込", $extra);
        } else if ($model->GradGrade == "1") {
            $extra = $disabled ." onclick=\"return btn_submit('reload2');\"".$setColor;
            $arg["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", "調査書取込", $extra);
        }

        //出欠の記録参照ボタン
        if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
            $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);
            $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
        }

        //出欠備考参照ボタン
        $sdate = $model->exp_year.'-04-01';
        $edate = ((int)$model->exp_year+1).'-03-31';
        //和暦表示フラグ
        $warekiFlg = "";
        if ($model->Properties["useWarekiHyoji"] == "1") {
            $warekiFlg = "1";
        }
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $extra = "style=\"color:#1E90FF;font:bold;\"";
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = "";
            }
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $extra .= $disabled ." onclick=\"return btn_submit('torikomi3');\"";
            } else {
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
        } else {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }

        //要録の出欠備考参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "TYOSASYO_SANSYO",
                            "value"     => "調査書(進学用)の出欠の記録参照",
                            "extrahtml" => $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_ENTREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:230px;\""));
        $arg["TYOSASYO_SANSYO"] = $objForm->ge("TYOSASYO_SANSYO");

        //年間出欠備考選択ボタン
        if ($model->Properties["useReasonCollectionBtn"] == 1) {
            $arg["btn_reason_collection_select"] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select", "年間出欠備考選択", "ATTENDREC_REMARK", $disabled);
            $arg["REASON_COLLECTION_SELECT"] = 1;
        }

        //成績参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "SEISEKI_SANSYO",
                            "value"     => "成績参照",
                            "extrahtml" => $disabled." onclick=\"return btn_submit('subform4');\" style=\"width:70px\"" ) );

        $arg["SEISEKI_SANSYO"] = $objForm->ge("SEISEKI_SANSYO");

        $arg["IFRAME"] = VIEW::setIframeJs();
        //ボタン
        //通知票所見参照ボタンを非表示とする。(近大、海上学園のみ)
        $cnt = knja120dQuery::getKindaiJudgment($model);
        $db = Query::dbCheckOut();
        $schoolName = $db->getOne(knja120dQuery::getSchoolName("NAME1"));
        Query::dbCheckIn($db);
        if ($cnt > 0 || $schoolName === 'kaijyo') {
        } else {
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_TUUCHISYOKEN_SELECT/knjx_tuuchisyoken_selectindex.php";
            $extra .= "?PROGRAMID=".PROGRAMID."&SEND_PRGID=".PROGRAMID."";
            $extra .= "&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}";
            $extra .= "&SCHREGNO={$model->schregno}&NAME={$model->name}'";
            $extra .= ",0,document.documentElement.scrollTop || document.body.scrollTop,900,350);\"";
            $arg["button"]["btn_popup"] = knjCreateBtn($objForm, "btn_popup", "通知表所見参照", $extra);

        }

        //行動の記録参照ボタン
        $extra = $disabled." onclick=\"return btn_submit('act_doc');\"";
        $arg["button"]["btn_actdoc"] = knjCreateBtn($objForm, "btn_actdoc", "行動の記録参照", $extra);

        //委員会選択ボタン
        $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREMARK", $disabled);

        //部活動選択ボタン（特別活動所見）1:表示
        if ($model->Properties["useKnja120_clubselect_Button"] == "1") {
            $arg["useclubselect"] = 1;
            $arg["button"]["btn_club_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SPECIALACTREMARK", $disabled);
        } else {
            $arg["useclubselect"] = 0;
        }

        //部活動選択ボタン（総合所見及び指導上参考となる諸事項）
        $arg["button"]["btn_club_tra2"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TRAIN_REF2", $disabled);

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $arg["button"]["btn_club_kirokubikou"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TRAIN_REF2", $disabled);
        }

        //検定選択ボタン
        $arg["button"]["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TRAIN_REF2", $disabled);

        //賞選択ボタン
        if ($model->Properties["useHyosyoSansyoButton_H"]) {
            $arg["button"]["btn_hyosyo"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TRAIN_REF2", $disabled);
        }

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //PDF取込
        if ($model->Properties["useUpdownPDF"] === '1') {
            $arg["useUpdownPDF"] = '1';
            updownPDF($objForm, $arg, $model);
        }

        //CSV処理
        if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $fieldActSize = "TOTALSTUDYACT=528,";
            $fieldValSize = "TOTALSTUDYVAL=528,";
            $gyouActSize = "TOTALSTUDYACT=8,";
            $gyouValSize = "TOTALSTUDYVAL=8,";
        } else {   
            $fieldActSize = "TOTALSTUDYACT=132,";
            $fieldValSize = "TOTALSTUDYVAL=198,";
            $gyouActSize  = "TOTALSTUDYACT=4,";
            $gyouValSize  = "TOTALSTUDYVAL=6,";
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            $fieldActSize = "TOTALSTUDYACT=".($model->totalstudyact_moji * 3 * $model->totalstudyact_gyou) .",";
            $gyouActSize  = "TOTALSTUDYACT=$model->totalstudyact_gyou,";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            $fieldValSize = "TOTALSTUDYVAL=".($model->totalstudyval_moji * 3 * $model->totalstudyval_gyou) .",";
            $gyouValSize  = "TOTALSTUDYVAL=$model->totalstudyval_gyou,";
        }
        $fieldSize = $fieldActSize.$fieldValSize;
        $gyouSize = $gyouActSize.$gyouValSize;
        if ($model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            $fieldSize .= "SPECIALACTREMARK=".($model->specialactremark_moji * 3 * $model->specialactremark_gyou).",";
            $gyouSize  .= "SPECIALACTREMARK=$model->specialactremark_gyou,";
        } else if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            $fieldSize .= "SPECIALACTREMARK=660,";
            $gyouSize  .= "SPECIALACTREMARK=10,";
        } else {
            $fieldSize .= "SPECIALACTREMARK=198,";
            $gyouSize  .= "SPECIALACTREMARK=6,";
        }
        
        if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            $fieldSize .= "TOTALREMARK=".($model->totalremark_moji * 3 * $model->totalremark_gyou).",";
            $gyouSize  .= "TOTALREMARK=$model->totalremark_gyo,";
        } else if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            $fieldSize .= "TOTALREMARK=1584,";
            $gyouSize  .= "TOTALREMARK=8,";
        } else if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $fieldSize .= "TOTALREMARK=1386,";
            $gyouSize  .= "TOTALREMARK=7,";
        } else {
            $fieldSize .= "TOTALREMARK=792,";
            $gyouSize  .= "TOTALREMARK=6,";
        }

        $fieldSize .= "TRAIN_REF1=".($model->train_ref1_moji * 3 * $model->train_ref1_gyou).",";
        $gyouSize  .= "TRAIN_REF1={$model->train_ref1_gyou},";
        $fieldSize .= "TRAIN_REF2=".($model->train_ref2_moji * 3 * $model->train_ref2_gyou).",";
        $gyouSize  .= "TRAIN_REF2={$model->train_ref2_gyou},";
        $fieldSize .= "TRAIN_REF3=".($model->train_ref3_moji * 3 * $model->train_ref3_gyou).",";
        $gyouSize  .= "TRAIN_REF3={$model->train_ref3_gyou},";

        if ($model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            $fieldSize .= "ATTENDREC_REMARK=".($model->attendrec_remark_moji * 3 * $model->attendrec_remark_gyou).",";
            $gyouSize  .= "ATTENDREC_REMARK=$model->attendrec_remark_gyou,";
        } else if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $fieldSize .= "ATTENDREC_REMARK=240,";
            $gyouSize  .= "ATTENDREC_REMARK=2,";
        } else {
            $fieldSize .= "ATTENDREC_REMARK=120,";
            $gyouSize  .= "ATTENDREC_REMARK=2,";
        }

        $fieldSize .= "VIEWREMARK=0,";
        $gyouSize  .= "VIEWREMARK=0,";
        $fieldSize .= "BEHAVEREC_REMARK=0";
        $gyouSize  .= "BEHAVEREC_REMARK=0,";

        //セキュリティーチェック
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knja120DQuery::getSecurityHigh());
        Query::dbCheckIn($db);
        $csvSetName = "ＣＳＶ";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_check1",
                                "value"     => $csvSetName."出力",
                                "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX180/knjx180index.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&SEND_PRGID=KNJA120D&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));

            $arg["button"]["btn_check1"] = $objForm->ge("btn_check1");
        }

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));

        //hidden
        knjCreateHidden($objForm, "TEXTNAME", "TRAIN_REF2");
        knjCreateHidden($objForm, "TRAINREF_TARGET", "TRAIN_REF1");
        knjCreateHidden($objForm, "TOTALSTUDYACT_BG_COLOR_FLG", $model->field["TOTALSTUDYACT_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "TOTALSTUDYVAL_BG_COLOR_FLG", $model->field["TOTALSTUDYVAL_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "SPECIALACTREMARK_BG_COLOR_FLG", $model->field["SPECIALACTREMARK_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "TRAIN_REF1_BG_COLOR_FLG", $model->field["TRAIN_REF1_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "TRAIN_REF2_BG_COLOR_FLG", $model->field["TRAIN_REF2_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "TRAIN_REF3_BG_COLOR_FLG", $model->field["TRAIN_REF3_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "ATTENDREC_REMARK_BG_COLOR_FLG", $model->field["ATTENDREC_REMARK_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "useQualifiedMst", $model->Properties["useQualifiedMst"]);

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja120dForm1.html", $arg);
    }
}

//PDF取込
function updownPDF(&$objForm, &$arg, $model) {
    //移動後のファイルパス単位
    if ($model->schregno) {
        $dir = "/pdf/" . $model->schregno . "/";
        $dataDir = DOCUMENTROOT . $dir;
        if (!is_dir($dataDir)) {
            //echo "ディレクトリがありません。";
        } else if ($aa = opendir($dataDir)) {
            $cnt = 0;
            while (false !== ($filename = readdir($aa))) {
                $filedir = REQUESTROOT . $dir . $filename;
                $info = pathinfo($filedir);
                //拡張子
                if ($info["extension"] == "pdf" && $cnt < 5) {
                    $setFilename = mb_convert_encoding($filename,"UTF-8", "SJIS-win");
                    $setFiles = array();
                    $setFiles["PDF_FILE_NAME"] = $setFilename;
                    $setFiles["PDF_URL"] = REQUESTROOT . $dir . $setFilename;
                    $arg["down"][] = $setFiles;
                    $cnt++;
                }
            }
            closedir($aa);
        }
    }
    //ファイルからの取り込み
    $arg["up"]["FILE"] = knjCreateFile($objForm, "FILE", "", 10240000);
    //実行
    $extra = ($model->schregno) ? "onclick=\"return btn_submit('execute');\"" : "disabled";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled="") {
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                   //部活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } else if ($div == "committee") {       //委員会
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } else if ($div == "qualified") {       //検定
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"";
        } else if ($div == "hyosyo") {          //賞
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } else if ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } else if ($div == "reason_collection") {   //年間出欠備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } else if ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
?>
