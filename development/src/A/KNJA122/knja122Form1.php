<?php

require_once('for_php7.php');

class knja122Form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knja122index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row  = knja122Query::getTrainRow($model, $db);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row  =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        /******************/
        /* テキストエリア */
        /******************/
        //学習･活動
        $extra = "style=\"height:95px;width:195px;\"";
        $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 6, 12, "hard", $extra, $row["TOTALSTUDYACT"]);
        //観点
        $extra = "style=\"height:95px;width:195px;\"";
        $arg["data"]["VIEWREMARK"]    = knjCreateTextArea($objForm, "VIEWREMARK",    6, 12, "hard", $extra, $row["VIEWREMARK"]);
        //評価
        $extra = "style=\"height:95px;width:375px;\"";
        $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 6, 25, "hard", $extra, $row["TOTALSTUDYVAL"]);
        //総合所見
        $extra = "style=\"height:95px;width:870px;\"";
        $arg["data"]["TOTALREMARK"]   = knjCreateTextArea($objForm, "TOTALREMARK",   6, 60, "hard", $extra, $row["TOTALREMARK"]);
        //出欠の記録備考
        $extra = "style=\"height:35px;width:310px;\"";
        $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 2, 20, "hard", $extra, $row["ATTENDREC_REMARK"]);

        /**********/
        /* ボタン */
        /**********/
        //行動の記録
        if (in_array($model->grade, array('01', '02', '03'))) {
            $extra = "onclick=\"return btn_submit('subform1');\"";
        } else {
            $extra = "disabled=\"disabled\"";
        }
        $arg["button"]["BEHAVIOR_DAT"] = knjCreateBtn($objForm, 'BEHAVIOR_DAT', '行動の記録', $extra);
        //特別活動の記録
        $extra = "onclick=\"return btn_submit('subform2');\"";
        $arg["button"]["SPECIAL_REMARKS"] = knjCreateBtn($objForm, 'SPECIAL_REMARKS', '特別活動の記録', $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');
        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);
        //CSV処理
        $fieldSize  = "TOTALSTUDYACT=216,";
        $fieldSize .= "TOTALSTUDYVAL=450,";
        $fieldSize .= "TOTALREMARK=1080,";
        $fieldSize .= "ATTENDREC_REMARK=120,";
        $fieldSize .= "VIEWREMARK=216,";
        $fieldSize .= "CLASSACT=210,";
        $fieldSize .= "STUDENTACT=210,";
        $fieldSize .= "CLUBACT=210,";
        $fieldSize .= "SCHOOLEVENT=210";
        //データCSV
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX180/knjx180index.php?FIELDSIZE={$fieldSize}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_check1"] = knjCreateBtn($objForm, 'btn_check1', 'データCSV', $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "GRADE"   , $model->grade);
        knjCreateHidden($objForm, "YEAR"    , $model->exp_year);
        knjCreateHidden($objForm, "SEMESTER", $model->exp_semester);

        //DB切断
        Query::dbCheckIn($db);
        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }
        $arg["IFRAME"] = VIEW::setIframeJs();
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knja122Form1.html", $arg);
    }
}
?>
