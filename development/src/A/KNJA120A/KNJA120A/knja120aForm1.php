<?php

require_once('for_php7.php');

class knja120aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knja120aindex.php", "", "edit");
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            if ($model->cmd !== 'torikomi3' && $model->cmd !== 'torikomi4' && $model->cmd !== 'reload' && $model->cmd !== 'reload2') {
                $row = knja120aQuery::getTrainRow($model->schregno, $model->exp_year, $model);

                //特別活動（札幌開成）
                if ($model->schregno && !$row["SCHREGNO"] && !$row["SPECIALACTREMARK"] && knja120aQuery::getSapporoHantei() > 0) {
                    $row["SPECIALACTREMARK"] = "ホームルーム役員名（　）\n\n生徒会役員名（　　　　）";
                }
            } else {
                $row =& $model->field;
            }
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }
        //卒業可能な学年か判定
        $getData = knja120aQuery::getGraduationGrade($model);
        $model->GradGrade = "";
        $model->GradGrade = $getData["FLG"];
        $model->schoolKind = "";
        $model->schoolKind = $getData["SCHOOL_KIND"];

        //高校
        $notHDisabled = "";
        if ($model->schregno && $model->schoolKind != "H") {
            $arg["err_alert"] = "alert('更新対象外の生徒です。');";
            $notHDisabled = " disabled ";
        }

        //2018年度以降かどうか
        $model->over2018 = "";
        if ($model->exp_year < 2018) {
            $model->over2018 = "";
        } else if ($model->exp_year < 2020) {
            $isAny = false;
            $db = Query::dbCheckOut();
            $query = knja120AQuery::getTotalRemarkDisable($model);
            $result = $db->query($query);
            while ($disableRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($disableRow["YEAR"] == $model->exp_year
                    && $disableRow["GRADE"] == $model->grade) {
                    $isAny = true;
                    break;
                }
            }
            $result->free();
            Query::dbCheckIn($db);
            //取得した年度のGRADEは使用不可
            if ($isAny) {
                $model->over2018 = "1";
            }
        } else {
            // 2020年度以降は全て使用不可
            $model->over2018 = "1";
        }

        //調査書より読込ボタンを作成する
        if ($model->GradGrade == "1") {
            $arg["chousasho_yomikomi"] = "1";
            $extra = $disabled."onclick=\" return btn_submit('reload');\" style=\"color:#1E90FF;font:bold\"";
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
                if ($model->over2018 != "1") {
                    //総合所見(調査書データが3分割ではない時のみ取込可能)
                    $row["TOTALREMARK"]         = $getRow["TRAIN_REF"];
                }
                //出欠の記録備考
                $row["ATTENDREC_REMARK"]    = $getRow["ATTENDREC_REMARK"];
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
                $query = knja120aQuery::get_record_totalstudytime_dat($model);
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
        } else if ($model->cmd === 'torikomi4') {
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
        $arg["data"]["REMARK1_005"] = KnjCreateTextArea($objForm, "REMARK1_005", 7, 37, "soft", $extra, $row["REMARK1_005"]);
        $arg["data"]["REMARK1_005_TYUI"] = '(全角18文字X6行まで)';

        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            //活動内容
            $height = (int)$model->totalstudyact_gyou * 13.5 + ((int)$model->totalstudyact_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ((int)$model->totalstudyact_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            //評価（明治:Catholic Spirit）
            $height = (int)$model->totalstudyval_gyou * 13.5 + ((int)$model->totalstudyval_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ((int)$model->totalstudyval_moji * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            //出欠の記録備考
            $height = (int)$model->attendrec_remark_gyou * 13.5 + ((int)$model->attendrec_remark_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ((int)$model->attendrec_remark_moji * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);
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
            $height = (int)$model->remark1_003_gyou * 13.5 + ((int)$model->remark1_003_gyou -1 ) * 3 + 5;
            $extra  = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["REMARK1_003"] = KnjCreateTextArea($objForm, "REMARK1_003", $model->remark1_003_gyou, ((int)$model->remark1_003_moji * 2 + 1), "soft", $extra, $row["REMARK1_003"]);
            $arg["data"]["REMARK1_003_TYUI"] = "(全角{$model->remark1_003_moji}文字{$model->remark1_003_gyou}行まで)";

            //評価
            $height = (int)$model->remark2_003_gyou * 13.5 + ((int)$model->remark2_003_gyou -1 ) * 3 + 5;
            $extra  = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["REMARK2_003"] = KnjCreateTextArea($objForm, "REMARK2_003", $model->remark2_003_gyou, ((int)$model->remark2_003_moji * 2 + 1), "soft", $extra, $row["REMARK2_003"]);
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
            $height = (int)$model->specialactremark_gyou * 13.5 + ((int)$model->specialactremark_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", $model->specialactremark_gyou, ((int)$model->specialactremark_moji * 2 + 1), "soft", $extra, $row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = "(全角{$model->specialactremark_moji}文字{$model->specialactremark_gyou}行まで)";
        }

        //2018以降で処理が変わる
        $setReadOnly = "";
        $setStyle = "";
        if ($model->over2018) {
            $arg["OVER_ENT2018"] = 1;
            if ($model->Properties["useSeitoSidoYorokuSougouShoken"] == '1') {
                $readOnly = " readOnly ";
                $setStyle = " background-color:gray; ";
            } else {
                $arg["SHOKEN_ZERO_FLG"] = 1;
            }

            $link = REQUESTROOT."/E/KNJE015/knje015index.php?cmd=edit&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&NAME=".$model->name."&SEND_PRGID=".PROGRAMID."&SEND_AUTH=".$model->auth;
            $extra = "onClick=\"if (!confirm('保存されていないデータは破棄されます。処理を続行しますか？')) {return;}wopen('{$link}','SUBWIN2', 0, 0, window.outerWidth, window.outerHeight);\"";
            $arg["button"]["btn_KNJE015"] = KnjCreateBtn($objForm, "btn_KNJE015", "平成30年度以降入学者 入力プログラムへ", $extra);
        } else {
            $arg["UNDER_ENT2018"] = 1;
        }
        if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            //総合所見
            $extra = $readOnly." style=\"{$setStyle}\"";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 8, 133, "soft", $extra, $row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X8行まで)';
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //総合所見
            $extra = $readOnly."style=\"height:90px;{$setStyle}\"";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 7, 133, "soft", $extra, $row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X7行まで)';
        } else {
            //総合所見
            $extra = $readOnly."style=\"height:90px;{$setStyle}\"";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 6, 89, "soft", $extra, $row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角44文字X6行まで)';
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            //総合所見
            $height = (int)$model->totalremark_gyou * 13.5 + ((int)$model->totalremark_gyou -1 ) * 3 + 5;
            $extra = $readOnly."style=\"height:{$height}px;{$setStyle}\" ";
            $extra .= " onChange=\"setDataChangeFlg()\"";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", $model->totalremark_gyou, ((int)$model->totalremark_moji * 2 + 1), "soft", $extra, $row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = "(全角{$model->totalremark_moji}文字{$model->totalremark_gyou}行まで)";
        }

        //学校判定用
        $getSchoolName = knja120aQuery::getSchoolHantei();

        //出欠の記録参照ボタン
        if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
            $extra = "onclick=\"return btn_submit('syukketsu');\"";
            $arg["btn_syukketsu_sansyo"] = knjCreateBtn($objForm, "btn_syukketsu_sansyo", "出欠の記録参照", $extra);
            $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
        }

        //出欠備考参照ボタン
        $sdate = $model->exp_year.'-04-01';
        $edate = ((int)$model->exp_year+1).'-03-31';
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $extra = "style=\"color:#1E90FF;font:bold;\"";
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = "";
            }
            if (!$model->schregno) {
                $extra .= "onclick=\"alert('データを指定してください。')\"";
            } else {
                //まとめ出欠備考を取込みへ変更する
                if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                    $extra .= $disabled ." onclick=\"return btn_submit('torikomi3');\"";
                } else {
                    $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
                }
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
        } else if ($getSchoolName == "mieken") {
            $extra  = "style=\"color:#1E90FF;font:bold;\"";
            $extra .= $disabled ." onclick=\"return btn_submit('torikomi4');\"";
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "通知票取込", $extra);
        } else {
            if (!$model->schregno) {
                $extra = "onclick=\"alert('データを指定してください。')\"";
            } else {
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }

        //要録の出欠備考参照ボタン
        $extra = "onclick=\"loadwindow('../../X/KNJXATTEND_ENTREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:230px;\"";
        $arg["TYOSASYO_SANSYO"] = knjCreateBtn($objForm, "TYOSASYO_SANSYO", "調査書(進学用)の出欠の記録参照", $extra);

        //成績参照ボタン
        $extra = "onclick=\"return btn_submit('subform4');\" style=\"width:70px\"";
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
                    $extra = "onclick=\"return btn_submit('subform1');\"";
                    $arg["button"]["btn_popup"] = knjCreateBtn($objForm, "btn_popup", "通知表所見参照", $extra);
                }
            }
        }

        if ($cnt > 0) {
            $extra = "onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["button"]["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", $reload2_setname, $extra);
        } else if ($model->GradGrade != "1" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
            $extra = "onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["button"]["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", $reload2_setname, $extra);
        } else if ($model->GradGrade == "1") {
            $extra = "onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["button"]["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", "調査書取込", $extra);
        }

        //部活動選択ボタン（特別活動所見）1:表示
        if ($model->Properties["useKnja120_clubselect_Button"] == "1") {
            $arg["useclubselect"] = 0;
        } else {
            $arg["useclubselect"] = 1;
            $arg["button"]["btn_club_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SPECIALACTREMARK", "");
        }

        //部活動選択ボタン（総合所見）
        $arg["button"]["btn_club_total"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TOTALREMARK", "");

        //委員会選択ボタン
        $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREMARK", "");

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $arg["button"]["btn_club_kirokubikou"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TOTALREMARK", "");
        }

        //検定選択ボタン
        $arg["button"]["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TOTALREMARK", "");

        //賞選択ボタン
        if ($model->Properties["useHyosyoSansyoButton_H"]) {
            $arg["button"]["btn_hyosyo"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TOTALREMARK", "");
        }

        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $notHDisabled.$extra);

        if (!$model->schregno) {
            $extra = "onclick=\"alert('データを指定してください。')\"";
        } else {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&OUTPUT_FIELD=TOTALREMARK&OUTPUT_HEIGHT=75&OUTPUT_WIDTH=600',0,document.documentElement.scrollTop || document.body.scrollTop,800,300);return;\"";
        }
        $arg["button"]["SOUGOU_SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "過年度参照", $extra);

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
            $fieldActSize = "TOTALSTUDYACT=".((int)$model->totalstudyact_moji * 3 * (int)$model->totalstudyact_gyou) .",";
            $gyouActSize  = "TOTALSTUDYACT=$model->totalstudyact_gyou,";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            $fieldValSize = "TOTALSTUDYVAL=".((int)$model->totalstudyval_moji * 3 * (int)$model->totalstudyval_gyou) .",";
            $gyouValSize  = "TOTALSTUDYVAL=$model->totalstudyval_gyou,";
        }
        $fieldSize = $fieldActSize.$fieldValSize;
        $gyouSize = $gyouActSize.$gyouValSize;
        if ($model->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]) {
            $fieldSize .= "REMARK1_003=".((int)$model->remark1_003_moji * 3 * (int)$model->remark1_003_gyou).",";
            $gyouSize  .= "REMARK1_003=$model->remark1_003_gyou,";
            $fieldSize .= "REMARK2_003=".((int)$model->remark2_003_moji * 3 * (int)$model->remark2_003_gyou).",";
            $gyouSize  .= "REMARK2_003=$model->remark2_003_gyou,";
        }
        if ($model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            $fieldSize .= "SPECIALACTREMARK=".((int)$model->specialactremark_moji * 3 * (int)$model->specialactremark_gyou).",";
            $gyouSize  .= "SPECIALACTREMARK=$model->specialactremark_gyou,";
        } else if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            $fieldSize .= "SPECIALACTREMARK=660,";
            $gyouSize  .= "SPECIALACTREMARK=10,";
        } else {
            $fieldSize .= "SPECIALACTREMARK=198,";
            $gyouSize  .= "SPECIALACTREMARK=6,";
        }
        
        if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            $fieldSize .= "TOTALREMARK=".((int)$model->totalremark_moji * 3 * (int)$model->totalremark_gyou).",";
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

        if ($model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            $fieldSize .= "ATTENDREC_REMARK=".((int)$model->attendrec_remark_moji * 3 * (int)$model->attendrec_remark_gyou).",";
            $gyouSize  .= "ATTENDREC_REMARK=$model->attendrec_remark_gyou,";
        } else if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $fieldSize .= "ATTENDREC_REMARK=240,";
            $gyouSize  .= "ATTENDREC_REMARK=2,";
        } else {
            $fieldSize .= "ATTENDREC_REMARK=120,";
            $gyouSize  .= "ATTENDREC_REMARK=2,";
        }

        if($nara > 0){
            $fieldSize .= "REMARK1_005=108,";
            $gyouSize  .= "REMARK1_005=6,";
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

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "updateNextStudent('{$model->schregno}', 0);";
        }elseif($model->cmd =="clear"){
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
function updownPDF(&$objForm, &$arg, $model, $notHDisabled) {
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
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $notHDisabled.$extra);
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
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
?>
