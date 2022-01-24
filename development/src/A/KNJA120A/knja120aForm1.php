<?php

require_once('for_php7.php');
class knja120aForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knja120aindex.php", "", "edit");
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->cmd !== 'torikomi3' && $model->cmd !== 'torikomi4' && $model->cmd !== 'torikomi5' && $model->cmd !== 'reload' && $model->cmd !== 'reload2') {
                $row = knja120aQuery::getTrainRow($model->schregno, $model->exp_year, $model);

                // 総合所見3分割か
                $sogoshoken3bunkatsu = knja120aQuery::isSogoshoken3bunkatsu($model);
                // 総合所見6分割か
                $sogoshoken6bunkatsu = knja120aQuery::isSogoshoken6bunkatsu($model);
                $detailRow = array();
                $trainRefRow = array();
                if ($sogoshoken3bunkatsu) {
                    $detailRow = knja120aQuery::getTrainDetailRow($model->schregno, $model->exp_year);
                } elseif ($sogoshoken6bunkatsu) {
                    $trainRefRow = knja120aQuery::getTrainRefRow($model->schregno, $model->exp_year);
                }

                //特別活動（札幌開成）
                if ($model->schregno && !$row["SCHREGNO"] && !$row["SPECIALACTREMARK"] && knja120aQuery::getSapporoHantei() > 0) {
                    $row["SPECIALACTREMARK"] = "ホームルーム役員名（　）\n\n生徒会役員名（　　　　）";
                }
            } else {
                $row =& $model->field;
                $detailRow = $row;
                $trainRefRow = $row;
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
            $detailRow = $row;
            $trainRefRow = $row;
        }

        //卒業可能な学年か判定
        $getData = knja120aQuery::getGraduationGrade($model);
        $model->GradGrade = "";
        $model->GradGrade = $getData["FLG"];
        $model->schoolKind = "";
        $model->schoolKind = $getData["SCHOOL_KIND"];
        $model->AllGrade = "";
        $model->AllGrade = $getData["FLG2"];

        //高校
        $notHDisabled = "";
        if ($model->schregno && $model->schoolKind != "H") {
            $arg["err_alert"] = "alert('更新対象外の生徒です。');";
            $notHDisabled = " disabled ";
        }
        $disabled = ($model->schregno) ? "" : "disabled";

        //総合的な学習の時間のタイトルの設定(元々の処理はelse側の処理。2021年以降は上の条件の表示となる。2019、2020は過渡期。)
        $db = Query::dbCheckOut();
        $gradeCd = $model->grade == "" ? "" : $db->getOne(knja120AQuery::getGradeCd($model));
        Query::dbCheckIn($db);
        if ($model->exp_year >= 2021
                   || ($model->exp_year == 2019 && $gradeCd == 1)
                   || ($model->exp_year == 2020 && ($gradeCd == 1 || $gradeCd == 2))) {
            $arg["TOP"]["TOTAL_STUDY_TIME_TITLE"] = "総<br>合<br>的<br>な<br>探<br>究<br>の<br>時<br>間<br>の<br>記<br>録<br>";
        } else {
            $arg["TOP"]["TOTAL_STUDY_TIME_TITLE"] = "総<br>合<br>的<br>な<br>学<br>習<br>の<br>時<br>間<br>の<br>記<br>録<br>";
        }

        // 総合所見3分割か
        $sogoshoken3bunkatsu = knja120aQuery::isSogoshoken3bunkatsu($model);
        // 総合所見6分割か
        $sogoshoken6bunkatsu = knja120aQuery::isSogoshoken6bunkatsu($model);

        if ($model->Properties["useKnja120a_TyousasyoSentaku_Button"] === "1") {
            //調査書内容選択ボタン
            if ($model->AllGrade == "1") {
                $arg["chousasho_sentaku"] = "1";
                $extra = "onclick=\"return btn_submit('tyousasyoSelect');\"";
                $arg["btn_tyousasyo_select"] = knjCreateBtn($objForm, "btn_tyousasyo_select", "調査書内容選択", $extra);
            }
        } else {
            //調査書より読込ボタンを作成する
            if ($model->GradGrade == "1") {
                $arg["chousasho_yomikomi"] = "1";
                $extra = $disabled." onclick=\" return btn_submit('reload');\" style=\"color:#1E90FF;font:bold\"";
                $arg["btn_reload"] = KnjCreateBtn($objForm, "btn_reload", "調査書より読込", $extra);
                if ($model->cmd === 'reload') {
                    $getRow = knja120aQuery::getHexamEntremark($model);
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
                    $row["SPECIALACTREMARK"]    = $getRow["SPECIALACTREC"];
                    //出欠の記録備考
                    $row["ATTENDREC_REMARK"]    = $getRow["ATTENDREC_REMARK"];

                    if ($sogoshoken3bunkatsu) {
                        $detailRow["TRAIN_REF1"] = $getRow["TRAIN_REF1"];
                        $detailRow["TRAIN_REF2"] = $getRow["TRAIN_REF2"];
                        $detailRow["TRAIN_REF3"] = $getRow["TRAIN_REF3"];
                    } elseif ($sogoshoken6bunkatsu) {
                        $trainRefRow = knja120aQuery::getHexamTrainRefRow($model->schregno, $model->exp_year);
                    } else {
                        $db = Query::dbCheckOut();
                        //総合所見(6分割取込)
                        $query = knja120aQuery::getHexamTrainRef($model);
                        $result = $db->query($query);
                        $totalRemark = "";
                        $sep = "";
                        while ($getRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            if ($getRow["REMARK"]) {
                                $totalRemark .= $sep . $getRow["REMARK"];
                            }
                            $sep = "\n";
                        }
                        $row["TOTALREMARK"] = $totalRemark;
                        Query::dbCheckIn($db);
                    }
                }
            }
        }

        //読込みボタンが押された時の通知書より読込む
        if ($model->cmd == 'reload2') {
            $cntFlg = knja120aQuery::getKindaiJudgment($model);//近大フラグ

            //3年次　調査書
            if ($cntFlg == "0" && $model->GradGrade == "1") {
                $getRow = knja120aQuery::getHexamEntremark($model);
                //総合的な学習の時間　活動、評価
                if ($model->Properties["tyousasyoSougouHyoukaNentani"] !== '1') {
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL"];
                } else {
                    //年単位の時
                    $row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT_YEAR"];
                    $row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL_YEAR"];
                }
                //1,2年次　通知票
            } else {
                $totalstudytimeArray = array();
                $totalstudyactArray  = array();
                $query = knja120aQuery::getRecordTotalstudytimeDat($model);
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
                $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
                $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
                Query::dbCheckIn($db);
            }
        }

        if ($model->cmd == "torikomi5") {
            if ($sogoshoken3bunkatsu) {
                // 総合所見3分割のみ取込み
                $getRow = knja120aQuery::getHexamEntremark($model);
                $detailRow["TRAIN_REF1"] = $getRow["TRAIN_REF1"];
                $detailRow["TRAIN_REF2"] = $getRow["TRAIN_REF2"];
                $detailRow["TRAIN_REF3"] = $getRow["TRAIN_REF3"];
            } elseif ($sogoshoken6bunkatsu) {
                // 総合所見6分割のみ取込み
                $trainRefRow = knja120aQuery::getHexamTrainRefRow($model->schregno, $model->exp_year);
            }
        }

        //明治判定
        $meiji = knja120aQuery::getMeijiHantei();
        if ($meiji > 0) {
            $arg["meiji"] = 1;
        } else {
            $arg["not_meiji"] = 1;
        }

        //奈良判定
        $nara = knja120aQuery::getNaraHantei();
        if ($nara > 0) {
            $arg["nara"] = 1;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //出欠の記録備考取込
        if ($model->cmd === 'torikomi3') {
            $set_remark = knja120aQuery::getSemesRemark($model);
            $row["ATTENDREC_REMARK"] = $set_remark;
        } elseif ($model->cmd === 'torikomi4') {
            $set_remark = knja120aQuery::getHreportremarkDetailDat($model);
            $row["ATTENDREC_REMARK"] = $set_remark;
        }

        if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //活動内容
            $extra = " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 8, 45, "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角22文字X8行まで)';

            //評価（明治:Catholic Spirit）
            $extra = "style=\"height:90px;\"";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, 45, "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角22文字X8行まで)';

            //出欠の記録備考
            $extra = " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 81, "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角40文字X2行まで)';
        } else {
            //活動内容
            $extra = " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 5, 23, "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角11文字X4行まで)';

            //評価（明治:Catholic Spirit）
            $extra = "style=\"height:90px;\"";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", 6, 23, "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角11文字X6行まで)';

            //出欠の記録備考
            $extra = " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 41, "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角20文字X2行まで)';
        }

        //奈良Time
        $extra = " onChange=\"setDataChangeFlg()\"";
        $arg["data"]["REMARK1_005"] = KnjCreateTextArea($objForm, "REMARK1_005", 11, 50, "soft", $extra, $row["REMARK1_005"]);
        $arg["data"]["REMARK1_005_TYUI"] = '(全角25文字X10行まで)';

        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            //活動内容
            $height = $model->totalstudyact_gyou * 13.5 + ($model->totalstudyact_gyou -1) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            //評価（明治:Catholic Spirit）
            $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            //出欠の記録備考
            $height = $model->attendrec_remark_gyou * 13.5 + ($model->attendrec_remark_gyou -1) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ($model->attendrec_remark_moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";
        }

        if ($model->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]) {
            $arg["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"] = 1;

            //項目名
            $itemName = knja120aQuery::getItemName($model);
            $itemName = ($itemName == "") ? 'キャリアプラン' : $itemName;

            $set_itemName = $sep = "";
            for ($i = 0; $i < mb_strlen($itemName); $i++) {
                $set_itemName .= $sep.mb_substr($itemName, $i, 1);
                $sep = "<br>";
            }
            $arg["data"]["ITEM_NAME"] = $set_itemName;

            //活動内容
            $height = $model->remark1_003_gyou * 13.5 + ($model->remark1_003_gyou -1) * 3 + 5;
            $extra  = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["REMARK1_003"] = KnjCreateTextArea($objForm, "REMARK1_003", $model->remark1_003_gyou, ($model->remark1_003_moji * 2 + 1), "soft", $extra, $row["REMARK1_003"]);
            $arg["data"]["REMARK1_003_TYUI"] = "(全角{$model->remark1_003_moji}文字{$model->remark1_003_gyou}行まで)";

            //評価
            $height = $model->remark2_003_gyou * 13.5 + ($model->remark2_003_gyou -1) * 3 + 5;
            $extra  = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["REMARK2_003"] = KnjCreateTextArea($objForm, "REMARK2_003", $model->remark2_003_gyou, ($model->remark2_003_moji * 2 + 1), "soft", $extra, $row["REMARK2_003"]);
            $arg["data"]["REMARK2_003_TYUI"] = "(全角{$model->remark2_003_moji}文字{$model->remark2_003_gyou}行まで)";
        }

        if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            //特別活動所見
            $extra = "";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 45, "soft", $extra, $row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角22文字X10行まで)';
        } else {
            //特別活動所見
            $extra = "style=\"height:90px;\"";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角11文字X6行まで)';
        }
        if ($model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            //特別活動所見
            $height = $model->specialactremark_gyou * 13.5 + ($model->specialactremark_gyou -1) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", $model->specialactremark_gyou, ($model->specialactremark_moji * 2 + 1), "soft", $extra, $row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = "(全角{$model->specialactremark_moji}文字{$model->specialactremark_gyou}行まで)";
        }

        //定型文選択(総合所見) ※ボタン2つセットVer
        if ($model->Properties["SpecialAct_HTRAINREMARK_TEMP_DAT"] == "1") {
            $arg["TEIKEI_FLG1"] = "1";
            createTeikeiBtn($arg, $objForm, $model, "14-15", "特別活動所見", "SPECIALACTREMARK");
        }

        if ($sogoshoken3bunkatsu) {
            $arg["show_3bunkatsu"] = "1";
            for ($n = 1; $n <= 3; $n++) {
                $field = "TRAIN_REF".$n;
                $moji = $model->moji[$field];
                $gyo = $model->gyo[$field];
                $height = $gyo * 13.5 + ($gyo -1 ) * 3 + 5;
                $extra = "style=\"height:{$height}px;\" ";
                $extra .= " onChange=\"setDataChangeFlg()\"";
                $arg["data"][$field] = KnjCreateTextArea($objForm, $field, ($gyo + 1), ($moji * 2 + 1), "soft", $extra, $detailRow[$field]);
                $arg["data"][$field."_COMMENT"] = "(全角{$moji}文字{$gyo}行まで)";
            }
        } elseif ($sogoshoken6bunkatsu) {
            $arg["show_6bunkatsu"] = "1";
            //指導上参考となる諸事項
            $gyoMax = 0;
            for ($i = 1; $i <= 6; $i++) {
                $gyo = $model->gyo["TRAIN_REF".$i];
                if ($gyoMax < $gyo) {
                    $gyoMax = $gyo;
                }
            }
            $innerScroll = "";
            if ($gyoMax < 15) {
                $arg["SHOJIKOU_HEIGHT"] = " height: ".($gyoMax + 1)."em; overflow-y: hidden; ";
                $innerScroll = "scroll";
            } else {
                $arg["SHOJIKOU_HEIGHT"] = " height: 15em; overflow-y: scroll; ";
                $innerScroll = "hidden";
            }

            for ($n = 1; $n <= 6; $n++) {
                $field = "TRAIN_REF".$n;
                $moji = $model->moji[$field];
                $gyo = $model->gyo[$field];
                $height = $gyo * 13.5 + ($gyo -1 ) * 3 + 5;
                $extra = "style=\"height:{$height}px;\" ";
                $extra .= " onChange=\"setDataChangeFlg()\"";
                $arg["data"][$field."_ITEMNAME"] = $model->itemname[$field];
                $arg["data"][$field] = KnjCreateTextArea($objForm, $field, ($gyo + 1), ($moji * 2 + 1), "soft", $extra, $trainRefRow[$field]);
                $arg["data"][$field."_COMMENT"] = "(全角{$moji}文字{$gyo}行まで)";
            }
        } else {
            $arg["show_TOTALREMARK"] = "1";
            $readOnly = "";
            $setStyle = "";
            //総合所見
            if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
                $moji = 66;
                $gyo = 8;
                $extra = $readOnly." style=\"{$setStyle}\"";
            } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $moji = 66;
                $gyo = 7;
                $extra = $readOnly." style=\"height:90px;{$setStyle}\"";
            } else {
                $moji = 44;
                $gyo = 6;
                $extra = $readOnly." style=\"height:90px;{$setStyle}\"";
            }

            if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
                //総合所見
                $moji = $model->totalremark_moji;
                $gyo = $model->totalremark_gyou;
                $height = $gyo * 13.5 + ($gyo -1 ) * 3 + 5;
                $extra = $readOnly." style=\"height:{$height}px;{$setStyle}\" ";
            }
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", $gyo, ($moji * 2 + 1), "soft", $extra, $row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = "(全角{$moji}文字{$gyo}行まで)";
        }

        //帝八判定
        $teihachi = knja120aQuery::getTeihachiHantei();
        if ($teihachi > 0) {
            //中学で履修済み備考
            $extra = " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["REMARK1_002"] = KnjCreateTextArea($objForm, "REMARK1_002", 5, 118, "soft", $extra, $row["REMARK1_002"]);
            $arg["data"]["REMARK1_002_TYUI"] = '(全角59文字X5行まで)';
        }

        //出欠の記録参照ボタン
        if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
            $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);
            $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
        }

        //出欠備考参照ボタン
        $sdate = $model->exp_year.'-04-01';
        $edate = ($model->exp_year+1).'-03-31';
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
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
        } elseif ($getSchoolName == "mieken") {
            $extra  = "style=\"color:#1E90FF;font:bold;\"";
            $extra .= $disabled ." onclick=\"return btn_submit('torikomi4');\"";
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "通知票取込", $extra);
        } else {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }

        //要録の出欠備考参照ボタン
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_ENTREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:230px;\"";
        $arg["TYOSASYO_SANSYO"] = knjCreateBtn($objForm, "TYOSASYO_SANSYO", "調査書(進学用)の出欠の記録参照", $extra);

        //年間出欠備考選択ボタン
        if ($model->Properties["useReasonCollectionBtn"] == 1) {
            $arg["btn_reason_collection_select"] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select", "年間出欠備考選択", "ATTENDREC_REMARK", $disabled);
            $arg["REASON_COLLECTION_SELECT"] = 1;
        }

        //成績参照ボタン
        $extra = $disabled." onclick=\"return btn_submit('subform4');\" style=\"width:70px\"";
        $arg["SEISEKI_SANSYO"] = knjCreateBtn($objForm, "SEISEKI_SANSYO", "成績参照", $extra);

        $arg["IFRAME"] = VIEW::setIframeJs();
        //ボタン
        //通知票所見参照ボタンを非表示とする。(近大・札幌開成、京都西山)
        $reload2_setname = "";
        $cnt = knja120aQuery::getKindaiJudgment($model);
        if ($cnt > 0) {
            $reload2_setname = "総合的な学習の時間より読込";
        } else {
            $reload2_setname = "通知票取込";
            if ($getSchoolName != 'sapporo' && $getSchoolName != 'nishiyama') {
                if ($model->Properties["unUseSyokenSansyoButton_H"] != '1') {
                    $extra = $disabled." onclick=\"return btn_submit('subform1');\"";
                    $arg["button"]["btn_popup"] = knjCreateBtn($objForm, "btn_popup", "通知表所見参照", $extra);
                }
            }
        }

        if ($cnt > 0) {
            $extra = $disabled." onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["button"]["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", $reload2_setname, $extra);
        } elseif ($model->GradGrade != "1" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
            $extra = $disabled." onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["button"]["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", $reload2_setname, $extra);
        } elseif ($model->GradGrade == "1") {
            $extra = $disabled." onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["button"]["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", "調査書取込", $extra);
        }

        //部活動選択ボタン（特別活動所見）1:表示
        if ($model->Properties["useKnja120_clubselect_Button"] == "1") {
            $arg["useclubselect"] = 0;
        } else {
            $arg["useclubselect"] = 1;
            $arg["button"]["btn_club_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SPECIALACTREMARK", $disabled);
        }

        //部活動選択ボタン（総合所見）
        if ($sogoshoken6bunkatsu) {
            $arg["button"]["btn_club_tra3"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TRAIN_REF3", $disabled);
        } else {
            $torikomiField2 = $sogoshoken3bunkatsu ? "TRAIN_REF2" : "TOTALREMARK";
            $arg["button"]["btn_club_total"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", $torikomiField2, $disabled);
        }

        //委員会選択ボタン
        $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREMARK", $disabled);

        //駒沢
        if ($getSchoolName == "koma") {
            $arg["isKoma"] = "1";
            //マラソン大会
            $arg["btn_marathon"] = makeSelectBtn($objForm, $model, "marathon", "btn_marathon", "マラソン大会選択", "SPECIALACTREMARK", $disabled);
            //臘八摂心皆勤
            $rouhatsuKaikin = "";
            $db = Query::dbCheckOut();
            $query = knja120aQuery::getRouhatsuKaikin($model);
            $rouhatsuRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if ($rouhatsuRow["REC_CNT"] > 0 && $rouhatsuRow["REC_CNT"] == $rouhatsuRow["KAIKIN_CNT"]) {
                $rouhatsuKaikin = "臘八摂心皆勤";
            }
            knjCreateHidden($objForm, "ROUHATSU_KAIKIN", $rouhatsuKaikin);
            $extra = $disabled." onclick=\"document.forms[0].SPECIALACTREMARK.value += document.forms[0].ROUHATSU_KAIKIN.value\"";
            $arg["btn_rouhatsu"] = knjCreateBtn($objForm, "btn_rouhatsu", "臘八摂心皆勤", $extra);
        }

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            if ($sogoshoken6bunkatsu) {
                $arg["button"]["btn_club_kirokubikou_tra3"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TRAIN_REF3", $disabled);
                $arg["button"]["btn_club_kirokubikou_tra5"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TRAIN_REF5", $disabled);
            } else {
                $torikomiField2 = $sogoshoken3bunkatsu ? "TRAIN_REF2" : "TOTALREMARK";
                $arg["button"]["btn_club_kirokubikou"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", $torikomiField2, $disabled);
            }
        }

        //検定選択ボタン
        if ($sogoshoken6bunkatsu) {
            $arg["button"]["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TRAIN_REF4", $disabled);
        } else {
            $torikomiField2 = $sogoshoken3bunkatsu ? "TRAIN_REF2" : "TOTALREMARK";
            $arg["button"]["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", $torikomiField2, $disabled);
        }

        if ($model->Properties["useHyosyoSansyoButton_H"]) {
            //賞選択ボタン
            if ($sogoshoken6bunkatsu) {
                $arg["button"]["btn_hyosyo_tr5"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TRAIN_REF5", $disabled);
            } else {
                $torikomiField2 = $sogoshoken3bunkatsu ? "TRAIN_REF2" : "TOTALREMARK";
                $arg["button"]["btn_hyosyo"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", $torikomiField2, $disabled);
                //罰選択ボタン
                $arg["button"]["btn_batsu"] = makeSelectBtn($objForm, $model, "batsu", "btn_batsu", "罰選択", $torikomiField2, $disabled);
            }
        }

        //定型文選択(総合所見)
        //定型文選択(総合所見) ※ボタン2つセットVer
        if ($model->Properties["TotalRemark_HTRAINREMARK_TEMP_DAT"] == "1") {
            $arg["TEIKEI_FLG2"] = "1";
            createTeikeiBtn($arg, $objForm, $model, "12-13", "総合所見", "TOTALREMARK");
        } elseif ($model->Properties["seitoSidoYorokuSougou_Teikei_Button_Hyouji"] == "1") {
            $extra = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
            $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}&DATA_DIV=12&TITLE=総合所見&TEXTBOX=TOTALREMARK'";
            $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
            $arg["button"]["btn_teikei_totalremark"] = knjCreateBtn($objForm, "btn_teikei_totalremark", "定型文選択", $extra);
        }

        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $notHDisabled.$extra);

        if ($sogoshoken3bunkatsu || $sogoshoken6bunkatsu) {
            $extra = $disabled." onclick=\" return btn_submit('torikomi5');\" style=\"color:#1E90FF;font:bold\"";
            $arg["button"]["btn_torikomi5"] = KnjCreateBtn($objForm, "btn_torikomi5", "調査書取込", $extra);
        } else {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&OUTPUT_FIELD=TOTALREMARK&OUTPUT_HEIGHT=75&OUTPUT_WIDTH=600',0,document.documentElement.scrollTop || document.body.scrollTop,800,300);return;\"";

            $arg["button"]["SOUGOU_SANSYO"] = KnjCreateBtn($objForm, "SOUGOU_SANSYO", "過年度参照", $extra);

            $prgid = "KNJX_HEXAM_ENTREMARK_TRAINREF_SELECT";
            $extra = $disabled ." onclick=\"loadwindow('../../X/{$prgid}/index.php?GRADE_YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SEND_PRGID={$prgid}&SEND_AUTH={$model->auth}&TRAINREF_TARGET=TOTALREMARK',0,document.documentElement.scrollTop || document.body.scrollTop,550,570);return;\"";

            $arg["button"]["TYOUSASYO_SENTAKU"] = KnjCreateBtn($objForm, "TYOUSASYO_SENTAKU", "調査書選択", $extra);
        }

        //定型文選択ボタンを作成する
        if ($model->Properties["Teikei_Button_Hyouji"] == "1") {
            $extra = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
            $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}&DATA_DIV=01&TITLE=総合的な学習の時間の記録（活動内容）&TEXTBOX=TOTALSTUDYACT'";
            $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
            $arg["button"]["btn_teikei_act"] = knjCreateBtn($objForm, "btn_teikei_act", "定型文選択", $extra);

            $extra = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
            $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}&DATA_DIV=02&TITLE=総合的な学習の時間の記録（評価）&TEXTBOX=TOTALSTUDYVAL'";
            $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
            $arg["button"]["btn_teikei_val"] = knjCreateBtn($objForm, "btn_teikei_val", "定型文選択", $extra);
        }

        //更新後前の生徒へボタン
        $setUpNext = View::updateNext($model, $objForm, 'btn_update');
        $arg["button"]["btn_up_next"] = str_replace("onclick", $notHDisabled."onclick", $setUpNext);

        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $notHDisabled.$extra, "reset");

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //PDF取込
        if ($model->Properties["useUpdownPDF"] === '1') {
            $arg["useUpdownPDF"] = '1';
            updownPDF($objForm, $arg, $model, $notHDisabled);
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
        if ($model->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]) {
            $fieldSize .= "REMARK1_003=".($model->remark1_003_moji * 3 * $model->remark1_003_gyou).",";
            $gyouSize  .= "REMARK1_003=$model->remark1_003_gyou,";
            $fieldSize .= "REMARK2_003=".($model->remark2_003_moji * 3 * $model->remark2_003_gyou).",";
            $gyouSize  .= "REMARK2_003=$model->remark2_003_gyou,";
        }
        if ($model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            $fieldSize .= "SPECIALACTREMARK=".($model->specialactremark_moji * 3 * $model->specialactremark_gyou).",";
            $gyouSize  .= "SPECIALACTREMARK=$model->specialactremark_gyou,";
        } elseif ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            $fieldSize .= "SPECIALACTREMARK=660,";
            $gyouSize  .= "SPECIALACTREMARK=10,";
        } else {
            $fieldSize .= "SPECIALACTREMARK=198,";
            $gyouSize  .= "SPECIALACTREMARK=6,";
        }

        if (!$sogoshoken6bunkatsu) {
            if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
                $fieldSize .= "TOTALREMARK=".($model->totalremark_moji * 3 * $model->totalremark_gyou).",";
                $gyouSize  .= "TOTALREMARK=$model->totalremark_gyo,";
            } elseif ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
                $fieldSize .= "TOTALREMARK=1584,";
                $gyouSize  .= "TOTALREMARK=8,";
            } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $fieldSize .= "TOTALREMARK=1386,";
                $gyouSize  .= "TOTALREMARK=7,";
            } else {
                $fieldSize .= "TOTALREMARK=792,";
                $gyouSize  .= "TOTALREMARK=6,";
            }
        }

        if ($sogoshoken3bunkatsu) {
            for ($n = 1; $n <= 3; $n++) {
                $fieldSize .= "TRAIN_REF".$n."=".($model->moji["TRAIN_REF".$n] * 3 * $model->gyo["TRAIN_REF".$n]).",";
                $gyouSize  .= "TRAIN_REF".$n."={$model->gyo["TRAIN_REF".$n]},";
            }
        } elseif ($sogoshoken6bunkatsu) {
            for ($n = 1; $n <= 6; $n++) {
                $fieldSize .= "TRAIN_REF".$n."=".($model->moji["TRAIN_REF".$n] * 3 * $model->gyo["TRAIN_REF".$n]).",";
                $gyouSize  .= "TRAIN_REF".$n."={$model->gyo["TRAIN_REF".$n]},";
            }
        }

        if ($model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            $fieldSize .= "ATTENDREC_REMARK=".($model->attendrec_remark_moji * 3 * $model->attendrec_remark_gyou).",";
            $gyouSize  .= "ATTENDREC_REMARK=$model->attendrec_remark_gyou,";
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $fieldSize .= "ATTENDREC_REMARK=240,";
            $gyouSize  .= "ATTENDREC_REMARK=2,";
        } else {
            $fieldSize .= "ATTENDREC_REMARK=120,";
            $gyouSize  .= "ATTENDREC_REMARK=2,";
        }

        if ($nara > 0) {
            $fieldSize .= "REMARK1_005=750,";
            $gyouSize  .= "REMARK1_005=10,";
        }

        $fieldSize .= "VIEWREMARK=0,";
        $gyouSize  .= "VIEWREMARK=0,";
        $fieldSize .= "BEHAVEREC_REMARK=0";
        $gyouSize  .= "BEHAVEREC_REMARK=0,";

        //セキュリティーチェック
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knja120AQuery::getSecurityHigh());
        Query::dbCheckIn($db);
        $csvSetName = "ＣＳＶ";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX180/knjx180index.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&SEND_PRGID=KNJA120A&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check1"] = knjCreateBtn($objForm, "btn_check1", $csvSetName."出力", $notHDisabled.$extra);
        }
        //プレビュー／印刷
        if ($model->Properties["sidouyourokuShokenPreview"] == '1') {
            $gradehrclass = knja120aQuery::getGradeHrclass($model);
            $extra =  "onclick=\"return newwin('".SERVLET_URL."', '".$gradehrclass."');\"";
            $arg["button"]["btn_print"] = KnjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $notHDisabled.$extra);
        }
        //hidden
        knjCreateHidden($objForm, "cmd");

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        knjCreateHidden($objForm, "PRGID", "KNJA120A");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "seitoSidoYorokuFieldSize", $model->Properties["seitoSidoYorokuFieldSize"]);
        knjCreateHidden($objForm, "seitoSidoYorokuSougouFieldSize", $model->Properties["seitoSidoYorokuSougouFieldSize"]);
        knjCreateHidden($objForm, "seitoSidoYorokuSpecialactremarkFieldSize", $model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
        knjCreateHidden($objForm, "seitoSidoYorokuZaisekiMae", $model->Properties["seitoSidoYorokuZaisekiMae"]);
        knjCreateHidden($objForm, "seitoSidoYorokuKoumokuMei", $model->Properties["seitoSidoYorokuKoumokuMei"]);
        knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg", $model->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]);
        knjCreateHidden($objForm, "useQualifiedMst", $model->Properties["useQualifiedMst"]);
        knjCreateHidden($objForm, "seitoSidoYorokuFormType", $model->Properties["seitoSidoYorokuFormType"]);
        knjCreateHidden($objForm, "seitoSidouYorokuUseEditKinsokuH", $model->Properties["seitoSidouYorokuUseEditKinsokuH"]);
        knjCreateHidden($objForm, "notPrintFinschooltypeName", $model->Properties["notPrintFinschooltypeName"]);
        knjCreateHidden($objForm, "seitoSidoYorokuFinschoolFinishDateYearOnly", $model->Properties["seitoSidoYorokuFinschoolFinishDateYearOnly"]);
        knjCreateHidden($objForm, "seitoSidoYorokuKinsokuForm", $model->Properties["seitoSidoYorokuKinsokuForm"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_SpecialactremarkSize", $model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalremarkSize", $model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_Attendrec_RemarkSiz", $model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
        knjCreateHidden($objForm, "seitoSidoYorokuTotalStudyCombineHtrainremarkDat", $model->Properties["seitoSidoYorokuTotalStudyCombineHtrainremarkDat"]);
        knjCreateHidden($objForm, "seitoSidoYorokuSougouHyoukaNentani", $model->Properties["seitoSidoYorokuSougouHyoukaNentani"]);
        knjCreateHidden($objForm, "seitoSidoYorokuHoushiNentani", $model->Properties["seitoSidoYorokuHoushiNentani"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning)== 0 && $model->cmd !="clear") {
            $arg["next"] = "updateNextStudent('{$model->schregno}', 0);";
        } elseif ($model->cmd =="clear") {
            $arg["next"] = "updateNextStudent('{$model->schregno}', 1);";
        }
        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja120aForm1.html", $arg);
    }
}

//PDF取込
function updownPDF(&$objForm, &$arg, $model, $notHDisabled)
{
    //移動後のファイルパス単位
    if ($model->schregno) {
        $dir = "/pdf/" . $model->schregno . "/";
        $dataDir = DOCUMENTROOT . $dir;
        if (!is_dir($dataDir)) {
            //echo "ディレクトリがありません。";
        } elseif ($aa = opendir($dataDir)) {
            $cnt = 0;
            while (false !== ($filename = readdir($aa))) {
                $filedir = REQUESTROOT . $dir . $filename;
                $info = pathinfo($filedir);
                //拡張子
                if ($info["extension"] == "pdf" && $cnt < 5) {
                    $setFilename = mb_convert_encoding($filename, "UTF-8", "SJIS-win");
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
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $notHDisabled.$extra);
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                   //部活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "committee") {       //委員会
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } elseif ($div == "qualified") {       //検定
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"";
        } elseif ($div == "hyosyo") {          //賞
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } elseif ($div == "batsu") {          //罰
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_BATSU_SELECT/knjx_batsu_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } elseif ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "reason_collection") {   //年間出欠備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "marathon") {   //マラソン大会選択
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_MARATHON_SELECT/knjx_marathon_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}

//定型ボタン作成
function createTeikeiBtn(&$arg, &$objForm, $model, $property, $title, $textbox)
{
    $sendDataDivArr = explode("-", $property);
    if (get_count($sendDataDivArr) != 2) {
        return;
    }

    for ($i = 0; $i < 2; $i++) {
        $sendDataDiv = $sendDataDivArr[$i];
        $bangou = $i + 1;

        $extra  = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
        $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}&DATA_DIV={$sendDataDiv}&TITLE={$title}{$bangou}&TEXTBOX={$textbox}'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
        $arg["button"]["btn_teikei".$bangou."_".$textbox] = knjCreateBtn($objForm, "btn_teikei".$bangou, "定型文選択".$bangou, $extra);
    }
}
