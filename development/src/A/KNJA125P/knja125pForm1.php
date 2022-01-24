<?php

require_once('for_php7.php');

class knja125pForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja125pindex.php", "", "edit");

        $arg["fep"] = $model->Properties["FEP"];

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->cmd !== 'value_set') {
                $row = $db->getRow(knja125pQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);
                $model->field["FOREIGNLANGACT4_BG_COLOR_FLG"] = "";
            } else {
                $row =& $model->field;
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        // 外国語活動の記録
        if ($model->is_gaikokugo1) {
            $arg["GAIKOKUGO_1"] = "1";
            $moji = 32;
            $gyo = 4;
            //知識・技能 思考・判断・表現 主体的に学習に取り組む態度
            $extra = "id=\"FOREIGNLANGACT1_2\" style=\"height:62px;\"";
            $inputGradeCd = $model->isOsakashinnai ? array(1, 2) : array(3, 4);
            if (!in_array((int) $model->gradeCd, $inputGradeCd)) {
                $extra .= " disabled ";
            }
            $arg["data"]["FOREIGNLANGACT1_2"] = knjCreateTextArea($objForm, "FOREIGNLANGACT1_2", $gyo, (int)$moji * 2 + 1, "soft", $extra, $row["FOREIGNLANGACT1"]);
            knjCreateHidden($objForm, "FOREIGNLANGACT1_2_KETA", (int)$moji * 2);
            knjCreateHidden($objForm, "FOREIGNLANGACT1_2_GYO", $gyo);
            KnjCreateHidden($objForm, "FOREIGNLANGACT1_2_STAT", "statusarea1");
        } else {
            $arg["GAIKOKUGO_3"] = "1";

            //コミュニケーションへの関心・意欲・態度
            $extra = "id=\"FOREIGNLANGACT1\" style=\"height:62px;\"";
            $arg["data"]["FOREIGNLANGACT1"] = knjCreateTextArea($objForm, "FOREIGNLANGACT1", 4, 21, "soft", $extra, $row["FOREIGNLANGACT1"]);
            knjCreateHidden($objForm, "FOREIGNLANGACT1_KETA", 20);
            knjCreateHidden($objForm, "FOREIGNLANGACT1_GYO", 4);
            KnjCreateHidden($objForm, "FOREIGNLANGACT1_STAT", "statusarea1");

            //外国語への慣れ親しみ
            $extra = "id=\"FOREIGNLANGACT2\" style=\"height:62px;\"";
            $arg["data"]["FOREIGNLANGACT2"] = knjCreateTextArea($objForm, "FOREIGNLANGACT2", 4, 21, "soft", $extra, $row["FOREIGNLANGACT2"]);
            knjCreateHidden($objForm, "FOREIGNLANGACT2_KETA", 20);
            knjCreateHidden($objForm, "FOREIGNLANGACT2_GYO", 4);
            KnjCreateHidden($objForm, "FOREIGNLANGACT2_STAT", "statusarea3");

            //言語や文化に関する気付き
            $extra = "id=\"FOREIGNLANGACT3\" style=\"height:62px;\"";
            $arg["data"]["FOREIGNLANGACT3"] = knjCreateTextArea($objForm, "FOREIGNLANGACT3", 4, 21, "soft", $extra, $row["FOREIGNLANGACT3"]);
            knjCreateHidden($objForm, "FOREIGNLANGACT3_KETA", 20);
            knjCreateHidden($objForm, "FOREIGNLANGACT3_GYO", 4);
            KnjCreateHidden($objForm, "FOREIGNLANGACT3_STAT", "statusarea5");
        }

        //総合所見及び指導上参考となる諸事項
        $arg["data"]["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->totalremark_moji, $model->totalremark_gyou, $row["TOTALREMARK"], $model, "");
        $arg["data"]["TOTALREMARK_COMMENT"] = "(全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで)";
        knjCreateHidden($objForm, "TOTALREMARK_KETA", (int)$model->totalremark_moji * 2);
        knjCreateHidden($objForm, "TOTALREMARK_GYO", $model->totalremark_gyou);
        KnjCreateHidden($objForm, "TOTALREMARK_STAT", "statusarea7");

        //学習活動
        $arg["data"]["TOTALSTUDYACT"] = getTextOrArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_moji, $model->totalstudyact_gyou, $row["TOTALSTUDYACT"], $model, "");
        $arg["data"]["TOTALSTUDYACT_COMMENT"] = "(全角".$model->totalstudyact_moji."文字X".$model->totalstudyact_gyou."行まで)";
        knjCreateHidden($objForm, "TOTALSTUDYACT_KETA", (int)$model->totalstudyact_moji * 2);
        knjCreateHidden($objForm, "TOTALSTUDYACT_GYO", $model->totalstudyact_gyou);
        KnjCreateHidden($objForm, "TOTALSTUDYACT_STAT", "statusarea2");

        //観点
        $arg["data"]["VIEWREMARK"] = getTextOrArea($objForm, "VIEWREMARK", $model->viewremark_moji, $model->viewremark_gyou, $row["VIEWREMARK"], $model, "");
        $arg["data"]["VIEWREMARK_COMMENT"] = "(全角".$model->viewremark_moji."文字X".$model->viewremark_gyou."行まで)";
        knjCreateHidden($objForm, "VIEWREMARK_KETA", (int)$model->viewremark_moji * 2);
        knjCreateHidden($objForm, "VIEWREMARK_GYO", $model->viewremark_gyou);
        KnjCreateHidden($objForm, "VIEWREMARK_STAT", "statusarea4");

        //評価
        $arg["data"]["TOTALSTUDYVAL"] = getTextOrArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_moji, $model->totalstudyval_gyou, $row["TOTALSTUDYVAL"], $model, "");
        $arg["data"]["TOTALSTUDYVAL_COMMENT"] = "(全角".$model->totalstudyval_moji."文字X".$model->totalstudyval_gyou."行まで)";
        knjCreateHidden($objForm, "TOTALSTUDYVAL_KETA", (int)$model->totalstudyval_moji * 2);
        knjCreateHidden($objForm, "TOTALSTUDYVAL_GYO", $model->totalstudyval_gyou);
        KnjCreateHidden($objForm, "TOTALSTUDYVAL_STAT", "statusarea6");

        //行動の記録・特別活動の記録ボタン
        $extra = "onclick=\"return btn_submit('form2');\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "行動の記録・特別活動の記録", $extra);

        //出欠の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $row["ATTENDREC_REMARK"], $model, "");
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";
        knjCreateHidden($objForm, "ATTENDREC_REMARK_KETA", (int)$model->attendrec_remark_moji * 2);
        knjCreateHidden($objForm, "ATTENDREC_REMARK_GYO", $model->attendrec_remark_gyou);
        KnjCreateHidden($objForm, "ATTENDREC_REMARK_STAT", "statusarea8");

        //出欠の記録参照ボタン
        $extra = "onclick=\"return btn_submit('subform2');\"";
        $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "出欠の記録参照", $extra);

        //年間出欠備考選択ボタン
        if ($model->Properties["useReasonCollectionBtn"] == 1) {
            $arg["btn_reason_collection_select"] = makeSelectBtn($objForm, $model, "reason_collection", "btn_reason_collection_select", "年間出欠備考選択", "ATTENDREC_REMARK", "");
            $arg["REASON_COLLECTION_SELECT"] = 1;
        }

        //道徳
        if ($model->field["FOREIGNLANGACT4_BG_COLOR_FLG"]) {
            $extra = " background-color:#FFCCFF ";
        } else {
            $extra = "";
        }
        $arg["data"]["FOREIGNLANGACT4"] = getTextOrArea($objForm, "FOREIGNLANGACT4", $model->foreignlangact4_moji, $model->foreignlangact4_gyou, $row["FOREIGNLANGACT4"], $model, $extra);
        $arg["data"]["FOREIGNLANGACT4_COMMENT"] = "(全角".$model->foreignlangact4_moji."文字X".$model->foreignlangact4_gyou."行まで)";
        knjCreateHidden($objForm, "FOREIGNLANGACT4_KETA", (int)$model->foreignlangact4_moji * 2);
        knjCreateHidden($objForm, "FOREIGNLANGACT4_GYO", $model->foreignlangact4_gyou);
        KnjCreateHidden($objForm, "FOREIGNLANGACT4_STAT", "statusarea9");

        //定型文選択ボタン
        $extra = "onclick=\"return btn_submit('teikei');\"";
        $arg["button"]["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);

        //通知表所見参照ボタン
        $extra = "onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "通知表所見参照", $extra);
        //プロパティ（tutihyosanshouHyoujiFlg_P = 1）の時、非表示とする。
        $arg["tutihyosanshouHyoujiFlg_P"] = ($model->Properties["tutihyosanshouHyoujiFlg_P"] == '1') ? "" : "on";

        //学校種別
        $schoolkind = $db->getOne(knja125pQuery::getSchoolKind($model));

        if ((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'P') {
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

        //CSV処理
        $fieldSize  = "FOREIGNLANGACT1=120,";
        $fieldSize .= "FOREIGNLANGACT2=120,";
        $fieldSize .= "FOREIGNLANGACT3=120,";
        $fieldSize .= "FOREIGNLANGACT1_2=".(32 * 4 * 3).",";
        $fieldSize .= "FOREIGNLANGACT4=".((int)$model->foreignlangact4_moji * (int)$model->foreignlangact4_gyou * 3).",";
        $fieldSize .= "TOTALSTUDYACT=".((int)$model->totalstudyact_moji * (int)$model->totalstudyact_gyou * 3).",";
        $fieldSize .= "VIEWREMARK=".((int)$model->viewremark_moji * (int)$model->viewremark_gyou * 3).",";
        $fieldSize .= "TOTALSTUDYVAL=".((int)$model->totalstudyval_moji * (int)$model->totalstudyval_gyou * 3).",";
        $fieldSize .= "TOTALREMARK=".((int)$model->totalremark_moji * (int)$model->totalremark_gyou * 3).",";
        $fieldSize .= "ATTENDREC_REMARK=".((int)$model->attendrec_remark_moji * (int)$model->attendrec_remark_gyou * 3).",";
        $fieldSize .= "SPECIALACTREMARK=510";

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A125P/knjx_a125pindex.php?FIELDSIZE=".$fieldSize."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."&EXP_YEAR=".$model->exp_year."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //部活動選択ボタン
        $arg["button"]["btn_club"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "TOTALREMARK", "");
        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $arg["button"]["btn_club_kirokubikou"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "TOTALREMARK", "");
        }
        //委員会選択ボタン
        $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "TOTALREMARK", "");
        //検定選択ボタン
        $arg["button"]["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "TOTALREMARK", "");
        //賞選択ボタン
        if ($model->Properties["useHyosyoSansyoButton_J"]) {
            $arg["button"]["btn_hyosyo"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "TOTALREMARK", "");
        }

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEMES_CNT", $model->control["学期数"]);
        knjCreateHidden($objForm, "FOREIGNLANGACT4_BG_COLOR_FLG", $model->field["FOREIGNLANGACT4_BG_COLOR_FLG"]);
        knjCreateHidden($objForm, "useKnja125pBehaviorSemesMst", $model->Properties["useKnja125pBehaviorSemesMst"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning)== 0 && $model->cmd !="clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knja125pForm1.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $bgColor)
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
        $extra = "id=\"{$name}\" style=\"height:".$height."px;".$bgColor."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "id=\"{$name}\" onkeypress=\"btn_keypress();\"";
        if ($bgColor) {
            $extra .= " style=\"".$bgColor."\"";
        }
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
            $extra = $disabled." onclick=\"if(dataSelectCheck()){loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);}\"";
        } elseif ($div == "committee") {       //委員会
            $extra = $disabled." onclick=\"if(dataSelectCheck()){loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);}\"";
        } elseif ($div == "qualified") {       //検定
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $extra = $disabled." onclick=\"if(dataSelectCheck()){loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);}\"";
        } elseif ($div == "hyosyo") {          //賞
            $extra = $disabled." onclick=\"if(dataSelectCheck()){loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);}\"";
        } elseif ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." onclick=\"if(dataSelectCheck()){loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);}\"";
        } elseif ($div == "reason_collection") {   //年間出欠備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
