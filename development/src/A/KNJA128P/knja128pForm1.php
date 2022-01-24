<?php

require_once('for_php7.php');

class knja128pForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        // Add by HPA for title and textarea_cursor 2020-01-20 start
        if ($model->schregno != "" && $model->name != "") {
            $arg["TITLE"] = "".$model->schregno."". $model->name."の情報画面";
        } else {
            $arg["TITLE"] = "右情報画面";
        }
        if ($model->message915 == "") {
            echo "<script>sessionStorage.removeItem(\"KNJA128PForm1_CurrentCursor915\");</script>";
        } else {
            echo "<script>var x= '".$model->message915."';
              sessionStorage.setItem(\"KNJA128PForm1_CurrentCursor915\", x);
              sessionStorage.removeItem(\"KNJA128PForm1_CurrentCursor\");</script>";
            $model->message915 = "";
        }
        // Add by HPA for title and textarea_cursor 2020-01-31 end

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja128pindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knja128pQuery::getTrainRemarkData($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        if ($model->schregno == "") {
            $disabled = " disabled";
        } else {
            $disabled = "";
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        $arg['HANDICAP'] = ($db->getOne(knja128pQuery::getHandicap($model)) == '002') ? '(準ずる教育)' : '';

        $fdisabled = $model->useForeignlangActFlg ? '' : ' disabled="disabled"';
        if ($model->is_gaikokugo1) {
            $arg["GAIKOKUGO_1"] = "1";
            $moji = $model->foreignlangact_2020_moji;
            $gyo = $model->foreignlangact_2020_gyou;
            //知識・技能　思考・判断・表現　主体的に学習に取り組む態度
            $extra = "style=\"height:62px;\" aria-label = \"知識・技能　思考・判断・表現　主体的に学習に取り組む態度 全角${moji}文字X${gyo}行まで\"" . $fdisabled;
            $arg["data"]["FOREIGNLANGACT1_2"] = knjCreateTextArea($objForm, "FOREIGNLANGACT1_2", $gyo, $moji * 2 + 1, "soft", $extra, $row["FOREIGNLANGACT1"]);
            $arg["data"]["FOREIGNLANGACT1_2_COMMENT"] = "(全角".$moji."文字X".$gyo."行まで)";
        } else {
            $arg["GAIKOKUGO_3"] = "1";
            //コミュニケーションへの関心・意欲・態度
            $extra = "style=\"height:62px;\" aria-label = \"コミュニケーションへの関心・意欲・態度 全角11文字X4行まで\"" . $fdisabled;
            $arg["data"]["FOREIGNLANGACT1"] = knjCreateTextArea($objForm, "FOREIGNLANGACT1", 4, 23, "soft", $extra, $row["FOREIGNLANGACT1"]);

            //外国語への慣れ親しみ
            $extra = "style=\"height:62px;\" aria-label = \"外国語への慣れ親しみ 全角11文字X4行まで\"" . $fdisabled;
            $arg["data"]["FOREIGNLANGACT2"] = knjCreateTextArea($objForm, "FOREIGNLANGACT2", 4, 23, "soft", $extra, $row["FOREIGNLANGACT2"]);

            //言語や文化に関する気付き
            $extra = "style=\"height:62px;\" aria-label = \"言語や文化に関する気付き 全角11文字X4行まで\"" . $fdisabled;
            $arg["data"]["FOREIGNLANGACT3"] = knjCreateTextArea($objForm, "FOREIGNLANGACT3", 4, 23, "soft", $extra, $row["FOREIGNLANGACT3"]);
        }

        //自立活動の記録
        $extra = "aria-label =\"自立活動の記録 全角".$model->indep_remark_moji."文字X".$model->indep_remark_gyou."行まで\"";
        $arg["data"]["INDEPENDENT_REMARK"] = getTextOrArea($objForm, "INDEPENDENT_REMARK", $model->indep_remark_moji, $model->indep_remark_gyou, $row["INDEPENDENT_REMARK"], $model, $extra);
        $arg["data"]["INDEPENDENT_REMARK_COMMENT"] = "(全角".$model->indep_remark_moji."文字X".$model->indep_remark_gyou."行まで)";

        //総合所見及び指導上参考となる諸事項
        $extra = "aria-label =\"総合所見及び指導上参考となる諸事項 全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで\"";
        $arg["data"]["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->totalremark_moji, $model->totalremark_gyou, $row["TOTALREMARK"], $model, $extra);
        $arg["data"]["TOTALREMARK_COMMENT"] = "(全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで)";


        $tdisabled = $model->useTotalStudyValFlg ? '' : ' disabled="disabled"';
        //学習活動
        $extra = "aria-label =\"学習活動 全角".$model->totalstudyact_moji."文字X".$model->totalstudyact_gyou."行まで\"" . $tdisabled ;
        $arg["data"]["TOTALSTUDYACT"] = getTextOrArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_moji, $model->totalstudyact_gyou, $row["TOTALSTUDYACT"], $model, $extra);
        $arg["data"]["TOTALSTUDYACT_COMMENT"] = "(全角".$model->totalstudyact_moji."文字X".$model->totalstudyact_gyou."行まで)";

        //観点
        $extra = "aria-label =\"観点 全角".$model->viewremark_moji."文字X".$model->viewremark_gyou."行まで\"" . $tdisabled ;
        $arg["data"]["VIEWREMARK"] = getTextOrArea($objForm, "VIEWREMARK", $model->viewremark_moji, $model->viewremark_gyou, $row["VIEWREMARK"], $model, $extra);
        $arg["data"]["VIEWREMARK_COMMENT"] = "(全角".$model->viewremark_moji."文字X".$model->viewremark_gyou."行まで)";

        //評価
        $extra = "aria-label =\"評価 全角".$model->totalstudyval_moji."文字X".$model->totalstudyval_gyou."行まで\"" . $tdisabled ;
        $arg["data"]["TOTALSTUDYVAL"] = getTextOrArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_moji, $model->totalstudyval_gyou, $row["TOTALSTUDYVAL"], $model, $extra);
        $arg["data"]["TOTALSTUDYVAL_COMMENT"] = "(全角".$model->totalstudyval_moji."文字X".$model->totalstudyval_gyou."行まで)";

        //入学時の障害の状態
        $extra = "aria-label =\"入学時の障害の状態 全角".$model->disability_moji."文字X".$model->disability_gyou."行まで\"";
        $gyo = ($model->disability_gyou > 7) ? "7" : $model->disability_gyou;
        $arg["data"]["ENT_DISABILITY_REMARK"] = getTextOrArea($objForm, "ENT_DISABILITY_REMARK", $model->disability_moji, $gyo, $row["ENT_DISABILITY_REMARK"], $model, $extra);
        $arg["data"]["ENT_DISABILITY_REMARK_COMMENT"] = "(全角".$model->disability_moji."文字X".$model->disability_gyou."行まで)";

        //行動の記録・特別活動の記録ボタン
        $extra = "id= \"btn_form2\" onclick=\"current_cursor('btn_form2');return btn_submit('form2');\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "行動の記録・特別活動の記録", $extra);

        //出欠の記録備考
        $extra = "aria-label =\"出欠の記録備考 全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで\"";
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $row["ATTENDREC_REMARK"], $model, $extra);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //出欠の記録参照ボタン
        $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);

        //学校種別
        $schoolkind = $db->getOne(knja128pQuery::getSchoolKind($model));

        if ((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'P') {
            //更新ボタン
            $extra ="disabled aria-label = \"更新\"";
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //前の生徒へボタン
            $extra = " id=\"btn_up_pre\" style=\"width:130px\" onclick=\"current_cursor('btn_up_pre');top.left_frame.nextStudentOnly('pre');\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
            //次の生徒へボタン
            $extra = " id=\"btn_up_next\" style=\"width:130px\" onclick=\"current_cursor('btn_up_next');top.left_frame.nextStudentOnly('next');\"";
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
        } else {
            //更新ボタン
            $extra = "id=\"update\" aria-label = \"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"";
            $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
            //更新後前の生徒へボタン
            // Add by HPA for PC-talker 2020-01-20 start
            $current_cursor = "current_cursor";
            $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update', $current_cursor);
            // Add by HPA for PC-talker 2020-01-31 end
        }

        //道徳
        $extra = "aria-label = \"道徳 全角".$model->foreignlangact4_moji."文字X".$model->foreignlangact4_gyou."行まで\"";
        $arg["data"]["FOREIGNLANGACT4"] = getTextOrArea($objForm, "FOREIGNLANGACT4", $model->foreignlangact4_moji, $model->foreignlangact4_gyou, $row["FOREIGNLANGACT4"], $model, $extra);
        $arg["data"]["FOREIGNLANGACT4_COMMENT"] = "(全角".$model->foreignlangact4_moji."文字X".$model->foreignlangact4_gyou."行まで)";

        //定型文選択ボタン
        $extra = "id=\"close\" onclick=\"current_cursor('');return btn_submit('teikei');\"";
        //$arg["button"]["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);

        //教育支援計画参照ボタン
        $extra = " onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SIENKEIKAKU/knjx_sienkeikakuindex.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}&NAME={$model->name}',0,document.documentElement.scrollTop || document.body.scrollTop,350,450);\"";
        $arg["button"]["btn_sienkeikaku"] = KnjCreateBtn($objForm, "btn_sienkeikaku", "教育支援計画参照", $extra.$disabled);

        //取消ボタン
        $extra = "id=\"clear\" aria-label = \"取消\" onclick=\"current_cursor('clear');return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "id=\"close\" aria-label = \"終了\" onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //CSV処理
        $fieldSize  = "FOREIGNLANGACT1=132,";
        $fieldSize .= "FOREIGNLANGACT2=132,";
        $fieldSize .= "FOREIGNLANGACT3=132,";
        $fieldSize .= "FOREIGNLANGACT1_2=".(32 * 4 * 3).",";
        $fieldSize .= "TOTALSTUDYACT=".((int)$model->totalstudyact_moji * (int)$model->totalstudyact_gyou * 3).",";
        $fieldSize .= "VIEWREMARK=".((int)$model->viewremark_moji * (int)$model->viewremark_gyou * 3).",";
        $fieldSize .= "TOTALSTUDYVAL=".((int)$model->totalstudyval_moji * (int)$model->totalstudyval_gyou * 3).",";
        $fieldSize .= "FOREIGNLANGACT4=".((int)$model->foreignlangact4_moji * (int)$model->foreignlangact4_gyou * 3).",";
        $fieldSize .= "INDEPENDENT_REMARK=".((int)$model->indep_remark_moji * (int)$model->indep_remark_gyou * 3).",";
        $fieldSize .= "ENT_DISABILITY_REMARK=".((int)$model->disability_moji * (int)$model->disability_gyou * 3).",";
        $fieldSize .= "TOTALREMARK=".((int)$model->totalremark_moji * (int)$model->totalremark_gyou * 3).",";
        $fieldSize .= "ATTENDREC_REMARK=".((int)$model->attendrec_remark_moji * (int)$model->attendrec_remark_gyou * 3).",";
        $fieldSize .= "SPECIALACTREMARK=".((int)$model->specialactremark_moji * (int)$model->specialactremark_gyou * 3);

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A128P/knjx_a128pindex.php?FIELDSIZE=".$fieldSize."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");

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

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja128pForm1.html", $arg);
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
        $extra = " $setExtra style=\"height:".$height."px;\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = " $setExtra onkeypress=\"btn_keypress();\"";
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
        if ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." id=\"syukketsukiroku\" onclick=\"current_cursor('syukketsukiroku');loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
