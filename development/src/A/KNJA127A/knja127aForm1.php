<?php

require_once('for_php7.php');

class knja127aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja127aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->cmd !== 'torikomi3' && $model->cmd !== 'chousasho') {
                $Row  = $db->getRow(knja127aQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
                $RowH = $db->getRow(knja127aQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
                $RowD = $db->getRow(knja127aQuery::getTrainDetailRow($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row  =& $model->field;
                $RowH =& $model->field;
                $RowD =& $model->field;
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row  =& $model->field;
            $RowH =& $model->field;
            $RowD =& $model->field;
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //年次取得
        $model->gradecd = $db->getOne(knja127aQuery::getGradeCd($model));

        //調査書より読込ボタンを作成する
        if ($model->gradecd === '03') {
            //調査書より読込ボタン表示
            $arg["chousasho_yomikomi"] = "1";

            //調査書より読込ボタン
            $extra = "onclick=\" return btn_submit('chousasho');\" style=\"color:#1E90FF;font:bold\"";
            $arg["btn_chousasho"] = KnjCreateBtn($objForm, "btn_chousasho", "調査書より読込", $extra);

            //調査書データをセット
            if ($model->cmd === 'chousasho') {
                //調査書データ取得
                $getRow = $db->getRow(knja127aQuery::getHexamEntremark($model), DB_FETCHMODE_ASSOC);

                //総合的な学習の時間　活動、評価
                if ($model->Properties["tyousasyoSougouHyoukaNentani"] !== '1') {
                    $RowH["TOTALSTUDYACT"]   = $getRow["TOTALSTUDYACT"];
                    $RowH["TOTALSTUDYVAL"]   = $getRow["TOTALSTUDYVAL"];
                } else {
                    //年単位の時
                    $RowH["TOTALSTUDYACT"]   = $getRow["TOTALSTUDYACT_YEAR"];
                    $RowH["TOTALSTUDYVAL"]   = $getRow["TOTALSTUDYVAL_YEAR"];
                }
                //総合所見(調査書データが3分割ではない時のみ取込可能)
                $Row["TOTALREMARK"]         = $getRow["TRAIN_REF"];
                //出欠の記録備考
                $Row["ATTENDREC_REMARK"]    = $getRow["ATTENDREC_REMARK"];
            }
        }

        //活動内容
        $arg["data"]["TOTALSTUDYACT"] = getTextOrArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_moji, $model->totalstudyact_gyou, $RowH["TOTALSTUDYACT"], $model);
        $arg["data"]["TOTALSTUDYACT_COMMENT"] = "(全角".$model->totalstudyact_moji."文字X".$model->totalstudyact_gyou."行まで)";

        //評価
        $arg["data"]["TOTALSTUDYVAL"] = getTextOrArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_moji, $model->totalstudyval_gyou, $RowH["TOTALSTUDYVAL"], $model);
        $arg["data"]["TOTALSTUDYVAL_COMMENT"] = "(全角".$model->totalstudyval_moji."文字X".$model->totalstudyval_gyou."行まで)";

        //入学時の障害の状態
        $arg["data"]["ENT_DISABILITY_REMARK"] = getTextOrArea($objForm, "ENT_DISABILITY_REMARK", $model->disability_moji, $model->disability_gyou, $RowD["ENT_DISABILITY_REMARK"], $model);
        $arg["data"]["ENT_DISABILITY_REMARK_COMMENT"] = "(全角".$model->disability_moji."文字X".$model->disability_gyou."行まで)";

        //総合所見
        $arg["data"]["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->totalremark_moji, $model->totalremark_gyou, $Row["TOTALREMARK"], $model);
        $arg["data"]["TOTALREMARK_COMMENT"] = "(全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで)";

        $disabled = ($model->schregno) ? "" : "disabled";

        //部活動選択ボタン
        $arg["button"]["btn_club"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TOTALREMARK", $disabled);

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $arg["button"]["btn_club_kirokubikou"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TOTALREMARK", $disabled);
        }

        //検定選択ボタン
        $arg["button"]["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TOTALREMARK", $disabled);

        //賞選択ボタン
        if ($model->Properties["useHyosyoSansyoButton_A"]) {
            $arg["button"]["btn_hyosyo"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TOTALREMARK", $disabled);
        }

        //出欠の記録備考取込
        if ($model->cmd === 'torikomi3') {
            $set_remark = "";
            $count = 0;
            $query = knja127aQuery::getSemesRemark($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($count == 0) {
                    $set_remark .= $row["REMARK1"];
                } else {
                    if ($row["REMARK1"] != "") {
                        $set_remark .= "／".$row["REMARK1"];
                    }
                }
                $count++;
            }
            $Row["ATTENDREC_REMARK"] = $set_remark;
        }

        //出欠の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $Row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //出欠備考参照ボタン
        $sdate = $model->exp_year.'-04-01';
        $edate = ((int)$model->exp_year+1).'-03-31';
        //和暦表示フラグ
        $warekiFlg = "";
        if ($model->Properties["useWarekiHyoji"] == "1") {
            $warekiFlg = "1";
        }

        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            //まとめ出欠備考を取込へ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $extra = "style=\"color:#1E90FF;font:bold\""; 
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = "";
            }
            if (!$model->schregno) {
                $extra .= "onclick=\"alert('データを指定してください。')\"";
            } else {
                //まとめ出欠備考を取込みへ変更する
                if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                    $extra .= " onclick=\"return btn_submit('torikomi3');\"";
                } else {
                    $extra = " onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
                }
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
        } else {
            if (!$model->schregno) {
                $extra = "onclick=\"alert('データを指定してください。')\"";
            } else {
                $extra = " onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }

        //調査書（進学用）の出欠の記録参照ボタン
        $extra = "onclick=\"loadwindow('../../X/KNJXATTEND_ENTREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,document.documentElement.scrollTop || document.body.scrollTop,360,180);return;\" style=\"width:230px;\"";
        $arg["TYOSASYO_SANSYO"] = KnjCreateBtn($objForm, "TYOSASYO_SANSYO", "調査書(進学用)の出欠の記録参照", $extra);

        //学校種別
        $schoolkind = $db->getOne(knja127aQuery::getSchoolKind($model));

        if ((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'A') {
            //更新ボタン
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
            //前の生徒へボタン
            $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('pre');\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
            //次の生徒へボタン
            $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('next');\"";
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
        } else {
            //更新ボタン
            $extra = "onclick=\"return btn_submit('update');\"";
            $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //更新後前の生徒へボタン
            $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');
        }

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //障害の状態ボタン
        $extra = "onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "障害の状態", $extra);

        /***********/
        /* CSV処理 */
        /***********/
        //各項目の総桁数をセット
        $fieldSize  = "";
        $fieldSize .= "TOTALSTUDYACT=".((int)$model->totalstudyact_moji * 3 * (int)$model->totalstudyact_gyou).",";
        $fieldSize .= "TOTALSTUDYVAL=".((int)$model->totalstudyval_moji * 3 * (int)$model->totalstudyval_gyou).",";
        $fieldSize .= "ENT_DISABILITY_REMARK=".((int)$model->disability_moji * 3 * (int)$model->disability_gyou).",";
        $fieldSize .= "TOTALREMARK=".((int)$model->totalremark_moji * 3 * (int)$model->totalremark_gyou).",";
        $fieldSize .= "ATTENDREC_REMARK=".((int)$model->attendrec_remark_moji * 3 * (int)$model->attendrec_remark_gyou).",";

        //各項目の行数をセット
        $gyouSize  = "";
        $gyouSize .= "TOTALSTUDYVAL=".$model->totalstudyval_gyou.",";
        $gyouSize .= "ENT_DISABILITY_REMARK=".$model->disability_gyou.",";
        $gyouSize .= "TOTALREMARK=".$model->totalremark_gyou.",";
        $gyouSize .= "ATTENDREC_REMARK=".$model->attendrec_remark_gyou.",";

        //セキュリティーチェック
        $securityCnt = $db->getOne(knja127aQuery::getSecurityHigh());
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //CSVボタン
            $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX180/knjx180index.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&SEND_PRGID=KNJA127A&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check1"] = KnjCreateBtn($objForm, "btn_check1", "※2 データ".$csvSetName, $extra);
            //CSVボタン
            $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX181/knjx181index.php?SEND_PRGID=KNJA127A&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check2"] = KnjCreateBtn($objForm, "btn_check2", "※1 データ".$csvSetName, $extra);
        }

        //プレビュー／印刷ボタン
        if ($model->Properties["sidouyourokuShokenPreview"] == '1') {
            $gradehrclass = $db->getOne(knja127aQuery::getGradeHrclass($model));
            $extra = "onclick=\"return newwin('".SERVLET_URL."', '".$gradehrclass."');\"";
            $arg["button"]["btn_print"] = KnjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJA127A");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");

        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize_disability", $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize_disability"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize_disability", $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize_disability"]);
        knjCreateHidden($objForm, "HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H_disability", $model->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H_disability"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalremarkSize_disability", $model->Properties["seitoSidoYoroku_dat_TotalremarkSize_disability"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_Attendrec_RemarkSize_disability", $model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize_disability"]);
        knjCreateHidden($objForm, "seitoSidoYorokuZaisekiMae", $model->Properties["seitoSidoYorokuZaisekiMae"]);
        knjCreateHidden($objForm, "seitoSidoYorokuKoumokuMei", $model->Properties["seitoSidoYorokuKoumokuMei"]);
        knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knja127aForm1.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = (int)$gyou % 5;
            $minus = ((int)$gyou / 5) > 1 ? ((int)$gyou / 5) * 6 : 5;
        }
        $height = (int)$gyou * 13.5 + ((int)$gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
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
