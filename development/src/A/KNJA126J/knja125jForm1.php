<?php

require_once('for_php7.php');

class knja125jForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja125jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knja125jQuery::getTrainRow($model, ""), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //総合所見及び指導上参考となる諸事項
        $extra = "style=\"height:150px;\"";
        $arg["data"]["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->totalremark_moji, $model->totalremark_gyou, $row["TOTALREMARK"], $model);
        $arg["data"]["TOTALREMARK_COMMENT"] = "(全角".$model->totalremark_moji."文字X".$model->totalremark_gyou."行まで)";

        //学習活動
        $extra = "style=\"height:118px;\"onkeyup=\"charCount(this.value, 8, (5 * 2), true);\"";
        $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 8, 11, "soft", $extra, $row["TOTALSTUDYACT"]);

        //観点
        $extra = "style=\"height:118px;\"onkeyup=\"charCount(this.value, 8, (10 * 2), true);\"";
        $arg["data"]["VIEWREMARK"] = knjCreateTextArea($objForm, "VIEWREMARK", 8, 21, "soft", $extra, $row["VIEWREMARK"]);

        //評価
        $extra = "style=\"height:118px;\"onkeyup=\"charCount(this.value, 8, (15 * 2), true);\"";
        $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, 31, "soft", $extra, $row["TOTALSTUDYVAL"]);

        //行動の記録・特別活動の記録ボタン
        $extra = "onclick=\"return btn_submit('form2');\"";
        $arg["button"]["btn_form2"] = KnjCreateBtn($objForm, "btn_form2", "行動の記録・特別活動の記録", $extra);

        //出欠の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //出欠の記録参照ボタン
        $extra = "onclick=\"return btn_submit('subform2');\"";
        $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "出欠の記録参照", $extra);

        //通知表所見参照ボタン
        $extra = "onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "通知表所見参照", $extra);

        //学校種別
        $schoolkind = $db->getOne(knja125jQuery::getSchoolKind($model));

        if((AUTHORITY < DEF_UPDATE_RESTRICT) || $schoolkind != 'J'){
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

        //既入力内容参照（総合的な学習時間）
        $extra = " onclick=\"return btn_submit('shokenlist0');\"";
        $arg["button"]["shokenlist0"] = knjCreateBtn($objForm, "shokenlist0", "既入力内容の参照", $extra);
        //既入力内容参照（総合所見）
        $extra = " onclick=\"return btn_submit('shokenlist2');\"";
        $arg["button"]["shokenlist2"] = knjCreateBtn($objForm, "shokenlist2", "既入力内容の参照", $extra);
        //既入力内容参照（出欠の記録備考）
        $extra = " onclick=\"return btn_submit('shokenlist3');\"";
        $arg["button"]["shokenlist3"] = knjCreateBtn($objForm, "shokenlist3", "既入力内容の参照", $extra);

        //CSV処理
        $fieldSize  = "TOTALSTUDYACT=120,";
        $fieldSize .= "VIEWREMARK=240,";
        $fieldSize .= "TOTALSTUDYVAL=360,";
        $fieldSize .= "TOTALREMARK=".((int)$model->totalremark_moji * (int)$model->totalremark_gyou * 3).",";
        $fieldSize .= "ATTENDREC_REMARK=".((int)$model->attendrec_remark_moji * (int)$model->attendrec_remark_gyou * 3).",";
        $fieldSize .= "SPECIALACTREMARK=510";

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A125J/knjx_a125jindex.php?FIELDSIZE=".$fieldSize."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //hidden
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEMES_CNT", $model->control["学期数"]);

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
        View::toHTML($model, "knja125jForm1.html", $arg);
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
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
