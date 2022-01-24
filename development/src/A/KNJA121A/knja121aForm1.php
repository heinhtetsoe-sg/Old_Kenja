<?php

require_once('for_php7.php');

class knja121aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knja121aindex.php", "", "edit");
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = knja121aQuery::getTrainRow($model->schregno, $model->exp_year);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        
        //学習活動
        $arg["data"]["TOTALSTUDYACT"] = getTextOrArea($objForm, "TOTALSTUDYACT", $model->getPro["TOTALSTUDYACT"]["moji"], $model->getPro["TOTALSTUDYACT"]["gyou"], $row["TOTALSTUDYACT"], $model);
        $arg["data"]["TOTALSTUDYACT_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYACT"]["moji"]."文字X".$model->getPro["TOTALSTUDYACT"]["gyou"]."行まで)";

        //観点
        $arg["data"]["VIEWREMARK"] = getTextOrArea($objForm, "VIEWREMARK", $model->getPro["VIEWREMARK"]["moji"], $model->getPro["VIEWREMARK"]["gyou"], $row["VIEWREMARK"], $model);
        $arg["data"]["VIEWREMARK_COMMENT"] = "(全角".$model->getPro["VIEWREMARK"]["moji"]."文字X".$model->getPro["VIEWREMARK"]["gyou"]."行まで)";

        //評価
        $arg["data"]["TOTALSTUDYVAL"] = getTextOrArea($objForm, "TOTALSTUDYVAL", $model->getPro["TOTALSTUDYVAL"]["moji"], $model->getPro["TOTALSTUDYVAL"]["gyou"], $row["TOTALSTUDYVAL"], $model);
        $arg["data"]["TOTALSTUDYVAL_COMMENT"] = "(全角".$model->getPro["TOTALSTUDYVAL"]["moji"]."文字X".$model->getPro["TOTALSTUDYVAL"]["gyou"]."行まで)";

        //出欠の記録備考
        $arg["data"]["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->getPro["ATTENDREC_REMARK"]["moji"], $model->getPro["ATTENDREC_REMARK"]["gyou"], $row["ATTENDREC_REMARK"], $model);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->getPro["ATTENDREC_REMARK"]["moji"]."文字X".$model->getPro["ATTENDREC_REMARK"]["gyou"]."行まで)";

        //総合所見
        $arg["data"]["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->getPro["TOTALREMARK"]["moji"], $model->getPro["TOTALREMARK"]["gyou"], $row["TOTALREMARK"], $model);
        $arg["data"]["TOTALREMARK_COMMENT"] = "(全角".$model->getPro["TOTALREMARK"]["moji"]."文字X".$model->getPro["TOTALREMARK"]["gyou"]."行まで)";

        $arg["IFRAME"] = VIEW::setIframeJs();
        //ボタン
        //通知票所見参照ボタンを非表示とする。(近大中学のみ)---2006/03/24
        $cnt = knja121aQuery::getKindaiJudgment();
        if ($cnt > 0) {
        } else {

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_popup",
                            "value"       => "通知表所見参照",
                            "extrahtml"   => "onclick=\"return btn_submit('subform1');\"" ));
        $arg["button"]["btn_popup"] = $objForm->ge("btn_popup");

        }

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_popup2",
                            "value"       => "行動の記録・特別活動の記録",
                            "extrahtml"   => "onclick=\"return btn_submit('form2');\"" ));
        $arg["button"]["btn_popup2"] = $objForm->ge("btn_popup2");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

/*        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の生徒へ",
                            "extrahtml" =>  " onclick=\"return updateNextStudent('".$model->schregno ."', 1);\" style=\"width:130px\""));

        $arg["button"]["btn_up_pre"]    = $objForm->ge("btn_up_pre");

        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の生徒へ",
                            "extrahtml" =>  " onclick=\"return updateNextStudent('".$model->schregno ."', 0);\" style=\"width:130px\""));

        $arg["button"]["btn_up_next"]    = $objForm->ge("btn_up_next");
*/
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

        //CSV処理
        $totalstudyact_size = (((int)$model->getPro["TOTALSTUDYACT"]["gyou"] - 1) * 2) + ((int)$model->getPro["TOTALSTUDYACT"]["moji"] * (int)$model->getPro["TOTALSTUDYACT"]["gyou"] * 3);
        $totalstudyval_size = (((int)$model->getPro["TOTALSTUDYVAL"]["gyou"] - 1) * 2) + ((int)$model->getPro["TOTALSTUDYVAL"]["moji"] * (int)$model->getPro["TOTALSTUDYVAL"]["gyou"] * 3);
        $totalremark_size = (((int)$model->getPro["TOTALREMARK"]["gyou"] - 1) * 2) + ((int)$model->getPro["TOTALREMARK"]["moji"] * (int)$model->getPro["TOTALREMARK"]["gyou"] * 3);
        $attendrec_remark_size = (((int)$model->getPro["ATTENDREC_REMARK"]["gyou"] - 1) * 2) + ((int)$model->getPro["ATTENDREC_REMARK"]["moji"] * (int)$model->getPro["ATTENDREC_REMARK"]["gyou"] * 3);
        $viewremark_size = (((int)$model->getPro["VIEWREMARK"]["gyou"] - 1) * 2) + ((int)$model->getPro["VIEWREMARK"]["moji"] * (int)$model->getPro["VIEWREMARK"]["gyou"] * 3);

        $fieldSize  = "TOTALSTUDYACT={$totalstudyact_size},";
        $fieldSize .= "TOTALSTUDYVAL={$totalstudyval_size},";
        $fieldSize .= "TOTALREMARK={$totalremark_size},";
        $fieldSize .= "ATTENDREC_REMARK={$attendrec_remark_size},";
        $fieldSize .= "VIEWREMARK={$viewremark_size},";

        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A121A/knjx_a121aindex.php?FIELDSIZE=".$fieldSize."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja121aForm1.html", $arg);
    }
}

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
        $extra = "style=\"height:".$height."px;\" onPaste=\"return show(this);\""; 
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        if ($name == "ATTENDREC_REMARK") {
            $moji_size = ($moji > 40) ? 40 : $moji;
            $extra = "onPaste=\"return show(this);\" onkeypress=\"btn_keypress();\""; 
            $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji_size * 2), $moji, $extra);
        } else {
            $extra = "onPaste=\"return show(this);\" onkeypress=\"btn_keypress();\""; 
            $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
        }
    }
    return $retArg;
}
?>
