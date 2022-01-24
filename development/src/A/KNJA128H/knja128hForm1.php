<?php

require_once('for_php7.php');

class knja128hForm1
{

    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        // Add by HPA for title and textarea_cursor 2020-01-20 start
        if ($model->schregno != "" && $model->name != "") {
            $arg["TITLE"] = "".$model->schregno."". $model->name."の情報画面";
        } else {
            $arg["TITLE"] = "右結果画面";
        }
        if ($model->message915 == "") {
            echo "<script>sessionStorage.removeItem(\"KNJA128HForm1_CurrentCursor915\");</script>";
        } else {
            echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJA128HForm1_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJA128HForm1_CurrentCursor\");</script>";
            $model->message915 = "";
        }
        // Add by HPA for title and textarea_cursor 2020-01-31 end
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja128hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->cmd !== 'torikomi3') {
                $Row  = $db->getRow(knja128hQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
                $RowH = $db->getRow(knja128hQuery::getTrainHRow($model), DB_FETCHMODE_ASSOC);
                $RowD = $db->getRow(knja128hQuery::getTrainDetailRow($model), DB_FETCHMODE_ASSOC);
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

        //総合的な学習の時間のタイトルの設定(元々の処理はelse側の処理。2021年以降は上の条件の表示となる。2019、2020は過渡期。)
        $model->gradeCd = $model->schregno == "" ? "" : $db->getOne(knja128hQuery::getGradeCd($model));
        $startYear = 2019;
        if ($model->exp_year >= ($startYear + 2)
                   || ($model->exp_year == $startYear && $model->gradeCd == 1)
                   || ($model->exp_year == ($startYear + 1) && ($model->gradeCd == 1 || $model->gradeCd == 2))) {
            $title = "総<br>合<br>的<br>な<br>探<br>究<br>の<br>時<br>間<br>の<br>記<br>録<br>";
        } else {
            $title = "総<br>合<br>的<br>な<br>学<br>習<br>の<br>時<br>間<br>の<br>記<br>録<br>";
        }
        $arg["TOP"]["TOTAL_STUDY_TIME_TITLE"] = $title;

        //活動内容
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $arg["data"]["TOTALSTUDYACT"] = getTextOrArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_moji, $model->totalstudyact_gyou, $RowH["TOTALSTUDYACT"], $model, " aria-label = \"活動内容 全角".$model->totalstudyact_moji."文字X".$model->totalstudyact_gyou."行まで\" ");
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["TOTALSTUDYACT_COMMENT"] = "(全角".$model->totalstudyact_moji."文字X".$model->totalstudyact_gyou."行まで)";

        //評価
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $arg["data"]["TOTALSTUDYVAL"] = getTextOrArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_moji, $model->totalstudyval_gyou, $RowH["TOTALSTUDYVAL"], $model, " aria-label = \"評価 全角".$model->totalstudyval_moji."文字X".$model->totalstudyval_gyou."行まで\" ");
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["TOTALSTUDYVAL_COMMENT"] = "(全角".$model->totalstudyval_moji."文字X".$model->totalstudyval_gyou."行まで)";

        //特別活動所見
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $arg["data"]["SPECIALACTREMARK"] = getTextOrArea($objForm, "SPECIALACTREMARK", $model->specialactremark_moji, $model->specialactremark_gyou, $Row["SPECIALACTREMARK"], $model, " aria-label = \"特別活動所見 全角".$model->specialactremark_moji."文字X".$model->specialactremark_gyou."行まで\" ");
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["SPECIALACTREMARK_COMMENT"] = "(全角".$model->specialactremark_moji."文字X".$model->specialactremark_gyou."行まで)";

        //自立活動の記録
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $arg["data"]["INDEPENDENT_REMARK"] = getTextOrArea($objForm, "INDEPENDENT_REMARK", $model->indep_remark_moji, $model->indep_remark_gyou, $RowD["INDEPENDENT_REMARK"], $model, " aria-label = \"自立活動の記録 全角".$model->indep_remark_moji."文字X".$model->indep_remark_gyou."行まで\" ");
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["INDEPENDENT_REMARK_COMMENT"] = "(全角".$model->indep_remark_moji."文字X".$model->indep_remark_gyou."行まで)";

        //入学時の障害の状態
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $arg["data"]["ENT_DISABILITY_REMARK"] = getTextOrArea($objForm, "ENT_DISABILITY_REMARK", $model->disability_moji, $model->disability_gyou, $RowD["ENT_DISABILITY_REMARK"], $model, " aria-label = \"入学時の障害の状態 全角".$model->disability_moji."文字X".$model->disability_gyou."行まで\" ");
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["ENT_DISABILITY_REMARK_COMMENT"] = "(全角".$model->disability_moji."文字X".$model->disability_gyou."行まで)";

        //総合所見
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $arg["data"]["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->totalremark_moji, $model->totalremark_gyou, $Row["TOTALREMARK"], $model, " aria-label = \"総合所見 全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで\" ");
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["TOTALREMARK_COMMENT"] = "(全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで)";


        $disabled = ($model->schregno) ? "" : "disabled";

        //委員会選択ボタン
        $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREMARK", $disabled);


        //部活動選択ボタン（特別活動所見）
        $arg["button"]["btn_club_spe"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SPECIALACTREMARK", $disabled);

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
        /* Edit by HPA for current_cursor start 2020/01/20 */
        }

        //出欠の記録備考取込
        if ($model->cmd === 'torikomi3') {
            $set_remark = "";
            $count = 0;
            $query = knja128hQuery::getSemesRemark($model);
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
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $Row["ATTENDREC_REMARK"], $model, " aria-label = \"出欠の記録備考 全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで\" ");
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //出欠の記録参照ボタン
        $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);

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
                  /* Edit by HPA for current_cursor start 2020/01/20 */
                    $extra = "id= \"attend\" onclick=\"current_cursor('attend');loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
                    /* Edit by HPA for current_cursor end 2020/01/31 */
                }
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
        } else {
            if (!$model->schregno) {
                $extra = "onclick=\"alert('データを指定してください。')\"";
            } else {
                /* Edit by HPA for current_cursor start 2020/01/20 */
                $extra = "id= \"attend\" onclick=\"current_cursor('attend');loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&WAREKIFLG={$warekiFlg}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
                /* Edit by HPA for current_cursor end 2020/01/31 */
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }

        //成績参照ボタン
        $extra = " id=\"subform4\" onclick=\"current_cursor('subform4');return btn_submit('subform4');\" style=\"width:70px\"";
        $arg["SEISEKI_SANSYO"] = KnjCreateBtn($objForm, "SEISEKI_SANSYO", "成績参照", $extra);

        //学校種別
        $schoolkind = $db->getOne(knja128hQuery::getSchoolKind($model));

        if ((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'H') {
            //更新ボタン
            /* Edit by HPA for PC-talker 読み start 2020/01/20 */
            $extra = "disabled aria-label = \"更新\"";
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
            /* Edit by HPA for PC-talker 読み end 2020/01/31 */
            //前の生徒へボタン
            $extra = " id=\"btn_up_pre\" style=\"width:130px\" onclick=\"current_cursor('btn_up_pre');top.left_frame.nextStudentOnly('pre');\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
            //次の生徒へボタン
            $extra = "id=\"btn_up_next\" style=\"width:130px\" onclick=\"current_cursor('btn_up_next');top.left_frame.nextStudentOnly('next');\"";
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
        } else {
            //更新ボタン
            /* Edit by HPA for PC-talker 読み start 2020/01/20 */
            $extra = " id=\"btn_update\" onclick=\"current_cursor('btn_update');return btn_submit('update'); \"aria-label= \"更新\"";
            /* Edit by HPA for PC-talker 読み end 2020/01/31 */
            $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //更新後前の生徒へボタン
            // Add by HPA for PC-talker 2020-01-20 start
            $current_cursor = "current_cursor";
            $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update', $current_cursor);
            // Add by HPA for PC-talker 2020-01-31 end
        }

        //取消ボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset');return btn_submit('clear'); \"aria-label= \"取消\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "onclick=\"closeWin();\" aria-label= \"終了\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);


        /***********/
        /* CSV処理 */
        /***********/
        //各項目の総桁数をセット
        $fieldSize  = "";
        $fieldSize .= "TOTALSTUDYACT=".((int)$model->totalstudyact_moji * 3 * (int)$model->totalstudyact_gyou).",";
        $fieldSize .= "TOTALSTUDYVAL=".((int)$model->totalstudyval_moji * 3 * (int)$model->totalstudyval_gyou).",";
        $fieldSize .= "SPECIALACTREMARK=".((int)$model->specialactremark_moji * 3 * (int)$model->specialactremark_gyou).",";
        $fieldSize .= "INDEPENDENT_REMARK=".((int)$model->indep_remark_moji * 3 * (int)$model->indep_remark_gyou).",";
        $fieldSize .= "ENT_DISABILITY_REMARK=".((int)$model->disability_moji * 3 * (int)$model->disability_gyou).",";
        $fieldSize .= "TOTALREMARK=".((int)$model->totalremark_moji * 3 * (int)$model->totalremark_gyou).",";
        $fieldSize .= "ATTENDREC_REMARK=".((int)$model->attendrec_remark_moji * 3 * (int)$model->attendrec_remark_gyou).",";

        //各項目の行数をセット
        $gyouSize  = "";
        $gyouSize .= "TOTALSTUDYVAL=".$model->totalstudyval_gyou.",";
        $gyouSize .= "SPECIALACTREMARK=".$model->specialactremark_gyou.",";
        $gyouSize .= "INDEPENDENT_REMARK=".$model->indep_remark_gyou.",";
        $gyouSize .= "ENT_DISABILITY_REMARK=".$model->disability_gyou.",";
        $gyouSize .= "TOTALREMARK=".$model->totalremark_gyou.",";
        $gyouSize .= "ATTENDREC_REMARK=".$model->attendrec_remark_gyou.",";

        //セキュリティーチェック
        $securityCnt = $db->getOne(knja128hQuery::getSecurityHigh());
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //CSVボタン
            $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX180/knjx180index.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&SEND_PRGID=KNJA128H&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check1"] = KnjCreateBtn($objForm, "btn_check1", "※2 データ".$csvSetName, $extra);
            //CSVボタン
            $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX181/knjx181index.php?SEND_PRGID=KNJA128H&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check2"] = KnjCreateBtn($objForm, "btn_check2", "※1 データ".$csvSetName, $extra);
        }

        //プレビュー／印刷ボタン
        if ($model->Properties["sidouyourokuShokenPreview"] == '1') {
            $gradehrclass = $db->getOne(knja128hQuery::getGradeHrclass($model));
            $extra = "onclick=\"return newwin('".SERVLET_URL."', '".$gradehrclass."');\"";
            $arg["button"]["btn_print"] = KnjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJA128H");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");

        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize", $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_SpecialactremarkSize", $model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
        knjCreateHidden($objForm, "HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_H", $model->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_H"]);
        knjCreateHidden($objForm, "HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H", $model->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalremarkSize", $model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
        knjCreateHidden($objForm, "seitoSidoYoroku_dat_Attendrec_RemarkSize", $model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
        knjCreateHidden($objForm, "seitoSidoYorokuZaisekiMae", $model->Properties["seitoSidoYorokuZaisekiMae"]);
        knjCreateHidden($objForm, "seitoSidoYorokuKoumokuMei", $model->Properties["seitoSidoYorokuKoumokuMei"]);
        knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd == "clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knja128hForm1.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $setExtra = "")
{
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
        $extra = "$setExtra style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ((int)$moji * 2), true);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                   //部活動
            /* Edit by HPA for current_cursor start 2020/01/20 */
            $id = ($target == "SPECIALACTREMARK")? "club1":"club2";
            $extra = $disabled." id= $id onclick=\"current_cursor('$id');loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "committee") {       //委員会
            $extra = $disabled." id=\"committee\" onclick=\"current_cursor('committee'); loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } elseif ($div == "qualified") {       //検定
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $extra = $disabled." id=\"qualified\" onclick=\"current_cursor('qualified'); loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"";
        } elseif ($div == "hyosyo") {          //賞
            $extra = $disabled." id=\"hyosyo\" onclick=\"current_cursor('hyosyo'); loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } elseif ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." id=\"kirokubikou\" onclick=\"current_cursor('kirokubikou'); loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." id=\"syukketsukiroku\" onclick=\"current_cursor('syukketsukiroku'); loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
            /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
