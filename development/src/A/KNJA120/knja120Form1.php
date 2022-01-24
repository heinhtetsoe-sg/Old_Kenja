<?php

require_once('for_php7.php');
class knja120Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knja120index.php", "", "edit");

        $arg["fep"] = $model->Properties["FEP"];

        //東京仕様かを確認
        $model->getname = "";
        $model->getname = knja120Query::getNamecd("Z010", "00");
        if ($model->getname === 'tokyoto') {
            $arg["tokyoto"] = '1';
        }
        //警告メッセージを表示しない場合
        if (((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) && $model->cmd != 'reload'){
            if ($model->cmd !== 'torikomi3' && $model->cmd !== 'daigae' && $model->cmd !== 'chousasho') {
                $row = knja120Query::getTrainRow($model->schregno, $model);
                $row2 = knja120Query::getTrainHRow($model->schregno, $model);
            } else {
                $row =& $model->field;
                $row2 =& $model->field;
            }
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
            $row2 =& $model->field;
        }

        $disabled = ($model->schregno) ? "" : "disabled";

        //卒業可能な学年か判定
        $getData = knja120Query::getGraduationGrade($model);
        $model->GradGrade = "";
        $model->GradGrade = $getData["FLG"];
        $model->schoolKind = "";
        $model->schoolKind = $getData["SCHOOL_KIND"];

        if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            $totalRemarkKeta = 66;
            $totalRemarkGyo  = 8;
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $totalRemarkKeta = 66;
            $totalRemarkGyo  = 7;
        } else {
            $totalRemarkKeta = 44;
            $totalRemarkGyo  = 6;
        }
        $totalRemarkMoji = $totalRemarkKeta * 2;

        //総合的な学習の時間のタイトルの設定(元々の処理はelse側の処理。2021年以降は上の条件の表示となる。2019、2020は過渡期。)
        $db = Query::dbCheckOut();
        $gradeCd = $model->grade == "" ? "" : $db->getOne(knja120Query::getGradeCd($model));
        Query::dbCheckIn($db);
        if ($model->exp_year >= 2021
                   || ($model->exp_year == 2019 && $gradeCd == 1)
                   || ($model->exp_year == 2020 && ($gradeCd == 1 || $gradeCd == 2))) {
            $arg["TOP"]["TOTAL_STUDY_TIME_TITLE"] = "総<br>合<br>的<br>な<br>探<br>究<br>の<br>時<br>間<br>の<br>記<br>録<br>";
            $arg["TOP"]["TOTAL_STUDY_TIME_SUBTITLE"] = "総合的な探究の時間で代替";
        } else {
            $arg["TOP"]["TOTAL_STUDY_TIME_TITLE"] = "総<br>合<br>的<br>な<br>学<br>習<br>の<br>時<br>間<br>の<br>記<br>録<br>";
            $arg["TOP"]["TOTAL_STUDY_TIME_SUBTITLE"] = "総合的な学習の時間で代替";
        }

        //調査書より読込ボタンを作成する
        if ($model->GradGrade == "1") {
            $arg["chousasho_yomikomi"] = "1";
            $extra = $disabled." onclick=\" return btn_submit('chousasho');\" style=\"color:#1E90FF;font:bold\"";
            $arg["btn_chousasho"] = KnjCreateBtn($objForm, "btn_chousasho", "調査書より読込", $extra);
            if ($model->cmd === 'chousasho') {
                $getRow = knja120Query::getHexamEntremark($model);
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

                //総合所見(6分割取込)
                $db = Query::dbCheckOut();
                $query = knja120Query::getHexamTrainRef($model);
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
                if ($model->validate_row_cnt($totalRemark, $totalRemarkMoji) > $totalRemarkGyo) {
                    $torikomiErrMsg = "(総合所見は{$model->totalRemarkGyo}行までです)";
                    $arg["torikomiErr"] = "torikomiAlert('{$torikomiErrMsg}');";
                }

                //出欠の記録備考
                $row["ATTENDREC_REMARK"]    = $getRow["ATTENDREC_REMARK"];
            }
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //出欠の記録備考取込
        if ($model->cmd === 'torikomi3') {
            $set_remark = knja120Query::getSemesRemark($model);
            $row["ATTENDREC_REMARK"] = $set_remark;
        }
        
        //取込ボタンが押された時の1,2年次　通知票、3年次　調査書
        if ($model->cmd == 'reload') {
            //3年次　調査書
            if ($model->GradGrade == "1") {
                $getRow = knja120Query::getHexamEntremark($model);
                //総合的な学習の時間　活動、評価
                if ($model->Properties["tyousasyoSougouHyoukaNentani"] !== '1') {
                    $row2["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT"];
                    $row2["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL"];
                } else {
                    //年単位の時
                    $row2["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT_YEAR"];
                    $row2["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL_YEAR"];
                }
            //1,2年次　通知票
            } else {
                $totalstudytimeArray = array();
                $totalstudyactArray  = array();
                $query = knja120Query::get_record_totalstudytime_dat($model);
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
                $result->free();
                $row2["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
                $row2["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
                Query::dbCheckIn($db);
            }
        }

        //記入欄
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYACT",
                            "cols"        => 89,
                            "rows"        => 5,
                            "wrap"        => "soft",
                            "extrahtml"   => "id=\"TOTALSTUDYACT\"",
                            "value"       => $row2["TOTALSTUDYACT"] ));
        $arg["data"]["TOTALSTUDYACT"] = $objForm->ge("TOTALSTUDYACT");
        knjCreateHidden($objForm, "TOTALSTUDYACT_KETA", 88);
        knjCreateHidden($objForm, "TOTALSTUDYACT_GYO", 4);
        KnjCreateHidden($objForm, "TOTALSTUDYACT_STAT", "statusarea1");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYVAL",
                            "cols"        => 89,
                            "rows"        => 6,
                            "wrap"        => "soft",
                            "extrahtml"   => "id=\"TOTALSTUDYVAL\" style=\"height:90px;\"",
                            "value"       => $row2["TOTALSTUDYVAL"] ));
        $arg["data"]["TOTALSTUDYVAL"] = $objForm->ge("TOTALSTUDYVAL");
        knjCreateHidden($objForm, "TOTALSTUDYVAL_KETA", 88);
        knjCreateHidden($objForm, "TOTALSTUDYVAL_GYO", 6);
        KnjCreateHidden($objForm, "TOTALSTUDYVAL_STAT", "statusarea2");

        //奉仕の時間
        if ($model->getname === 'tokyoto') {
            //奉仕の時間チェックボックス
            $extra = " onclick=\"return btn_submit('daigae');\" id=\"DAIGAE_CHECK\"";
            if ($model->field["DAIGAE_CHECK"] == "1") {
                $extra .= "checked='checked' ";
                $row2["TOTALSTUDYACT2"] = '総合的な学習の時間で代替';
            } else {
                $extra .= "";
            }
            $arg["data"]["DAIGAE_CHECK"] = knjCreateCheckBox($objForm, "DAIGAE_CHECK", "1", $extra);
        
            $extra = "id=\"TOTALSTUDYACT2\" style=\"height:75px;\"";
            $arg["data"]["TOTALSTUDYACT2"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT2", 5, 89, "soft", $extra, $row2["TOTALSTUDYACT2"]);
            knjCreateHidden($objForm, "TOTALSTUDYACT2_KETA", 88);
            knjCreateHidden($objForm, "TOTALSTUDYACT2_GYO", 10);
            KnjCreateHidden($objForm, "TOTALSTUDYACT2_STAT", "statusarea3");

            //評価
            $extra = "id=\"TOTALSTUDYVAL2\" style=\"height:90px;\"";
            $arg["data"]["TOTALSTUDYVAL2"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL2", 6, 89, "soft", $extra, $row2["TOTALSTUDYVAL2"]);
            knjCreateHidden($objForm, "TOTALSTUDYVAL2_KETA", 88);
            knjCreateHidden($objForm, "TOTALSTUDYVAL2_GYO", 6);
            KnjCreateHidden($objForm, "TOTALSTUDYVAL2_STAT", "statusarea4");

            //備考
            $extra = "id=\"CREDITREMARK\" style=\"height:75px;\"";
            $arg["data"]["CREDITREMARK"] = KnjCreateTextArea($objForm, "CREDITREMARK", 5, 89, "soft", $extra, $row2["CREDITREMARK"]);
            knjCreateHidden($objForm, "CREDITREMARK_KETA", 88);
            knjCreateHidden($objForm, "CREDITREMARK_GYO", 5);
            KnjCreateHidden($objForm, "CREDITREMARK_STAT", "statusarea8");
        }
        
        if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "SPECIALACTREMARK",
                                "cols"        => 45,
                                "rows"        => 10,
                                "wrap"        => "soft",
                                "extrahtml"   => "id=\"SPECIALACTREMARK\" style=\"height:144px;\"",
                                "value"       => $row["SPECIALACTREMARK"] ));
            $arg["data"]["SPECIALACTREMARK"] = $objForm->ge("SPECIALACTREMARK");
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角22文字X10行まで)';
            knjCreateHidden($objForm, "SPECIALACTREMARK_KETA", 44);
            knjCreateHidden($objForm, "SPECIALACTREMARK_GYO", 10);
            KnjCreateHidden($objForm, "SPECIALACTREMARK_STAT", "statusarea5");
        } else {
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "SPECIALACTREMARK",
                                "cols"        => 23,
                                "rows"        => 6,
                                "wrap"        => "soft",
                                "extrahtml"   => "id=\"SPECIALACTREMARK\" style=\"height:90px;\"",
                                "value"       => $row["SPECIALACTREMARK"] ));
            $arg["data"]["SPECIALACTREMARK"] = $objForm->ge("SPECIALACTREMARK");
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角11文字X6行まで)';
            knjCreateHidden($objForm, "SPECIALACTREMARK_KETA", 22);
            knjCreateHidden($objForm, "SPECIALACTREMARK_GYO", 6);
            KnjCreateHidden($objForm, "SPECIALACTREMARK_STAT", "statusarea5");
        }

        $readOnly = "";
        $setStyle = "";
        if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "TOTALREMARK",
                                "cols"        => $totalRemarkMoji + 1,
                                "rows"        => $totalRemarkGyo,
                                "wrap"        => "soft",
                                "extrahtml"   => "id=\"TOTALREMARK\"{$readOnly} style=\"{$setStyle}\" ",
                                "value"       => $row["TOTALREMARK"] ));
            $arg["data"]["TOTALREMARK"] = $objForm->ge("TOTALREMARK");
            $arg["data"]["TOTALREMARK_TYUI"] = "(全角{$totalRemarkKeta}文字X{$totalRemarkGyo}行まで)";
            knjCreateHidden($objForm, "TOTALREMARK_KETA", $totalRemarkMoji);
            knjCreateHidden($objForm, "TOTALREMARK_GYO", $totalRemarkGyo);
            KnjCreateHidden($objForm, "TOTALREMARK_STAT", "statusarea6");
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "TOTALREMARK",
                                "cols"        => $totalRemarkMoji + 1,
                                "rows"        => $totalRemarkGyo,
                                "wrap"        => "soft",
                                "extrahtml"   => "id=\"TOTALREMARK\"{$readOnly} style=\"height:90px;{$setStyle}\"",
                                "value"       => $row["TOTALREMARK"] ));
            $arg["data"]["TOTALREMARK"] = $objForm->ge("TOTALREMARK");
            $arg["data"]["TOTALREMARK_TYUI"] = "(全角{$totalRemarkKeta}文字X{$totalRemarkGyo}行まで)";
            knjCreateHidden($objForm, "TOTALREMARK_KETA", $totalRemarkMoji);
            knjCreateHidden($objForm, "TOTALREMARK_GYO", $totalRemarkGyo);
            KnjCreateHidden($objForm, "TOTALREMARK_STAT", "statusarea6");
        } else {
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "TOTALREMARK",
                                "cols"        => $totalRemarkMoji + 1,
                                "rows"        => $totalRemarkGyo,
                                "wrap"        => "soft",
                                "extrahtml"   => "id=\"TOTALREMARK\"{$readOnly} style=\"height:90px;{$setStyle}\"",
                                "value"       => $row["TOTALREMARK"] ));
            $arg["data"]["TOTALREMARK"] = $objForm->ge("TOTALREMARK");
            $arg["data"]["TOTALREMARK_TYUI"] = "(全角{$totalRemarkKeta}文字X{$totalRemarkGyo}行まで)";
            knjCreateHidden($objForm, "TOTALREMARK_KETA", $totalRemarkMoji);
            knjCreateHidden($objForm, "TOTALREMARK_GYO", $totalRemarkGyo);
            KnjCreateHidden($objForm, "TOTALREMARK_STAT", "statusarea6");
        }

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "ATTENDREC_REMARK",
                            "cols"        => 41,
                            "rows"        => 3,
                            "wrap"        => "soft",
                            "extrahtml"   => "id=\"ATTENDREC_REMARK\" ",
                            "value"       => $row["ATTENDREC_REMARK"] ));
        $arg["data"]["ATTENDREC_REMARK"] = $objForm->ge("ATTENDREC_REMARK");
        knjCreateHidden($objForm, "ATTENDREC_REMARK_KETA", 40);
        knjCreateHidden($objForm, "ATTENDREC_REMARK_GYO", 2);
        KnjCreateHidden($objForm, "ATTENDREC_REMARK_STAT", "statusarea7");

        //出欠の記録参照ボタン
        if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
            $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);
            $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
        }

        //出欠備考参照ボタン
        $sdate = $model->exp_year.'-04-01';
        $edate = ((int)$model->exp_year+1).'-03-31';
        //和暦表示
        $warekiFlg = "";
        if ($model->Properties["useWarekiHyoji"] == 1) {
            $warekiFlg = "1";
        }
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $extra = "style=\"color:#1E90FF;font:bold\""; 
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = "";
            }
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $extra .= $disabled ." onclick=\"return btn_submit('torikomi3');\"";
            } else {
                $extra .= $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
        } else {
            if (!$model->schregno) {
                $extra = "onclick=\"alert('データを指定してください。')\"";
            } else {
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }

        //要録の出欠備考参照ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "TYOSASYO_SANSYO",
                            "value"     => "調査書(進学用)の出欠の記録参照",
                            "extrahtml" => $disabled." onclick=\"loadwindow('../../X/KNJXATTEND_ENTREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:230px;\""));
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
        //通知票所見参照ボタンを非表示とする。(近大のみ)
        $cnt = knja120Query::getKindaiJudgment($model);
        if ($cnt > 0) {
        } else {
            if ($model->Properties["unUseSyokenSansyoButton_H"] != '1') {
                $objForm->ae( array("type"        => "button",
                                    "name"        => "btn_popup",
                                    "value"       => "通知表所見参照",
                                    "extrahtml"   => $disabled." onclick=\"return btn_submit('subform1');\"" ));
                $arg["button"]["btn_popup"] = $objForm->ge("btn_popup");
            }
        }

        $prgid = "KNJX_HEXAM_ENTREMARK_TRAINREF_SELECT";
        $extraUrl = "'../../X/{$prgid}/index.php?GRADE_YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SEND_PRGID={$prgid}&SEND_AUTH={$model->auth}&TRAINREF_TARGET=TOTALREMARK{$addExtra}&TOTALREMARK_KETA=' + document.forms[0].TOTALREMARK_KETA.value + '&TOTALREMARK_GYO=' + document.forms[0].TOTALREMARK_GYO.value ";
        $extra = $disabled ." onclick=\"loadwindow({$extraUrl},0,document.documentElement.scrollTop || document.body.scrollTop,550,570);return;\"";

        $arg["button"]["TYOUSASYO_SENTAKU"] = KnjCreateBtn($objForm, "TYOUSASYO_SENTAKU", "調査書選択", $extra);

        if ($model->GradGrade != "1" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
            $extra = $disabled." onclick=\"return btn_submit('reload');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["button"]["btn_reload"] = knjCreateBtn($objForm, "btn_reload", "通知票取込", $extra);
        } else if ($model->GradGrade == "1") {
            $extra = $disabled." onclick=\"return btn_submit('reload');\" style=\"color:#1E90FF;font:bold;\"";
            $arg["button"]["btn_reload"] = knjCreateBtn($objForm, "btn_reload", "調査書取込", $extra);
        }

        //委員会選択ボタン
        $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREMARK", $disabled);

        //部活動選択ボタン（特別活動所見）1:表示
        if ($model->Properties["useKnja120_clubselect_Button"] == "1") {
            $arg["useclubselect"] = 1;
            $arg["button"]["btn_club_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SPECIALACTREMARK", $disabled);
        } else {
            $arg["useclubselect"] = 0;
        }

        //部活動選択ボタン（総合所見）
        $arg["button"]["btn_club_total"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TOTALREMARK", $disabled);

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $arg["button"]["btn_club_kirokubikou"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TOTALREMARK", $disabled);
        }

        //検定選択ボタン
        $arg["button"]["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TOTALREMARK", $disabled);

        //賞選択ボタン
        if ($model->Properties["useHyosyoSansyoButton_H"]) {
            $arg["button"]["btn_hyosyo"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TOTALREMARK", $disabled);
        }

        //定型文選択(総合所見)
        if ($model->Properties["seitoSidoYorokuSougou_Teikei_Button_Hyouji"] == "1") {
            $extra = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
            $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}&DATA_DIV=12&TITLE=総合所見&TEXTBOX=TOTALREMARK'";
            $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
            $arg["button"]["btn_teikei_totalremark"] = knjCreateBtn($objForm, "btn_teikei_totalremark", "定型文選択", $extra);
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
        //活動内容、評価はKNJA120では固定のため、CSVの呼び出し先KNJX181で指定
        $fieldSize  = "TOTALSTUDYACT=0,";
        $fieldSize .= "TOTALSTUDYVAL=0,";
        $gyouSize  = "TOTALSTUDYACT=0,";
        $gyouSize .= "TOTALSTUDYVAL=0,";
        //特別活動所見
        if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            $fieldSize .= "SPECIALACTREMARK=660,";
            $gyouSize .= "SPECIALACTREMARK=10,";
        } else {
            $fieldSize .= "SPECIALACTREMARK=198,";
            $gyouSize .= "SPECIALACTREMARK=6,";
        }
        //総合所見
        if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            $fieldSize .= "TOTALREMARK=1584,";
            $gyouSize .= "TOTALREMARK=8,";
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) { //KNJA120では総合所見のみプロパティは有効
            $fieldSize .= "TOTALREMARK=1386,";
            $gyouSize .= "TOTALREMARK=7,";
        } else {
            $fieldSize .= "TOTALREMARK=792,";
            $gyouSize .= "TOTALREMARK=6,";
        }
        //出欠の記録備考(KNJA120は一項目のみ)
        $fieldSize .= "ATTENDREC_REMARK=120,";
        $fieldSize .= "VIEWREMARK=0,";
        $fieldSize .= "BEHAVEREC_REMARK=0";
        $gyouSize .= "ATTENDREC_REMARK=2,";
        $gyouSize .= "VIEWREMARK=0,";
        $gyouSize .= "BEHAVEREC_REMARK=0";

        //セキュリティーチェック
        $db = Query::dbCheckOut();
        $securityCnt = $db->getOne(knja120Query::getSecurityHigh());
        Query::dbCheckIn($db);
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_check1",
                                "value"     => "※2 データ".$csvSetName,
                                "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX180/knjx180index.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&SEND_PRGID=KNJA120&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));
            $arg["button"]["btn_check1"] = $objForm->ge("btn_check1");

            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_check2",
                                "value"     => "※1 データ".$csvSetName,
                                "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX181/knjx181index.php?SEND_PRGID=KNJA120&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));
            $arg["button"]["btn_check2"] = $objForm->ge("btn_check2");
        }
        //プレビュー／印刷
        if ($model->Properties["sidouyourokuShokenPreview"] == '1') {
            $gradehrclass = knja120Query::getGradeHrclass($model);
            $extra =  "onclick=\"return newwin('".SERVLET_URL."', '".$gradehrclass."');\"";
            $arg["button"]["btn_print"] = KnjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        }
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));
        knjCreateHidden($objForm, "PRGID", "KNJA120");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "seitoSidoYorokuSougouFieldSize", $model->Properties["seitoSidoYorokuSougouFieldSize"]);
        knjCreateHidden($objForm, "seitoSidoYorokuSpecialactremarkFieldSize", $model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
        knjCreateHidden($objForm, "seitoSidoYorokuZaisekiMae", $model->Properties["seitoSidoYorokuZaisekiMae"]);
        knjCreateHidden($objForm, "seitoSidoYorokuKoumokuMei", $model->Properties["seitoSidoYorokuKoumokuMei"]);
        knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
        knjCreateHidden($objForm, "useQualifiedMst", $model->Properties["useQualifiedMst"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }
        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knja120Form1.html", $arg);
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
