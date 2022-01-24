<?php

require_once('for_php7.php');

class knja125mForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja125mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //東京仕様かを確認
        if ($model->getname === 'tokyoto') {
            $arg["tokyoto"] = '1';
        } else if ($model->getname === 'naraken') {
            $arg["naraken"] = '1';
        } else if ($model->getname === 'sagaken') {
            $arg["sagaken"] = '1';
        }

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knja125mQuery::getTrainRow($model->schregno, $model->exp_year, $model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //総合的な学習の時間のタイトルの設定(元々の処理はelse側の処理。2021年以降は上の条件の表示となる。2019、2020は過渡期。)
        $gradeCd = $model->grade == "" ? "" : $db->getOne(knja125mQuery::getGradeCd($model));
        if ($model->exp_year >= 2021
            || ($model->exp_year == 2019 && $gradeCd == 1)
            || ($model->exp_year == 2020 && ($gradeCd == 1 || $gradeCd == 2))) {
            $arg["TOP"]["TOTAL_STUDY_TIME_TITLE"] = "総<br>合<br>的<br>な<br>探<br>究<br>の<br>時<br>間<br>の<br>記<br>録<br>";
        } else {
            $arg["TOP"]["TOTAL_STUDY_TIME_TITLE"] = "総<br>合<br>的<br>な<br>学<br>習<br>の<br>時<br>間<br>の<br>記<br>録<br>";
        }

        //出席時数取込ボタン押下
        if($model->getname === 'sagaken'){
            if($model->cmd =="jisuu"){
                $data = $db->getRow(knja125mQuery::getSchAttendDat($model->schregno, $model->exp_year, $model), DB_FETCHMODE_ASSOC);
                $specialjisuu = $data["SPECIALJISUU"];
            }else{
                $specialjisuu = $row["SPECIALJISUU"];
            }
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //活動内容
        $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, (int)$model->totalstudyact_moji * 2 + 1, "soft", $extra, $row["TOTALSTUDYACT"]);
        $arg["data"]["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
        
        //評価
        $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, (int)$model->totalstudyval_moji * 2 + 1, "soft", $extra, $row["TOTALSTUDYVAL"]);
        $arg["data"]["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";

        if ($model->getname === 'tokyoto') {
            //奉仕の記録
            $extra = "style=\"height:35px;\"";
            $arg["data"]["TOTALSTUDYACT2"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT2", 2, 89, "soft", $extra, $row["TOTALSTUDYACT2"]);
            $arg["data"]["TOTALSTUDYVAL2"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL2", 2, 89, "soft", $extra, $row["TOTALSTUDYVAL2"]);
        
            //備考
            $extra = "style=\"height:75px;\"";
            $arg["data"]["CREDITREMARK"] = KnjCreateTextArea($objForm, "CREDITREMARK", 5, 89, "soft", $extra, $row["CREDITREMARK"]);
        } else if ($model->getname === 'naraken') {
            //奈良Time
            $extra = "";
            $arg["data"]["NARA_TIME"] = KnjCreateTextArea($objForm, "NARA_TIME", $model->nara_time_gyou, (int)$model->nara_time_moji * 2 + 1, "soft", $extra, $row["NARA_TIME"]);
            $arg["data"]["NARA_TIME_TYUI"] = "(全角{$model->nara_time_moji}文字{$model->nara_time_gyou}行まで)";
        } else if ($model->getname === 'sagaken') {
            //特別活動所見
            $extra = "";
            $arg["data"]["SPECIALJISUU"] = knjCreateTextArea($objForm, "SPECIALJISUU", $model->specialjisuu_gyou, (int)$model->specialjisuu_moji + 1, "soft", $extra, $specialjisuu);
            //出席時数取込ボタン
            $extra = "onclick=\"return btn_submit('jisuu');\"";
            $arg["button"]["btn_specialjisuu"] = KnjCreateBtn($objForm, "btn_specialjisuu", "出席時数取込", $extra);
        }

        //特別活動所見
        $extra = "";
        $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", $model->specialact_gyou, (int)$model->specialact_moji * 2 + 1, "soft", $extra, $row["SPECIALACTREMARK"]);
        $arg["data"]["SPECIALACTREMARK_TYUI"] = "(全角{$model->specialact_moji}文字{$model->specialact_gyou}行まで)";

        //総合所見
        $extra = "";
        $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", $model->totalremark_gyou, (int)$model->totalremark_moji * 2 + 1, "soft", $extra, $row["TOTALREMARK"]);
        $arg["data"]["TOTALREMARK_TYUI"] = "(全角{$model->totalremark_moji}文字{$model->totalremark_gyou}行まで)";

        //評価
        $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextBox($objForm, $row["ATTENDREC_REMARK"], "ATTENDREC_REMARK", 80, 80, "");

        $disabled = ($model->schregno) ? "" : "disabled";

        if ($model->Properties["useSpecialActivityMst"] == "1") {
            //特別活動選択ボタン
            $arg["button"]["btn_special_activity_sele"] = makeSelectBtn($objForm, $model, "specialActivityMst", "btn_specialActivityMst", "特別活動選択", "SPECIALACTREMARK", $disabled);
        }
        //委員会選択ボタン
        $arg["button"]["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SPECIALACTREMARK", "");

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
        }

        //調査書の出欠備考参照ボタン
        $extra = "onclick=\"loadwindow('../../X/KNJXATTEND_ENTREMARK/index.php?YEAR={$model->exp_year}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:230px;\"";
        $arg["button"]["TYOSASYO_SANSYO"] = KnjCreateBtn($objForm, "TYOSASYO_SANSYO", "調査書(進学用)の出欠の記録参照", $extra);

        //学校種別
        $schoolkind = $db->getOne(knja125mQuery::getSchoolKind($model));

        if((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'H'){
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
        $fieldSize  = "TOTALSTUDYACT=".((int)$model->totalstudyact_moji * 3 * (int)$model->totalstudyact_gyou).",";
        $fieldSize .= "TOTALSTUDYVAL=".((int)$model->totalstudyval_moji * 3 * (int)$model->totalstudyval_gyou).",";
        $gyouSize  = "TOTALSTUDYACT=".$model->totalstudyact_gyou.",";
        $gyouSize .= "TOTALSTUDYVAL=".$model->totalstudyval_gyou.",";
        if ($model->getname === 'tokyoto') {
            //奉仕の記録(HTRAINREMARK_HDAT)
            $fieldSize2 = "TOTALSTUDYACT2=264,";
            $gyouSize2  = "TOTALSTUDYACT2=2,";
            $fieldSize2 .= "TOTALSTUDYVAL2=264,";
            $gyouSize2  .= "TOTALSTUDYVAL2=2,";
            $fieldSize2 .= "CREDITREMARK=660,";
            $gyouSize2  .= "CREDITREMARK=5,";
        }
        if ($model->getname === 'sagaken') {
            $fieldSize .= "SPECIALJISUU=".((int)$model->specialjisuu_moji * (int)$model->specialjisuu_gyou).",";
        }
        $fieldSize .= "SPECIALACTREMARK=".((int)$model->specialact_moji * 3 * (int)$model->specialact_gyou).",";
        $fieldSize .= "TOTALREMARK=".((int)$model->totalremark_moji * 3 * (int)$model->totalremark_gyou).",";
        if ($model->getname === 'naraken') {
            $fieldSize .= "NARA_TIME=".((int)$model->nara_time_moji * 3 * (int)$model->nara_time_gyou).",";
        }
        $fieldSize .= "ATTENDREC_REMARK=120";
        $gyouSize .= "SPECIALACTREMARK=".$model->specialact_gyou.",";
        $gyouSize .= "TOTALREMARK=".$model->totalremark_gyou.",";
        if ($model->getname === 'naraken') {
            $gyouSize  .= "NARA_TIME=".$model->nara_time_gyou.",";
        }
        $gyouSize .= "ATTENDREC_REMARK=1";
        

        //ＣＳＶ出力ボタン
        $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX_A125M/knjx_a125mindex.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        if ($model->getname === 'tokyoto') {
            $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "※1 データＣＳＶ出力", $extra);
            
            $extra2 = " onClick=\" wopen('".REQUESTROOT."/X/KNJX_A125M_HDAT/knjx_a125m_hdatindex.php?FIELDSIZE=".$fieldSize2."&GYOUSIZE=".$gyouSize2."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_csv2"] = KnjCreateBtn($objForm, "btn_csv2", "※2 データＣＳＶ出力", $extra2);
        } else {
            $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja125mForm1.html", $arg);
    }
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
        } else if ($div == "specialActivityMst") { //特別活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SPECIAL_ACTIVITY_SELECT/knjx_special_activity_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
?>
