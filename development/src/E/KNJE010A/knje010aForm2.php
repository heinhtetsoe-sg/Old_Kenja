<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010aForm2 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("form2", "POST", "knje010aindex.php", "", "form2");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        //年度(年次)取得コンボ
        if ($model->cmd == "form2_first") { // すでに別の生徒を開いていた場合そのときの値が保持されているので
            $model->annual["YEAR"]   = "";  // 最初の呼出ならば、年度と年次をクリアする
            $model->annual["ANNUAL"] = "";
        }
        $opt = $chkOpt = array();
        
        //追加された年度を再セット
        $selectdata = ($model->selectdata != "") ? explode("-", $model->selectdata) : array();
        $selectdataText = ($model->selectdataText != "") ? explode("-", $model->selectdataText) : array();
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt[] = array("label" => $selectdataText[$i],
                           "value" => $selectdata[$i]);
        }

        $disabled = "disabled";
        $query = knje010aQuery::selectQueryAnnual_knje010aForm2($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (!in_array($row["YEAR"] ."," .$row["ANNUAL"], $selectdata)) {
                $opt[] = array("label" => $row["YEAR"] ."年度　" .(int) $row["ANNUAL"] ."学年(年次)",
                               "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                              );
            }
            $chkOpt[] = $row["YEAR"] ."," .$row["ANNUAL"];
            if (!isset($model->annual["YEAR"]) || ($model->cmd == "form2_first" && 
               (($model->mode == "ungrd" && $model->exp_year == $row["YEAR"]) || ($model->mode == "grd" && $model->grd_year == $row["YEAR"])))){
                $model->annual["YEAR"]   = $row["YEAR"];
                $model->annual["ANNUAL"] = $row["ANNUAL"];
            }

            $disabled = "";
        }
        if (!strlen($model->annual["YEAR"]) || !strlen($model->annual["ANNUAL"])) {
            list($model->annual["YEAR"], $model->annual["ANNUAL"]) = preg_split("/,/", $opt[0]["value"]);
        }
        
        $addYearFlg = "";
        if (!in_array($model->annual["YEAR"].",".$model->annual["ANNUAL"], $chkOpt)) {
            $disabled = "disabled";
            $addYearFlg = "1";
        }

        //年次取得　年度-学年コンボにより切り替わる
        $query = knje010aQuery::getGradeCd($model, "set");
        $getGdat = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->combo_gradecd = "";
        $model->combo_gradecd = $getGdat["GRADE_CD"];
        $model->schoolKind = "";
        $model->schoolKind = $getGdat["SCHOOL_KIND"];

        if (!isset($model->warning) && $model->cmd != 'reload4') {
            if ($model->cmd == "reload2_ok" || $model->cmd == "reload2_cancel") {
                $query = knje010aQuery::selectQuery_Htrainremark_Dat($model);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($model->cmd == "reload2_cancel") {
                    if ($row) {
                        foreach ($row as $key => $val) {
                            $row[$key] = $model->field2[$key]."\n".$val;
                        }
                    } else {
                        $row = $model->field2;
                    }
                }
            } else {
                if ($model->cmd !== 'torikomi3') {
                    $query = knje010aQuery::selectQueryForm2($model);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                } else {
                    $row = $model->field2;
                }
            }
            if (($model->cmd == "reload2_ok" || $model->cmd == "reload2_cancel") && $model->Properties["useSyojikou3"] == "1") {
                $row["TRAIN_REF1"] = $model->field2["TRAIN_REF1"];
                $row["TRAIN_REF2"] = $model->field2["TRAIN_REF2"];
                $row["TRAIN_REF3"] = $model->field2["TRAIN_REF3"];
            }
        } else {
            $row = $model->field2;
        }

        if($model->entDiv == '4' || $model->entDiv == '5' || $model->entDiv == '7'){
            $arg["addYear"] = 1;
            //追加年度
            $query = knje010aQuery::selectYearQuery($model);
            $extra = "onChange=\"return btn_submit('form2');\"";
            makeCmb($objForm, $arg, $db, $query, $model->addYear, "ADD_YEAR", $extra, 1);

            //追加学年
            $query = knje010aQuery::selectGradeQuery($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->addYearGrade, "ADD_YEAR_GRADE", $extra, 1);

            //年度追加ボタンを作成する
            $extra = " onclick=\"return add('form2');\"";
            $arg["btn_add_year"] = KnjCreateBtn($objForm, "btn_update", "年度追加", $extra);
        }

        //指導要録データ、調査書旧データ
        if ($model->Properties["useSyojikou3"] == "1") {
            $query = knje010aQuery::sansyou_data($model);
            $sansyou = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $row["TRAIN_REF"]   = $sansyou["TRAIN_REF"];
            $row["TOTALREMARK"] = $sansyou["TOTALREMARK"];
        }

        if ($model->cmd == 'reload4') {
            //1,2年次　指導要録取込
            if (intval($model->combo_gradecd) < "3") {
                $query = knje010aQuery::getYouroku($model);
                $resultYouroku = $db->query($query);
                $kaigyou = "";
                $row["TOTALSTUDYACT"] = "";
                $row["TOTALSTUDYVAL"] = "";
                while ($rowYouroku = $resultYouroku->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row["TOTALSTUDYACT"] .= $kaigyou.$rowYouroku["TOTALSTUDYACT"];
                    $row["TOTALSTUDYVAL"] .= $kaigyou.$rowYouroku["TOTALSTUDYVAL"];
                    $kaigyou = "\r\n";
                }
            //3年次　通知票取込
            } else {
                $totalstudytimeArray = array();
                $totalstudyactArray  = array();
                $query = knje010aQuery::get_record_totalstudytime_dat($model);
                $result = $db->query($query);
                while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($total_row["TOTALSTUDYACT"] != '') {
                        $totalstudyactArray[] = $total_row["TOTALSTUDYACT"];
                    }
                    if ($total_row["TOTALSTUDYTIME"] != '') {
                        $totalstudytimeArray[] = $total_row["TOTALSTUDYTIME"];
                    }
                }
                $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
                $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
            }
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual["YEAR"] ."," .$model->annual["ANNUAL"],
                            "extrahtml"  => "onChange=\"return btn_submit('form2');\"",
                            "options"    => $opt));

        $arg["ANNUAL"] = $objForm->ge("ANNUAL");
        
        //出欠の記録備考取込
        if ($model->cmd === 'torikomi3') {
            $set_remark = knje010aQuery::getSemesRemark($model, $db, $model->annual["YEAR"]);
            $row["ATTENDREC_REMARK"] = $set_remark;
        }

        /******************/
        /* テキストエリア */
        /******************/
        //出欠の記録備考
        $arg["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", ($model->attendrec_remark_gyou + 1), ($model->attendrec_remark_moji * 2 + 1), "soft", "", $row["ATTENDREC_REMARK"]);
        $arg["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字X{$model->attendrec_remark_gyou}行まで)";
        //特記事項なしチェックボックス
        $extra = " id=\"INS_COMMENTS\" onclick=\"return insertComment(this, 'ATTENDREC_REMARK', 'INS_COMMENTS_LABEL');\"";
        $arg["INS_COMMENTS"] = knjCreateCheckBox($objForm, "INS_COMMENTS", "1", $extra, "");
        //特記事項なし
        $ins_comments_label = '特記事項なし';
        knjCreateHidden($objForm, "INS_COMMENTS_LABEL", $ins_comments_label);
        $arg["INS_COMMENTS_LABEL"] = $ins_comments_label;
        //出欠の記録備考の「斜線を入れる」チェックボックス表示
        if ($model->Properties["useAttendrecRemarkSlashFlg"] == 1) {
            $arg["useAttendrecRemarkSlashFlg"] = 1;
        }
        //斜線を入れるチェックボックス
        $extra  = ($row["ATTENDREC_REMARK_SLASH_FLG"] == "1") ? "checked" : "";
        $extra .= " id=\"ATTENDREC_REMARK_SLASH_FLG\"";
        $arg["ATTENDREC_REMARK_SLASH_FLG"] = knjCreateCheckBox($objForm, "ATTENDREC_REMARK_SLASH_FLG", "1", $extra, "");

        //特別活動の記録
        $arg["SPECIALACTREC"] = KnjCreateTextArea($objForm, "SPECIALACTREC", ($model->specialactrec_gyou + 1), ($model->specialactrec_moji * 2 + 1), "soft", "", $row["SPECIALACTREC"]);
        $arg["SPECIALACTREC_TYUI"] = "(全角{$model->specialactrec_moji}文字X{$model->specialactrec_gyou}行まで)";
        //指導上参考となる諸事項
        if ($model->Properties["useSyojikou3"] == "1") {
            $arg["useSyojikou3"] = $model->Properties["useSyojikou3"];
            $height1 = $model->train_ref1_gyou * 13.5 + ($model->train_ref1_gyou -1 ) * 3 + 5;
            $height2 = $model->train_ref2_gyou * 13.5 + ($model->train_ref2_gyou -1 ) * 3 + 5;
            $height3 = $model->train_ref3_gyou * 13.5 + ($model->train_ref3_gyou -1 ) * 3 + 5;
            $arg["TRAIN_REF1"] = KnjCreateTextArea($objForm, "TRAIN_REF1", ($model->train_ref1_gyou + 1), ($model->train_ref1_moji * 2 + 1), "soft", "style=\"height:{$height1}px;\"", $row["TRAIN_REF1"]);
            $arg["TRAIN_REF2"] = KnjCreateTextArea($objForm, "TRAIN_REF2", ($model->train_ref2_gyou + 1), ($model->train_ref2_moji * 2 + 1), "soft", "style=\"height:{$height2}px;\"", $row["TRAIN_REF2"]);
            $arg["TRAIN_REF3"] = KnjCreateTextArea($objForm, "TRAIN_REF3", ($model->train_ref3_gyou + 1), ($model->train_ref3_moji * 2 + 1), "soft", "style=\"height:{$height3}px;\"", $row["TRAIN_REF3"]);
            $arg["TRAIN_REF"]  = KnjCreateTextArea($objForm, "TRAIN_REF", 5, 83, "soft", "style=\"background-color:#D0D0D0;height:60px;\"", $row["TRAIN_REF"]);
            $arg["TOTALREMARK"]  = KnjCreateTextArea($objForm, "TOTALREMARK", 5, 83, "soft", "style=\"background-color:#D0D0D0;height:60px;\"", $row["TOTALREMARK"]);
            $arg["COLSPAN2"] = "colspan=\"2\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"3\"";
            $arg["TRAIN_REF1_COMMENT"] = "(全角{$model->train_ref1_moji}文字{$model->train_ref1_gyou}行まで)";
            $arg["TRAIN_REF2_COMMENT"] = "(全角{$model->train_ref2_moji}文字{$model->train_ref2_gyou}行まで)";
            $arg["TRAIN_REF3_COMMENT"] = "(全角{$model->train_ref3_moji}文字{$model->train_ref3_gyou}行まで)";
        } else {
            $arg["no_useSyojikou3"] = '1';
            $arg["COLSPAN_TRAIN_REF"] = "colspan=\"2\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"2\"";
            if ($model->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                $arg["TRAIN_REF"] = KnjCreateTextArea($objForm, "TRAIN_REF", 7, 117, "soft", "style=\"height:105px;\"", $row["TRAIN_REF"]);
                $arg["TRAIN_REF_COMMENT"] = "(全角58文字X7行まで)";
            } else {
                $arg["TRAIN_REF"] = KnjCreateTextArea($objForm, "TRAIN_REF", 5, 83, "soft", "style=\"height:77px;\"", $row["TRAIN_REF"]);
                $arg["TRAIN_REF_COMMENT"] = "(全角41文字X5行まで)";
            }
        }
        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
            //HTML側で表示・非表示の判定に使う
            $arg["tyousasyoSougouHyoukaNentani"] = 1;

            //総合的な学習の時間の「斜線を入れる」チェックボックス表示
            if ($model->Properties["useTotalstudySlashFlg"] == 1) {
                $arg["useTotalstudySlashFlg"] = 1;
            }

            //活動内容
            $height = $model->totalstudyact_gyou * 13.5 + ($model->totalstudyact_gyou -1 ) * 3 + 5;
            $arg["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["TOTALSTUDYACT"]);
            $arg["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字X{$model->totalstudyact_gyou}行まで)";
            //斜線を入れるチェックボックス
            $extra  = ($row["TOTALSTUDYACT_SLASH_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"TOTALSTUDYACT_SLASH_FLG\"";
            $arg["TOTALSTUDYACT_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYACT_SLASH_FLG", "1", $extra, "");

            //評価
            $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1 ) * 3 + 5;
            $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["TOTALSTUDYVAL"]);
            $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字X{$model->totalstudyval_gyou}行まで)";
            //斜線を入れるチェックボックス
            $extra  = ($row["TOTALSTUDYVAL_SLASH_FLG"] == "1") ? "checked" : "";
            $extra .= " id=\"TOTALSTUDYVAL_SLASH_FLG\"";
            $arg["TOTALSTUDYVAL_SLASH_FLG"] = knjCreateCheckBox($objForm, "TOTALSTUDYVAL_SLASH_FLG", "1", $extra, "");
        }

        /**********/
        /* ボタン */
        /**********/
        //生徒指導要録より読込ボタンを作成する
        $extra = "onclick=\" return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
        $arg["btn_reload2"] = KnjCreateBtn($objForm, "btn_reload2", "生徒指導要録より読込", $extra);
        //部活動選択ボタン
        //特別活動の記録
        $arg["btn_club_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SPECIALACTREC", $disabled);
        if ($model->Properties["useSyojikou3"] == "1") {
            //指導上参考となる諸事項（3分割・中）
            $arg["btn_club_tra2"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TRAIN_REF2", $disabled);
        } else {
            //指導上参考となる諸事項
            $arg["btn_club_tra"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TRAIN_REF", $disabled);
        }
        //委員会選択ボタン
        $arg["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREC", $disabled);
        //賞選択ボタン
        if ($model->Properties["useHyosyoSansyoButton_H"]) {
            //特別活動の記録
            $arg["btn_hyosyo_spe"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "SPECIALACTREC", $disabled);
            if ($model->Properties["useSyojikou3"] == "1") {
                //指導上参考となる諸事項（3分割・中）
                $arg["btn_hyosyo_tra2"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TRAIN_REF2", $disabled);
            } else {
                //指導上参考となる諸事項
                $arg["btn_hyosyo_tra"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TRAIN_REF", $disabled);
            }
        }
        //検定選択ボタン
        if ($model->Properties["useSyojikou3"] == "1") {
            $arg["btn_qualified_tra2"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TRAIN_REF2", $disabled);
        } else {
            $arg["btn_qualified_tra"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TRAIN_REF", $disabled);
        }
        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            if ($model->Properties["useSyojikou3"] == "1") {
                //指導上参考となる諸事項（3分割・中）
                $arg["btn_club_kirokubikou_tra2"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TRAIN_REF2", $disabled);
            } else {
                //指導上参考となる諸事項
                $arg["btn_club_kirokubikou_tra"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TRAIN_REF", $disabled);
            }
        }
        //出欠の記録参照ボタン
        if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
            $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);
            $arg["tyousasyoSyukketsuKirokuBtn"] = 1;
        }
        //出欠備考参照ボタン
        $sdate = $model->annual["YEAR"].'-04-01';
        $edate = ($model->annual["YEAR"]+1).'-03-31';
        //和暦表示フラグ
        $warekiFlg = "";
        if ($model->Properties["useWarekiHyoji"] == "1") {
            $warekiFlg = "1";
        }
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $extra = $disabled ." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('torikomi3');\"";
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
        } else {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }
        //要録の出欠備考参照ボタン
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}',0,document.documentElement.scrollTop || document.body.scrollTop,360,180);return;\" style=\"width:210px;\"";
        $arg["YOROKU_SANSYO"] = KnjCreateBtn($objForm, "YOROKU_SANSYO", "要録の出欠の記録備考参照", $extra);
        //年間出欠備考選択ボタン
        if ($model->Properties["useReasonCollectionBtn"] == 1) {
            $arg["btn_reason_collection_select"] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select", "年間出欠備考選択", "ATTENDREC_REMARK", $disabled);
            $arg["REASON_COLLECTION_SELECT"] = 1;
        }

        //定型文選択ボタンを作成する
        if ($model->Properties["Teikei_Button_Hyouji_Tyousasyo"] == "1") {
            $extra = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
            $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}&DATA_DIV=01&TITLE=総合的な学習の時間の記録（活動内容）&TEXTBOX=TOTALSTUDYACT'";
            $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 100 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
            $arg["btn_teikei_act"] = knjCreateBtn($objForm, "btn_teikei_act", "定型文選択", $extra);

            $extra = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
            $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}&DATA_DIV=02&TITLE=総合的な学習の時間の記録（評価）&TEXTBOX=TOTALSTUDYVAL'";
            $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 250 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
            $arg["btn_teikei_val"] = knjCreateBtn($objForm, "btn_teikei_val", "定型文選択", $extra);
        }

        if($addYearFlg == "1"){
            //更新・クリアボタンを使用可とする
            $disabled = "";
        }

        //更新ボタンを作成する
        $extra = $disabled ." onclick=\"return btn_submit('update2');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //クリアボタンを作成する
        $extra = $disabled ." onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);
        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
            //1,2年次　指導要録取込、3年次　通知票取込み(総合的な学習の時間　通年用)
            if (intval($model->combo_gradecd) > "2" && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
                $extra = "onclick=\"return btn_submit('reload4');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["btn_reload4"] = knjCreateBtn($objForm, "btn_reload4", "通知票取込", $extra);
            } else if (intval($model->gradecd) < "3" && $model->Properties["tyousasyo_SidoYorokYoriYomikomiHyoujiFlg"] == 1) {
                $extra = "onclick=\"return btn_submit('reload4');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["btn_reload4"] = knjCreateBtn($objForm, "btn_reload4", "指導要録取込", $extra);
            }
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdataText");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010aForm2.html", $arg);
    }
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled="") {
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                   //部活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } else if ($div == "committee") {       //委員会
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } else if ($div == "qualified") {       //検定
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,900,500);\"";
        } else if ($div == "hyosyo") {          //賞
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } else if ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } else if ($div == "reason_collection") {   //年間出欠備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } else if ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->annual["YEAR"]}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    if ($name == "SEMESTER") {
        $value = ($value) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    }

    if ($name == "ADD_YEAR_GRADE" || $name == "ADD_YEAR") {
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}
?>