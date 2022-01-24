<?php

require_once('for_php7.php');
class knja120bForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja120bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            //HTRAINREMARK_DAT 取得
            if ($model->cmd !== 'torikomi4' && $model->cmd !== 'reload' && $model->cmd !== 'reload2') {
                $query = knja120bQuery::getTrainRow($model, "");
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        
        if($model->schregno){

            //年次取得
            $model->gradecd = "";
            $model->gradecd = $db->getOne(knja120bQuery::getGradeCd($model));
            //名称マスタ「A023」の予備2,予備3情報取得
            $startGradeCd = $db->getOne(knja120bQuery::getNameMstGradecdCheck("start"));
            $endGradeCd = $db->getOne(knja120bQuery::getNameMstGradecdCheck("end"));

            //1,2年次：通知票取込、3年次:調査書取込ボタンを作成する（プロパティにてボタン表示非表示の切り替え）
            if ($model->gradecd < $startGradeCd && $model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
                $extra = "onclick=\"return btn_submit('reload');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["btn_reload"] = knjCreateBtn($objForm, "btn_reload", "通知票取込", $extra);
            } else if ($startGradeCd <= $model->gradecd && $model->gradecd <= $endGradeCd) {
                //3年次通知票取込(京都からの要望)
                if ($model->Properties["tutihyoYoriYomikomiHyoujiFlg"] == 1) {
                    $extra = "onclick=\"return btn_submit('reload2');\" style=\"color:#1E90FF;font:bold;\"";
                    $arg["btn_reload2"] = knjCreateBtn($objForm, "btn_reload2", "通知票取込", $extra);
                }
                $extra = "onclick=\"return btn_submit('reload');\" style=\"color:#1E90FF;font:bold;\"";
                $arg["btn_reload"] = knjCreateBtn($objForm, "btn_reload", "調査書取込", $extra);
            }

            //通知票　調査書取込
            if ($model->cmd === 'reload') {
                //3年次　調査書
                if ($startGradeCd <= $model->gradecd && $model->gradecd <= $endGradeCd) {
                    $getRow = array();
                    $getRow = $db->getRow(knja120bQuery::getHexamEntremark($model), DB_FETCHMODE_ASSOC);
                    //総合的な学習の時間　活動、評価
                    if ($model->Properties["tyousasyoSougouHyoukaNentani"] !== '1') {
                        $Row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT"];
                        $Row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL"];
                    } else {
                        //年単位の時
                        $Row["TOTALSTUDYACT"]       = $getRow["TOTALSTUDYACT_YEAR"];
                        $Row["TOTALSTUDYVAL"]       = $getRow["TOTALSTUDYVAL_YEAR"];
                    }
                //1, 2年次　通知票
                } else {
                    $totalstudytimeArray = array();
                    $totalstudyactArray  = array();
                    $query = knja120bQuery::get_record_totalstudytime_dat($model);
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
                        $Row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
                    }
                    if (get_count($totalstudyactArray) > 0) {
                        $Row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
                    }
                }
            }

            //3年次用の通知票取込
            if ($model->cmd === 'reload2') {
                $totalstudytimeArray = array();
                $totalstudyactArray  = array();
                $query = knja120bQuery::get_record_totalstudytime_dat($model);
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
                    $Row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
                }
                if (get_count($totalstudyactArray) > 0) {
                    $Row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
                }
            }

            //学習記録データ
            $query = knja120bQuery::getStudyRec($model);
            $result = $db->query($query);
            $study = "";
            while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                              $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                } else {
                    $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                              $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                }
            }

            //出欠記録データ
            $attend = $db->getRow(knja120bQuery::getAttendRec($model), DB_FETCHMODE_ASSOC);

            //HTRAINREMARK_DATのハッシュ値取得
            $hash = ($model->schregno && $Row) ? $model->makeHash($Row, $study, $attend) : "";
            //ATTEST_OPINIONS_DATのハッシュ値取得
            $opinion = $db->getRow(knja120bQuery::getOpinionsDat($model), DB_FETCHMODE_ASSOC);

            //ハッシュ値の比較
            if(($opinion && $Row && ($opinion["OPINION"] != $hash)) || (!$hash && $opinion)) {
                $arg["jscript"] = "alert('署名時のデータと不一致です。')";
            }
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        if ($model->cmd === 'torikomi4') {
            $set_remark = knja120bQuery::getHreportremarkDetailDat($db, $model);
            $Row["ATTENDREC_REMARK"] = $set_remark;
        }

        //京都仕様の場合、総合的な学習の時間の記録の評価は入力不可とする
        $model->getSchoolName = "";
        $model->getSchoolName = $db->getOne(knja120bQuery::getNameMst("Z010"));
        if ($model->getSchoolName === 'kyoto') {
            if ($model->Properties["kyotoSougouNyuryokuOk"] == "1") {
                $kyotoReadOnly = "";
            } else {
                $kyotoReadOnly = " STYLE=\"background:darkgray\" readOnly ";
            }
        } else {
            $kyotoReadOnly = "";
        }
        if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //活動内容
            $extra = "style=\"height:120px;\"";
            $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 8, 45, "soft", $extra.$kyotoReadOnly, $Row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角22文字X8行まで)';

            //評価
            $extra = "style=\"height:120px;\"";
            $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, 45, "soft", $extra.$kyotoReadOnly, $Row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角22文字X8行まで)';

            //出欠の記録備考
            $extra = "style=\"height:35px;\"";
            $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 81, "soft", $extra, $Row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角40文字X2行まで)';
        } else {
            //活動内容
            $extra = "style=\"height:63px;\"";
            $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 5, 23, "soft", $extra.$kyotoReadOnly, $Row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角11文字X4行まで)';

            //評価
            $extra = "style=\"height:90px;\"";
            $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 6, 23, "soft", $extra.$kyotoReadOnly, $Row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角11文字X6行まで)';

            //出欠の記録備考
            $extra = "style=\"height:35px;\"";
            $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 41, "soft", $extra, $Row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角20文字X2行まで)';
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            //活動内容
            $height = $model->totalstudyact_gyou * 13.5 + ($model->totalstudyact_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", $extra.$kyotoReadOnly, $Row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            //評価
            $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", $extra.$kyotoReadOnly, $Row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]) {
            //出欠の記録備考
            $height = $model->attendrec_remark_gyou * 13.5 + ($model->attendrec_remark_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ($model->attendrec_remark_moji * 2 + 1), "soft", $extra, $Row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";
        }

        if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            //特別活動所見
            $extra = "style=\"height:145px;\"";
            $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 45, "soft", $extra, $Row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角22文字X10行まで)';
        } else {
            //特別活動所見
            $extra = "style=\"height:90px;\"";
            $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $Row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角11文字X6行まで)';
        }
        if ($model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]) {
            //特別活動所見
            $height = $model->specialactremark_gyou * 13.5 + ($model->specialactremark_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", $model->specialactremark_gyou, ($model->specialactremark_moji * 2 + 1), "soft", $extra, $Row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = "(全角{$model->specialactremark_moji}文字{$model->specialactremark_gyou}行まで)";
        }

        if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            //総合所見
            $extra = "style=\"height:120px;\"";
            $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 8, 133, "soft", $extra, $Row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X8行まで)';
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //総合所見
            $extra = "style=\"height:105px;\"";
            $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 7, 133, "soft", $extra, $Row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X7行まで)';
        } else {
            //総合所見
            $extra = "style=\"height:90px;\"";
            $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 6, 89, "soft", $extra, $Row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角44文字X6行まで)';
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]) {
            //総合所見
            $height = $model->totalremark_gyou * 13.5 + ($model->totalremark_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", $model->totalremark_gyou, ($model->totalremark_moji * 2 + 1), "soft", $extra, $Row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = "(全角{$model->totalremark_moji}文字{$model->totalremark_gyou}行まで)";
        }

        //署名チェック
        $query = knja120bQuery::getOpinionsWk($model);
        $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opinion = ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) ? false : true;

        //ボタン作成
        makeBtn($objForm, $arg, $db, $model, $opinion);

        //PDF取込
        if ($model->Properties["useUpdownPDF"] === '1') {
            $arg["useUpdownPDF"] = '1';
            updownPDF($objForm, $arg, $model);
        }

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja120bForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$db, $model, $opinion)
{
    if((AUTHORITY < DEF_UPDATE_RESTRICT) || !$opinion){
        //更新ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
        //前の生徒へボタン
        $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('pre');\"";
        $arg["button"]["btn_up_pre"]   = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
        //次の生徒へボタン
        $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('next');\"";
        $arg["button"]["btn_up_next"]  = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
    } else {
        //更新ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = updateNext($model, $objForm, $arg, 'btn_update');
    }
    //取消ボタン
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    $disabled = ($model->schregno) ? "" : "disabled";

    //委員会選択ボタン
    $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREMARK", $disabled);

    //部活動選択ボタン（特別活動所見）1:表示
    if ($model->Properties["useKnja120_clubselect_Button"] == "1") {
        $arg["useclubselect"] = 1;
        $arg["button"]["btn_club_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SPECIALACTREMARK", $disabled);
    } else {
        $arg["useclubselect"] = 0;
    }

    //調査書選択(総合所見)
    $prgid = "KNJX_HEXAM_ENTREMARK_TRAINREF_SELECT";
    $auth = AUTHORITY;
    $extra = $disabled ." onclick=\"loadwindow('../../X/{$prgid}/index.php?GRADE_YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SEND_PRGID={$prgid}&SEND_AUTH={$auth}&TRAINREF_TARGET=TOTALREMARK',0,document.documentElement.scrollTop || document.body.scrollTop,550,570);return;\"";
    $arg["button"]["TYOUSASYO_SENTAKU"] = KnjCreateBtn($objForm, "TYOUSASYO_SENTAKU", "調査書選択", $extra);

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

    //通知票所見参照
    $cnt = $db->getOne(knja120bQuery::getKindaiJudgment($model));
    if (!$cnt) {
        $extra = " onclick=\"return btn_submit('tuutihyou');\"";
        $arg["button"]["tuutihyou"] = knjCreateBtn($objForm, "tuutihyou", "通知票所見参照", $extra);
    }
    //出欠備考参照ボタン
    $sdate = CTRL_YEAR.'-04-01';
    $edate = (CTRL_YEAR+1).'-03-31';
    if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        $arg["button"]["syukketu"] = KnjCreateBtn($objForm, "syukketu", "まとめ出欠備考参照", $extra);
    } else if ($model->getSchoolName == "mieken") {
        $extra  = "style=\"color:#1E90FF;font:bold;\"";
        $extra .= $disabled ." onclick=\"return btn_submit('torikomi4');\"";
        $arg["button"]["syukketu"] = KnjCreateBtn($objForm, "syukketu", "通知票取込", $extra);
    } else {
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
        $arg["button"]["syukketu"] = KnjCreateBtn($objForm, "syukketu", "日々出欠備考参照", $extra);
    }
    
    //調査書(進学用)出欠の記録参照
    $extra = " onclick=\"return btn_submit('tyousasyo');\"";
    $arg["button"]["tyousasyo"] = knjCreateBtn($objForm, "tyousasyo", "調査書(進学用)出欠の記録参照", $extra);
    //既入力内容参照（特別活動所見）
    $extra = " onclick=\"return btn_submit('shokenlist1');\"";
    $arg["button"]["shokenlist1"] = knjCreateBtn($objForm, "shokenlist1", "既入力内容の参照", $extra);
    //既入力内容参照（総合所見）
    $extra = " onclick=\"return btn_submit('shokenlist2');\"";
    $arg["button"]["shokenlist2"] = knjCreateBtn($objForm, "shokenlist2", "既入力内容の参照", $extra);
    //既入力内容参照（出欠の記録備考）
    $extra = " onclick=\"return btn_submit('shokenlist3');\"";
    $arg["button"]["shokenlist3"] = knjCreateBtn($objForm, "shokenlist3", "既入力内容の参照", $extra);

    //CSV処理用フィールドサイズ取得
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

    //CSVボタン
    $extra = ($model->schregno) ? " onClick=\" wopen('".REQUESTROOT."/X/KNJX180B/knjx180bindex.php?cmd=sign&FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&AUTH=".AUTHORITY."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" : "disabled";
    $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);
    //プレビュー／印刷
    if ($model->Properties["sidouyourokuShokenPreview"] == '1') {
        $extra =  ($model->schregno) ? "onclick=\"return newwin('".SERVLET_URL."');\"" : "disabled";
        $arg["button"]["btn_print"] = KnjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
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

function updateNext(&$model, &$objForm, &$arg, $btn='btn_update'){
    //更新ボタン
    $objForm->ae( array("type"      =>  "button",
                        "name"      =>  "btn_up_pre",
                        "value"     =>  "更新後前の生徒へ",
                        "extrahtml" =>  "style=\"width:130px\" onclick=\"top.left_frame.updateNext(self, 'pre','".$btn ."');\""));

    //更新ボタン
    $objForm->ae( array("type"      =>  "button",
                        "name"      =>  "btn_up_next",
                        "value"     =>  "更新後次の生徒へ",
                        "extrahtml" =>  "style=\"width:130px\" onclick=\"top.left_frame.updateNext(self, 'next','".$btn ."');\""));

    if ($_POST["_ORDER"] == "pre" || $_POST["_ORDER"] == "next" ){
       $order = $_POST["_ORDER"];
       if (!isset($model->warning)){
            $arg["jscript"] = "updBtnNotDisp(); top.left_frame.nextLink('".$order."')";
            unset($model->message);
       }
    }
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "_ORDER" ));
                    
    return $objForm->ge("btn_up_pre") .$objForm->ge("btn_up_next");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

    knjCreateHidden($objForm, "PRGID", "KNJA120B");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRINT_YEAR");
    knjCreateHidden($objForm, "PRINT_SEMESTER");
    knjCreateHidden($objForm, "GRADE_HR_CLASS");
    knjCreateHidden($objForm, "seitoSidoYorokuSougouFieldSize", $model->Properties["seitoSidoYorokuSougouFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuSpecialactremarkFieldSize", $model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuZaisekiMae", $model->Properties["seitoSidoYorokuZaisekiMae"]);
    knjCreateHidden($objForm, "seitoSidoYorokuKoumokuMei", $model->Properties["seitoSidoYorokuKoumokuMei"]);
    knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg", $model->Properties["seitoSidoYoroku_Totalstudyact2_val2_UseTextFlg"]);
    knjCreateHidden($objForm, "seitoSidoYorokuFormType", $model->Properties["seitoSidoYorokuFormType"]);
    knjCreateHidden($objForm, "notPrintFinschooltypeName", $model->Properties["notPrintFinschooltypeName"]);
    knjCreateHidden($objForm, "seitoSidoYorokuFinschoolFinishDateYearOnly", $model->Properties["seitoSidoYorokuFinschoolFinishDateYearOnly"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_SpecialactremarkSize", $model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalremarkSize", $model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_Attendrec_RemarkSize", $model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuTotalStudyCombineHtrainremarkDat", $model->Properties["seitoSidoYorokuTotalStudyCombineHtrainremarkDat"]);
    knjCreateHidden($objForm, "seitoSidoYorokuSougouHyoukaNentani", $model->Properties["seitoSidoYorokuSougouHyoukaNentani"]);
    knjCreateHidden($objForm, "seitoSidoYorokuHoushiNentani", $model->Properties["seitoSidoYorokuHoushiNentani"]);

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
