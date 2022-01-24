<?php

require_once('for_php7.php');

class knja121jForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knja121jindex.php", "", "edit");
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = knja121jQuery::getTrainRow($model->schregno);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //記入欄
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYACT",
                            "cols"        => 41,
                            "rows"        => 4,
                            "wrap"        => "soft",    //NO001
//                            "wrap"        => "hard",
                            "value"       => $row["TOTALSTUDYACT"] ));
        $arg["data"]["TOTALSTUDYACT"] = $objForm->ge("TOTALSTUDYACT");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALSTUDYVAL",
                            "cols"        => 89,
                            "rows"        => 4,
                            "wrap"        => "soft",    //NO001
//                            "wrap"        => "hard",
//                            "extrahtml"   => "style=\"height:90px;\"",
                            "value"       => $row["TOTALSTUDYVAL"] ));
        $arg["data"]["TOTALSTUDYVAL"] = $objForm->ge("TOTALSTUDYVAL");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "TOTALREMARK",
                            "cols"        => 177,
                            "rows"        => 4,
                            "wrap"        => "soft",    //NO001
//                            "wrap"        => "hard",
//                            "extrahtml"   => "style=\"height:90px;\"",
                            "value"       => $row["TOTALREMARK"] ));
        $arg["data"]["TOTALREMARK"] = $objForm->ge("TOTALREMARK");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "ATTENDREC_REMARK",
                            "cols"        => 41,
                            "rows"        => 3,
                            "wrap"        => "soft",    //NO001
//                            "wrap"        => "hard",
                            "value"       => $row["ATTENDREC_REMARK"] ));
        $arg["data"]["ATTENDREC_REMARK"] = $objForm->ge("ATTENDREC_REMARK");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "VIEWREMARK",
                            "cols"        => 41,
                            "rows"        => 4,
                            "wrap"        => "soft",    //NO001
//                            "wrap"        => "hard",
                            "value"       => $row["VIEWREMARK"] ));
        $arg["data"]["VIEWREMARK"] = $objForm->ge("VIEWREMARK");

        $objForm->ae( array("type"        => "textarea",
                            "name"        => "BEHAVEREC_REMARK",
                            "cols"        => 41,
                            "rows"        => 3,
                            "wrap"        => "soft",    //NO001
//                            "wrap"        => "hard",
                            "value"       => $row["BEHAVEREC_REMARK"] ));
        $arg["data"]["BEHAVEREC_REMARK"] = $objForm->ge("BEHAVEREC_REMARK");

        $arg["IFRAME"] = VIEW::setIframeJs();
        //ボタン
        //通知票所見参照ボタンを非表示とする。(近大のみ)
        $cnt = knja121jQuery::getKindaiJudgment();
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
        $fieldSize  = "TOTALSTUDYACT=180,";
        $fieldSize .= "TOTALSTUDYVAL=396,";
        $fieldSize .= "SPECIALACTREMARK=0,";
        $fieldSize .= "TOTALREMARK=792,";
        $fieldSize .= "ATTENDREC_REMARK=120,";
        $fieldSize .= "VIEWREMARK=180,";
        $fieldSize .= "BEHAVEREC_REMARK=120";

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_check1",
                            "value"     => "データCSV",
                            "extrahtml" => " onClick=\" wopen('".REQUESTROOT."/X/KNJX180/knjx180index.php?FIELDSIZE=".$fieldSize."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" ));
        $arg["button"]["btn_check1"] = $objForm->ge("btn_check1");

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

        View::toHTML($model, "knja121jForm1.html", $arg);
    }
}
?>
